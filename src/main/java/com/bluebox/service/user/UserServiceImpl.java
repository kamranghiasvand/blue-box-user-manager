package com.bluebox.service.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserEntity create(UserEntity user) throws UserException {
        if (user == null) {
            LOGGER.error("User cannot be null");
            throw new UserException("User cannot be null");
        }
        var current = findByEmail(user.getEmail());
        if (current.isPresent()) {
            LOGGER.error("User already exists");
            throw new UserException("User already exists");
        }
        if (user.getPassword() != null) {
            LOGGER.info("Password is not null. Applying encryption");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            LOGGER.warn("User does not have a password");
        }
        return repository.save(user);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return repository.findByEmail(email);
    }
}
