package com.bluebox.api.registeration;

import com.bluebox.api.registeration.dto.RegistrationReq;
import com.bluebox.api.registeration.dto.RegistrationResp;
import com.bluebox.service.user.UserEntity;
import com.bluebox.service.user.UserException;
import com.bluebox.service.user.UserService;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.bluebox.Constants.REGISTRATION_BASE;
import static com.bluebox.Constants.VERIFY;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = REGISTRATION_BASE, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class RegistrationController {
    private static final Logger LOGGER = LogManager.getLogger(RegistrationController.class);
    private final UserService service;
    private final ModelMapper mapper;

    @Autowired
    public RegistrationController(final UserService service, final ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public RegistrationResp register(@RequestBody @Valid final RegistrationReq request) throws UserException {
        LOGGER.info("receiving a request for registration");
        LOGGER.info(request::toString);
        final var entity = reqToEntity(request);
        final var response = service.create(entity);
        LOGGER.info(response::toString);
        return entityToResp(response);
    }

    @GetMapping(path = VERIFY, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> verify(@RequestParam("code") final String code, @RequestParam("uuid") final String uuid) throws UserException {
        service.verifyEmail(uuid, code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private RegistrationResp entityToResp(final UserEntity resp) {
        return mapper.map(resp, RegistrationResp.class);
    }

    private UserEntity reqToEntity(final RegistrationReq dto) {
        var entity= mapper.map(dto, UserEntity.class);
        entity.setEmail( StringEscapeUtils.escapeHtml4(entity.getEmail()));
        return entity;
    }

}
