package com.bluebox.service.user;

import java.util.Optional;

public interface UserService {
    UserEntity create(UserEntity entity) throws UserException;

    Optional<UserEntity> findByEmail(final String email);

    Optional<UserEntity> findById(final Long id);

    void verifyEmail(final String uid, final String code) throws UserException;

    void sendResetPassEmail(final String email) throws UserException;

    void verifyResetPassToken(final String token) throws UserException;

    Optional<UserEntity> findByResetPassToken(final String token) throws UserException;

    void changePassword(final String email, final String password, final String token) throws UserException;
}
