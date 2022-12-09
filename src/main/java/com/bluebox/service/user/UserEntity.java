package com.bluebox.service.user;


import com.bluebox.service.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import static com.bluebox.Constants.UNIQUE_USER_EMAIL;

@Setter
@ToString(callSuper = true, exclude = {"password"})
@EqualsAndHashCode(callSuper = true, of = {})
@Entity
@Table(name = "tbl_user",
        uniqueConstraints = @UniqueConstraint(name = UNIQUE_USER_EMAIL, columnNames = "email"))
public class UserEntity extends BaseEntity {

    private String firstName;
    private String lastName;
    private String phone;
    protected String email;
    private String password;
    private boolean isEnabled = true;

    @Column(name = "first_name")
    public String getFirstName() {
        return firstName;
    }

    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    @Column(name = "password")
    public String getPassword() {
        return password;
    }

    @Column(name = "enabled")
    public boolean isEnabled() {
        return isEnabled;
    }

    @Column(name = "phone")
    public String getPhone() {
        return phone;
    }

    @Column(name = "email", nullable = false)
    public String getEmail() {
        return email;
    }
}