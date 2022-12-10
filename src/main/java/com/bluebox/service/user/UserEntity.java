package com.bluebox.service.user;


import com.bluebox.service.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

import static com.bluebox.Constants.UNIQUE_USER_EMAIL;

@Setter
@Getter
@ToString(callSuper = true, exclude = {"password"})
@EqualsAndHashCode(callSuper = true, of = {})
@Entity
@Table(name = "tbl_user",
        uniqueConstraints = @UniqueConstraint(name = UNIQUE_USER_EMAIL, columnNames = "email"))
public class UserEntity extends BaseEntity {

    @Column(name = "first_name")
    private String firstName;
    @Column(name = "last_name")
    private String lastName;
    @Column(name = "phone")
    private String phone;
    @Column(name = "email", nullable = false)
    protected String email;
    @Column(name = "password")
    private String password;
    @Column(name = "enabled")
    private Boolean enabled = false;

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }
}