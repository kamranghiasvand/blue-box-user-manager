package com.bluebox.service.mail;

import com.bluebox.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public final class MailServiceImpl implements MailService {
    private final AppConfig config;
    private final JavaMailSender mailSender;

    @Autowired
    public MailServiceImpl(AppConfig config, JavaMailSender mailSender) {
        this.config = config;
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(String toAddress, String senderName, String subject, String content) throws EmailException {
        try {
            var fromAddress = config.getEmailAddress();
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message);
            helper.setFrom(fromAddress, senderName);
            helper.setTo(toAddress);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (Exception ex) {
            throw new EmailException("Cannot send the email", ex);
        }

    }
}
