package com.bluebox.service.user;


import com.bluebox.service.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Setter
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true, of = {})
@Entity
@Table(name = "tbl_verification")
public class VerificationEntity extends BaseEntity {

    @Column(name = "code")
    private String code;
    @Column(name = "user_uid")
    private String userUid;
}