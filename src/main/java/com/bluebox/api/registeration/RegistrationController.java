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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import java.sql.Timestamp;

import static com.bluebox.Constants.REGISTRATION_BASE;
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
    public RegistrationResp register(@RequestBody @Valid RegistrationController.RegistrationReq request) throws UserException {
        LOGGER.info("receiving a request for registration");
        LOGGER.info(request::toString);
        UserEntity entity = reqToEntity(request);
        UserEntity response = service.create(entity);
        LOGGER.info(response::toString);
        return entityToResp(response);
    }

    private RegistrationResp entityToResp(UserEntity resp) {
        return mapper.map(resp, RegistrationResp.class);
    }

    private UserEntity reqToEntity(RegistrationReq dto) {
        return mapper.map(dto, UserEntity.class);
    }

    @Data
    private static class RegistrationReq {
        @NotBlank(message = "firstName is required")
        private String firstName;
        @NotBlank(message = "lastName is required")
        private String lastName;
        @Pattern(regexp = "''|^\\+(?:\\d.?){6,14}\\d$", message = "phone should be in international form")
        private String phone;
        @Email
        protected String email;
        @ValidPassword
        private String password;
    }

    @Data
    private static class RegistrationResp {
        private Long id;
        private String uuid;
        private Boolean deleted;
        private Timestamp created;
        private Timestamp lastUpdated;
        private String firstName;
        private String lastName;
        private String phone;
        protected String email;
        private String password;
    }
}
