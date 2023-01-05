//package com.bluebox.service.role;
//
//
//import com.bluebox.service.BaseEntity;
//import lombok.Getter;
//import lombok.Setter;
//import lombok.ToString;
//import org.hibernate.Hibernate;
//
//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Table;
//import java.util.Objects;
//
//@Setter
//@Getter
//@ToString(callSuper = true)
//@Entity
//@Table(name = "tbl_permission")
//public class PermissionEntity extends BaseEntity {
//
//    @Column(name = "name")
//    private String name;
//    @Column(name = "enabled")
//    private Boolean enabled = false;
//
//
//    @Override
//    public boolean equals(final Object o) {
//        if (this == o) return true;
//        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
//        final var entity = (PermissionEntity) o;
//        return getId() != null && Objects.equals(getId(), entity.getId());
//    }
//
//    @Override
//    public int hashCode() {
//        return getClass().hashCode();
//    }
//}