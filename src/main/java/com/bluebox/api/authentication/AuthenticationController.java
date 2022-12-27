package com.bluebox.api.authentication;

import com.bluebox.api.authentication.dto.*;
import com.bluebox.security.JwtUtils;
import com.bluebox.security.UserDetailsImpl;
import com.bluebox.service.authentication.RefreshTokenEntity;
import com.bluebox.service.authentication.RefreshTokenServiceImpl;
import com.bluebox.service.authentication.TokenRefreshException;
import com.bluebox.service.user.UserException;
import com.bluebox.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.bluebox.Constants.AUTHENTICATION_BASE;
import static org.springframework.util.MimeTypeUtils.*;

@Controller
@RequestMapping(value = AUTHENTICATION_BASE, produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AuthenticationController {
    private static final Logger LOGGER = LogManager.getLogger(AuthenticationController.class);
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authManager;
    private final RefreshTokenServiceImpl refreshTokenService;
    private final UserService userService;


    @PostMapping("/sign-in")
    public ResponseEntity<LoginResp> authenticateUser(@Valid @RequestBody LoginReq req) {
        LOGGER.info("Received a sign-in request from: {}", req.getEmail());
        var authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        LOGGER.debug("user is authenticated");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var principle = (UserDetailsImpl) authentication.getPrincipal();
        var token = jwtUtils.generateJwtToken(principle);
        LOGGER.debug("JWT token is created");
        var roles = principle.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        var refreshToken = refreshTokenService.createRefreshToken(principle.getId());
        LOGGER.debug("Refresh token is created");
        return ResponseEntity.ok(new LoginResp(token, refreshToken.getToken(), "Bearer", principle.getUid(), principle.getUsername(), roles));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResp> refreshToken(@Valid @RequestBody final TokenRefreshReq request) {
        LOGGER.info("Received a refresh-token request for: {}...", () -> request.getRefreshToken().substring(0, 6));
        var requestRefreshToken = request.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshTokenEntity::getUserId)
                .map(id -> userService.findById(id).map(user -> {
                    var token = jwtUtils.generateTokenFromUsername(user.getEmail());
                    return ResponseEntity.ok(new TokenRefreshResp(token, requestRefreshToken, "Bearer"));
                }).orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "User does not exists")))
                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }

    @PostMapping("/request-reset-password")
    public ResponseEntity<Object> resetPassword(
            @Valid
            @RequestParam("email")
            @NotNull(message = "Email cannot be null") final String email) throws UserException {
        userService.sendResetPassEmail(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/change-password", consumes = ALL_VALUE)
    public ModelAndView changePassword(
            @Valid
            @RequestParam("token")
            @NotNull(message = "Token cannot be null") final String token) throws UserException {
        userService.verifyResetPassToken(token);

        var resp = new ModelAndView("change-password");
        var email = "";
        var userOption = userService.findByResetPassToken(token);
        if (userOption.isPresent()) {
            email = userOption.get().getEmail();
        }
        resp.getModelMap().addAttribute("email", email);
        resp.getModelMap().addAttribute("token", token);
        return resp;
    }

    @PostMapping(value = "/change-password", consumes = ALL_VALUE)
    public ResponseEntity<Object> changePassword(@Valid @RequestBody ChangePassReq req) throws UserException {
        userService.changePassword(req.getEmail(), req.getPassword(), req.getToken());
        return ResponseEntity.ok().build();
    }

}
