package com.bluebox.service.user;


import com.bluebox.service.BaseEntity;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Setter
@Getter
@ToString(callSuper = true)
@Entity
@Table(name = "tbl_reset_pass_token")
@AllArgsConstructor
@NoArgsConstructor
public class ResetPassTokenEntity extends BaseEntity {
    @Transient
    private static final int EXPIRATION = 60 * 24;
    @Column(name = "token", nullable = false)
    private String token;
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}