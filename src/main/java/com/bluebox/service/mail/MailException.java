package com.bluebox.service.mail;

public class MailException extends Exception {
    public MailException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public MailException(final String message) {
        super(message);
    }
}
