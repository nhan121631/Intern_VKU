package com.vku.job.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender emailSender;

    @Async
    public void sendVerificationCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your Verification Code for registeration");
            message.setText(
                    "Hi,\n\n" +
                            "Your verification code is: " + code + "\n\n" +
                            "The code will expire in 10 minutes.\n\n" +
                            "If you did not request this, please ignore this email.");
            emailSender.send(message);
            System.out.println("Sent code " + code + " to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to);
            e.printStackTrace();
        }
    }
}
