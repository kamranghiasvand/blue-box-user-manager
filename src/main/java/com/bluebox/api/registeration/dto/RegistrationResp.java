package com.bluebox.api.registeration.dto;

import lombok.Data;

@Data
public class RegistrationResp {
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
