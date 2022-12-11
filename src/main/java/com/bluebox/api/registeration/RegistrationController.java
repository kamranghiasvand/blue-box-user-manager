package com.bluebox.api.registeration;

import com.bluebox.api.registeration.validation.ValidPassword;
import com.bluebox.service.user.UserEntity;
import com.bluebox.service.user.UserException;
import com.bluebox.service.user.UserService;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

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
    public RegistrationController(UserService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public RegistrationResp register(@RequestBody @Valid RegistrationReq request) throws UserException {
        LOGGER.info("receiving a request for registration");
        LOGGER.info(request::toString);
        var entity = reqToEntity(request);
        var response = service.create(entity);
        LOGGER.info(response::toString);
        return entityToResp(response);
    }

    @GetMapping(path = VERIFY, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<String> verify(@RequestParam("code") String code, @RequestParam("uuid") String uuid) throws UserException {
        service.verify(uuid, code);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private RegistrationResp entityToResp(UserEntity resp) {
        return mapper.map(resp, RegistrationResp.class);
    }

    private UserEntity reqToEntity(RegistrationReq dto) {
        return mapper.map(dto, UserEntity.class);
    }

    @Data
    public static class RegistrationReq {
        @NotBlank(message = "firstName is required")
        private String firstName;
        @NotBlank(message = "lastName is required")
        private String lastName;
        @Pattern(regexp = "''|^\\+(?:\\d.?){6,14}\\d$", message = "international phone is required")
        private String phone;
        @NotBlank(message = "email is required")
        @Email(message = "valid email is required")
        protected String email;
        @ValidPassword(message = "valid password is required")
        @NotBlank(message = "password is required")
        private String password;
    }

    @Data
    public static class RegistrationResp {
        private String uuid;
        private Boolean deleted;
        private Long created;
        private Long lastUpdated;
        private String firstName;
        private String lastName;
        private String phone;
        protected String email;
        private Boolean enabled;
    }
}
