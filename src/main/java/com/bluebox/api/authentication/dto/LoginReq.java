package com.bluebox.api.authentication.dto;

import com.bluebox.api.registeration.validation.ValidPassword;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class LoginReq {
    @NotBlank(message = "email is required")
    @Email(message = "valid email is required")
    protected String email;
    @NotBlank(message = "password is required")
    @ValidPassword
    private String password;
}
