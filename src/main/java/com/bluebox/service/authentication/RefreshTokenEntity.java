package com.bluebox.service.authentication;

import com.bluebox.service.BaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.time.Instant;

@Setter
@Getter
@ToString(callSuper = true)
@Entity(name = "tbl_refresh_token")
public class RefreshTokenEntity extends BaseEntity {
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(name = "expire_data", nullable = false)
    private Instant expiryDate;
}
