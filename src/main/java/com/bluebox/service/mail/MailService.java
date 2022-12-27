package com.bluebox.service.mail;

public interface MailService {
    void send(String toAddress, String senderName, String subject, String content)
            throws MailException;

}
