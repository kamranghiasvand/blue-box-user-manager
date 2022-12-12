package com.bluebox.service.user;

import com.bluebox.AppConfig;
import com.bluebox.security.UserDetailsImpl;
import com.bluebox.service.mail.EmailException;
import com.bluebox.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.bluebox.Constants.REGISTRATION_BASE;
import static com.bluebox.Constants.VERIFY;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepository verificationRepository;
    private final AppConfig config;
    private final MailService mailService;


    @Override
    public UserEntity create(final UserEntity user) throws UserException {
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
        var entity = repository.save(user);
        try {
            sendVerificationCode(entity);
        } catch (EmailException e) {
            throw new UserException("Failed to send email verification link", e);
        }
        return entity;
    }

    @Override
    public Optional<UserEntity> findByEmail(final String email) {
        LOGGER.info("Finding user by email: {}", email);
        return repository.findByEmail(email);
    }

    @Override
    public Optional<UserEntity> findById(final Long id) {
        LOGGER.info("Finding user by id: {}", id);
        return repository.findById(id);
    }

    @Override
    public void verifyEmail(String uid, String code) throws UserException {
        LOGGER.info("Verifying user with code : {} and uuid: {}", code, uid);
        var userOpt = repository.findByUuid(uid);
        if (userOpt.isEmpty()) {
            LOGGER.error("Could not find the user with uuid: {}", uid);
            throw new UserException("Verification code id invalid");
        }
        var codeOpt = verificationRepository.findByUserUidAndCode(uid, code);
        if (codeOpt.isEmpty()) {
            LOGGER.error("Could not find an entity in database for code: {} and uuid: {}", code, uid);
            throw new UserException("Verification code id invalid");
        }
        LOGGER.info("Deleting the verification code from database");
        verificationRepository.delete(codeOpt.get());
        var user = userOpt.get();
        LOGGER.info("Setting user enabled: true");
        user.setEnabled(true);
        repository.save(user);
    }

    private void sendVerificationCode(UserEntity user) throws EmailException {
        LOGGER.info("Sending a verification link for user: {}", user.getUuid());
        var code = generateCode(user);
        sendEmail(user, config.getAppUrl(), code);

    }

    private void sendEmail(UserEntity user, String siteURL, VerificationEntity code)
            throws EmailException {
        var senderName = "Food Lovers";
        var subject = "Please verify your registration";
        var content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Your Blue Box Team.";

        content = content.replace("[[name]]", user.getFullName());
        var verifyURL = siteURL + REGISTRATION_BASE + VERIFY + "?code=" + code.getCode();
        verifyURL += "&uuid=" + user.getUuid();
        content = content.replace("[[URL]]", verifyURL);
        mailService.sendEmail(user.getEmail(), senderName, subject, content);

    }

    private VerificationEntity generateCode(UserEntity user) {
        verificationRepository.deleteAllByUserUid(user.getUuid());
        var entity = new VerificationEntity();
        entity.setCode(RandomString.make(8));
        entity.setUserUid(user.getUuid());
        LOGGER.info("Generated code for verification link : {}", entity.getCode());
        return verificationRepository.save(entity);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var option = findByEmail(email);
        if (option.isEmpty()) {
            throw new UsernameNotFoundException("No user found with username: " + email);
        }
        var user = option.get();
        return UserDetailsImpl.build(user);
    }
}
