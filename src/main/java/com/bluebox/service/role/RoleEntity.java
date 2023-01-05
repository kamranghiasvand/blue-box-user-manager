package com.bluebox.service.role;


import com.bluebox.service.BaseEntity;
import com.bluebox.service.user.UserEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.Hibernate;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

import static com.bluebox.Constants.UNIQUE_ROLE_NAME;

@Setter
@Getter
@ToString(callSuper = true)
@Entity
@Table(name = "tbl_role", uniqueConstraints = @UniqueConstraint(name = UNIQUE_ROLE_NAME, columnNames = "name"))
public class RoleEntity extends BaseEntity {

    @Column(name = "name")
    private String name;

    @Column(name = "enabled")
    private Boolean enabled = false;

    @ManyToMany(mappedBy = "roles")
    private Collection<UserEntity> users;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        final var entity = (RoleEntity) o;
        return getId() != null && Objects.equals(getId(), entity.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}