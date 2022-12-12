package com.bluebox.api.authentication.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class TokenRefreshReq {
    @NotBlank
    private String refreshToken;
}
