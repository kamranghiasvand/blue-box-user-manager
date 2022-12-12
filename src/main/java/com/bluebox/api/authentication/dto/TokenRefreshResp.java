package com.bluebox.api.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshResp {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
}
