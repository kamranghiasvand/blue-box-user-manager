package com.bluebox.api.registeration.dto;

import com.bluebox.api.registeration.validation.ValidPassword;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegistrationReq {
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
