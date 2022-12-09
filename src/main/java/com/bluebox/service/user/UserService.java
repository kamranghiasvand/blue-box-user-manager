package com.bluebox.service.user;

import java.util.Optional;

public interface UserService {
    UserEntity create(UserEntity entity) throws UserException;

    Optional<UserEntity> findByEmail(String email);
}
