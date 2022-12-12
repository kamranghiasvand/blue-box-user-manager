package com.bluebox.api.authentication;

import com.bluebox.api.authentication.dto.LoginReq;
import com.bluebox.api.authentication.dto.LoginResp;
import com.bluebox.api.authentication.dto.TokenRefreshReq;
import com.bluebox.api.authentication.dto.TokenRefreshResp;
import com.bluebox.security.JwtUtils;
import com.bluebox.security.UserDetailsImpl;
import com.bluebox.service.authentication.RefreshTokenEntity;
import com.bluebox.service.authentication.RefreshTokenServiceImpl;
import com.bluebox.service.authentication.TokenRefreshException;
import com.bluebox.service.user.UserService;
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

import static com.bluebox.Constants.AUTHENTICATION_BASE;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
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
        var authentication = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        var principle = (UserDetailsImpl) authentication.getPrincipal();
        var token = jwtUtils.generateJwtToken(principle);
        var roles = principle.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList();
        var refreshToken = refreshTokenService.createRefreshToken(principle.getId());
        return ResponseEntity.ok(new LoginResp(token, refreshToken.getToken(), "Bearer", principle.getUid(), principle.getUsername(), roles));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<TokenRefreshResp> refreshToken(@Valid @RequestBody TokenRefreshReq request) {
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


}
