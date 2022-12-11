package com.bluebox.api.authentication;

import com.bluebox.api.registeration.validation.ValidPassword;
import com.bluebox.security.JwtUtils;
import com.bluebox.security.UserDetailsImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.List;

import static com.bluebox.Constants.AUTHENTICATION_BASE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = AUTHENTICATION_BASE, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationController.class);
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authManager;


    @PostMapping("/sign-in")
    public ResponseEntity<LoginResp> authenticateUser(@Valid @RequestBody LoginReq req) {
        var authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var token = jwtUtils.generateJwtToken(authentication);

        var principle = (UserDetailsImpl) authentication.getPrincipal();
        var roles = principle.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();

        return ResponseEntity.ok(new LoginResp(token, "Bearer", principle.getUid(), principle.getUsername(), roles));
    }


    @Data
    public static class LoginReq {
        @NotBlank(message = "email is required")
        @Email(message = "valid email is required")
        protected String email;
        @NotBlank(message = "password is required")
        @ValidPassword
        private String password;
    }

    @Data
    @AllArgsConstructor
    public static class LoginResp {
        private String token;
        private String type;
        private String uid;
        private String email;
        private List<String> roles;
    }
}
