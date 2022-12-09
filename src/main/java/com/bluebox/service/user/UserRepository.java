package com.bluebox.service.user;

import com.bluebox.service.BaseRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends BaseRepository<UserEntity> {
    Optional<UserEntity> findByEmail(String email);
}
