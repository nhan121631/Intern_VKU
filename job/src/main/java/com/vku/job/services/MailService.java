package com.vku.job.services;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

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

    @Async
    public void sendPasswordResetCode(String to, String code) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Your Password Reset Code");
            message.setText(
                    "Hi,\n\n" +
                            "Your password reset code is: " + code + "\n\n" +
                            "The code will expire in 10 minutes.\n\n" +
                            "If you did not request this, please ignore this email.");
            emailSender.send(message);
            System.out.println("Sent password reset code " + code + " to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to);
            e.printStackTrace();
        }
    }

    @Async
    public void sendPdfReport(String to, byte[] pdfBytes, String subject, String body) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false = text, true = HTML
            helper.setSentDate(new Date());

            // attach PDF
            ByteArrayResource pdfResource = new ByteArrayResource(pdfBytes);
            helper.addAttachment("weekly-task-report.pdf", pdfResource);

            emailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send PDF report email", e);
        }
    }
}