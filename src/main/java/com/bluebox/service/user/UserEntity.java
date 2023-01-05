package com.bluebox.service.user;


import com.bluebox.service.BaseEntity;
//import com.bluebox.service.role.RoleEntity;
import lombok.Data;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Objects;

import static com.bluebox.Constants.UNIQUE_USER_EMAIL;

@Data
@ToString(callSuper = true, exclude = {"password"})
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
    @NotNull(message = "email is required")
    @Email(message = "valid email is required")
    @Column(name = "email", nullable = false)
    protected String email;
    @Column(name = "password")
    private String password;
    @Column(name = "enabled")
    private Boolean enabled = false;
//    @ManyToMany
//    @JoinTable(
//            name = "users_roles",
//            joinColumns = @JoinColumn(
//                    name = "user_id", referencedColumnName = "pk_id"),
//            inverseJoinColumns = @JoinColumn(
//                    name = "role_id", referencedColumnName = "pk_id"))
//    private Collection<RoleEntity> roles;


    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        UserEntity entity = (UserEntity) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}