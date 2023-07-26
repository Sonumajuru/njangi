package com.genesistech.njangiApi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service

public class EmailServiceImpl {
    private static final String EMAIL_PATTERN = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN, Pattern.CASE_INSENSITIVE);
    @Autowired
    private JavaMailSender emailSender;

    /**
     * handles email sending upon registration
     * @param recipient
     * @param confirmationLink
     */
    public void sendEmail(String recipient, String confirmationLink) {
        SimpleMailMessage mail = new SimpleMailMessage();

        String subject = "Registration Confirmation";
        String message = "Hello there, Yeah, you! Welcome - we're glad you joined the Njangi Community! While you here, let's have fun"
                + "\n\nPlease click the link below to complete registration!"
                + "\n" + confirmationLink
                + "\n\nThank you for registering with us!"
                + "\n\nThe Njangi Staff \u2764";

        mail.setFrom("Njangi <echttune@gmail.com>");
        mail.setTo(recipient);
        mail.setSubject(subject);
        mail.setText(message);

        emailSender.send(mail);
    }

    /**
     * Sends request for reset password
     * @param email
     * @param resetPasswordLink
     */
    public void sendResetPasswordEmail(String email, String resetPasswordLink) {
        SimpleMailMessage mail = new SimpleMailMessage();

        mail.setFrom("EchtTune <echttune@gmail.com>");
        mail.setTo(email);
        mail.setSubject("Reset your password");

        String message = "If you requested to change your password ? please click the link below to reset your password"
                + "\n" + resetPasswordLink
                + "\n\nThe EchtTune Staff";
        mail.setText(message);

        emailSender.send(mail);
    }

    /**
     * Check if email is valid
     * @param email
     * @return true or false if valid email
     */
    public boolean isValidEmail(String email) {
        if (email == null) {
            return false;
        }
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
}