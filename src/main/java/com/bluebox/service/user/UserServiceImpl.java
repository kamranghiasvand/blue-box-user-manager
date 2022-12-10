package com.bluebox.service.user;

import com.bluebox.AppConfig;
import net.bytebuddy.utility.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static com.bluebox.Constants.REGISTRATION_BASE;
import static com.bluebox.Constants.VERIFY;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationRepository verificationRepository;
    private final AppConfig config;
    private final JavaMailSender mailSender;

    @Autowired
    public UserServiceImpl(final UserRepository repo, final PasswordEncoder passEnc, final VerificationRepository vRepo,
                           final AppConfig config, final JavaMailSender mSender) {
        this.repository = repo;
        this.passwordEncoder = passEnc;
        this.verificationRepository = vRepo;
        this.config = config;
        this.mailSender = mSender;
    }

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
        } catch (MessagingException | UnsupportedEncodingException e) {
            throw new UserException("Failed to send email verification link");
        }
        return entity;
    }

    @Override
    public Optional<UserEntity> findByEmail(final String email) {
        return repository.findByEmail(email);
    }

    @Override
    public void verify(String uid, String code) throws UserException {
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

    private void sendVerificationCode(UserEntity user) throws MessagingException, UnsupportedEncodingException {
        LOGGER.info("Sending a verification link for user: {}", user.getUuid());
        var code = generateCode(user);
        sendEmail(user, config.getAppUrl(), code);

    }

    private void sendEmail(UserEntity user, String siteURL, VerificationEntity code)
            throws MessagingException, UnsupportedEncodingException {
        var toAddress = user.getEmail();
        var fromAddress = config.getEmailAddress();
        var senderName = "Food Lovers";
        var subject = "Please verify your registration";
        var content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Your Blue Box Team.";

        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);
        content = content.replace("[[name]]", user.getFullName());
        var verifyURL = siteURL + REGISTRATION_BASE + VERIFY + "?code=" + code.getCode();
        verifyURL += "&uuid=" + user.getUuid();
        content = content.replace("[[URL]]", verifyURL);
        helper.setText(content, true);
        mailSender.send(message);

    }

    private VerificationEntity generateCode(UserEntity user) {
        var entity = new VerificationEntity();
        entity.setCode(RandomString.make(8));
        entity.setUserUid(user.getUuid());
        LOGGER.info("Generated code for verification link : {}", entity.getCode());
        return verificationRepository.save(entity);
    }
}
