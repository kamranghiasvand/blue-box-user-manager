package com.bluebox.api.authentication.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class LoginResp {
    private String token;
    private String refreshToken;
    private String type;
    private String uid;
    private String email;
    private List<String> roles;
}
