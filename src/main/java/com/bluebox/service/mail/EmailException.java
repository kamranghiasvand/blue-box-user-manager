package com.bluebox.service.mail;

public class EmailException extends Exception {
    public EmailException(String message,Throwable cause) {
        super(message,cause);
    }
}
