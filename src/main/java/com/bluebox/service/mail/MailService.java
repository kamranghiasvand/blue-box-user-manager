package com.bluebox.service.mail;

public interface MailService {
    void sendEmail(String toAddress, String senderName, String subject, String content)
            throws EmailException;

}
