package com.bluebox.user;

import com.bluebox.service.user.UserEntity;
import com.bluebox.service.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static com.bluebox.Constants.UNIQUE_USER_EMAIL;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("user")
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @BeforeEach
    public void before() {
        repository.deleteAll();
    }

    @Test()
    void createWithoutEmail_shouldThrowException() {
        var user = (new UserBuilder()).build();
        var exception = assertThrows(DataIntegrityViolationException.class, () -> repository.save(user));
        String message = exception.getMessage();
        assertNotNull(message);
        Assertions.assertTrue(message.contains("email"));
    }

    @Test()
    void createCorrectUser_shouldWork() {
        var user = (new UserBuilder()).email("test@email.com").build();
        UserEntity saved = repository.save(user);
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertNotNull(saved.getUuid());
    }

    @Test()
    void serializingUser_shouldNotContainPassword() {
        var user = (new UserBuilder()).email("test@email.com").password("pass").build();
        assertFalse(user.toString().contains("pass"));
    }

    @Test()
    void createWithDuplicateEmail_shouldThrowException() {
        var first = (new UserBuilder()).email("test@email.com").build();
        repository.save(first);
        var second = (new UserBuilder()).email("test@email.com").build();
        var exception = assertThrows(DataIntegrityViolationException.class, () -> repository.save(second));
        String message = exception.getMessage();
        assertNotNull(message);
        Assertions.assertTrue(message.toLowerCase().contains(UNIQUE_USER_EMAIL));
    }

}
