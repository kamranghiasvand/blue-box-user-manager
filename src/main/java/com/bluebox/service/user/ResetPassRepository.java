package com.bluebox.service.user;

import com.bluebox.service.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetPassRepository extends BaseRepository<ResetPassTokenEntity> {
    Optional<ResetPassTokenEntity> findByToken(String token);

    @Modifying
    void deleteAllByUserId(Long id);
}
