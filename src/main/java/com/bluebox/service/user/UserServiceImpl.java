package com.bluebox.service.user;

import com.bluebox.AppConfig;
import com.bluebox.security.UserDetailsImpl;
import com.bluebox.service.mail.MailException;
import com.bluebox.service.mail.MailFactory;
import com.bluebox.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.bluebox.Constants.REGISTRATION_BASE;
import static com.bluebox.Constants.VERIFY;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private static final Logger LOGGER = LogManager.getLogger(UserServiceImpl.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepository verificationRepository;
    private final AppConfig config;
    private final MailService mailService;
    private final ResetPassRepository resetPassRepo;
    private final ApplicationContext context;


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
        } catch (MailException e) {
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

    @Override
    public void sendResetPassEmail(final String email) throws UserException {
        try {
            var option = findByEmail(email);
            if (option.isEmpty()) {
                throw new UserException(MessageFormat.format("User with email {} is not found", email));
            }
            var user = option.get();
            removeResetPassToken(user);
            var token = generateRestPassToken(user);
            var mailFactory = context.getBean(MailFactory.class);
            LOGGER.info("Sending a reset pass email to user: {}", user.getUuid());
            mailFactory.to(user.getEmail())
                    .withSubject("Reset Password")
                    .resetPass()
                    .withToken(token)
                    .and()
                    .send();
            LOGGER.info("Sent reset pass email");
        } catch (MailException ex) {
            LOGGER.error("Error occurs while sending reset pass");
            throw new UserException("Could not send reset password", ex);
        }
    }

    private void removeResetPassToken(UserEntity user) {
        LOGGER.info("Removing all reset password token for user: {}", user.getUuid());
        resetPassRepo.deleteAllByUserId(user.getId());
    }

    @Override
    public void verifyResetPassToken(String token) throws UserException {
        LOGGER.info("Verifying reset pass token");
        var optional = resetPassRepo.findByToken(token);
        boolean expired = optional.map(item -> {
            var instant = Instant.now().minus(24, ChronoUnit.HOURS);
            var timestamp = Timestamp.from(instant);
            return item.getCreated().before(timestamp);
        }).orElseThrow(() -> new UserException("Token is not found"));
        if (expired)
            throw new UserException("Token is expired");
        LOGGER.info("Token is valid");
    }

    @Override
    public Optional<UserEntity> findByResetPassToken(final String token) {
        LOGGER.info("Finding user by reset pass token: {}...", () -> token.substring(0, 6));
        var option = resetPassRepo.findByToken(token);
        if (option.isEmpty()) {
            LOGGER.info("Token is not found: {}...", () -> token.substring(0, 6));
            return Optional.empty();
        }
        return repository.findById(option.get().getUserId());
    }

    @Override
    public void changePassword(final String email, final String password, final String token) throws UserException {
        LOGGER.info("Changing password for user: {}", email);
        verifyResetPassToken(token);
        var option = findByResetPassToken(token);
        if (option.isEmpty()) {
            throw new UserException("User is not found");
        }
        var user = option.get();
        if (!Objects.equals(user.getEmail(), email)) {
            LOGGER.error("Provided email ({}) is not equal to the user's email in DB.", email);
            throw new UserException("User is not found");
        }
        user.setPassword(passwordEncoder.encode(password));
        repository.save(user);
        LOGGER.info("Password is changed");
        removeResetPassToken(user);
    }

    private String generateRestPassToken(UserEntity user) {
        LOGGER.info("Creating new reset password for user: {}", user.getUuid());
        var token = UUID.randomUUID().toString();
        var entity = new ResetPassTokenEntity(token, user.getId());
        resetPassRepo.save(entity);
        LOGGER.info("Reset password token created");
        return token;
    }


    private void sendVerificationCode(UserEntity user) throws MailException {
        LOGGER.info("Sending the verification email for user: {}", user.getUuid());
        var code = generateCode(user);
        sendEmail(user, config.getAppUrl(), code);
        LOGGER.info("The verification email is sent");

    }

    private void sendEmail(UserEntity user, String siteURL, VerificationEntity code)
            throws MailException {
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
        mailService.send(user.getEmail(), senderName, subject, content);

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
