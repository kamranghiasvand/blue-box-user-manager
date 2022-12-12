package com.bluebox.service.authentication;

import com.bluebox.service.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends BaseRepository<RefreshTokenEntity> {
    Optional<RefreshTokenEntity> findByToken(String token);

    @Modifying
    int deleteAllByUserId(Long id);
}
