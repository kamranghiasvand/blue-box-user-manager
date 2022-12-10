package com.bluebox.service.user;

import com.bluebox.service.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRepository extends BaseRepository<VerificationEntity> {
    Optional<VerificationEntity> findByUserUidAndCode(String uid, String code);
}
