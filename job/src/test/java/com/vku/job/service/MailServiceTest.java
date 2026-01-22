package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.vku.job.services.MailService;

import jakarta.mail.internet.MimeMessage;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        org.mockito.Mockito.lenient()
                .when(emailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    // ====== SEND VERIFICATION CODE TESTS ======

    // send verification code - success
    @Test
    void sendVerificationCode_success() {
        // given
        String to = "test@example.com";
        String code = "123456";

        // when
        mailService.sendVerificationCode(to, code);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(emailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals(to, message.getTo()[0]);
        assertEquals("Your Verification Code for registeration", message.getSubject());
        assertTrue(message.getText().contains(code));
    }

    // send verification code - exception handling
    @Test
    void sendVerificationCode_whenException_shouldNotThrow() {
        // given
        String to = "test@example.com";
        String code = "123456";

        doThrow(new MailSendException("error"))
                .when(emailSender).send(any(SimpleMailMessage.class));

        // when & then (no exception expected)
        assertDoesNotThrow(() -> mailService.sendVerificationCode(to, code));

        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    // ====== SEND PASSWORD RESET CODE TESTS ======

    // send password reset code - success
    @Test
    void sendPasswordResetCode_success() {
        // given
        String to = "test@example.com";
        String code = "654321";

        // when
        mailService.sendPasswordResetCode(to, code);

        // then
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(emailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals(to, message.getTo()[0]);
        assertEquals("Your Password Reset Code", message.getSubject());
        assertTrue(message.getText().contains(code));
    }

    // send password reset code - exception handling
    @Test
    void sendPasswordResetCode_whenException_shouldNotThrow() {
        // given
        String to = "test@example.com";
        String code = "654321";

        doThrow(new MailSendException("error"))
                .when(emailSender).send(any(SimpleMailMessage.class));

        // when & then (no exception expected)
        assertDoesNotThrow(() -> mailService.sendPasswordResetCode(to, code));

        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    // ====== SEND PDF REPORT TESTS ======

    // send PDF report - success
    @Test
    void sendPdfReport_success() {
        // given
        String to = "admin@example.com";
        byte[] pdfBytes = "pdf-content".getBytes();
        String subject = "Weekly Report";
        String body = "Please see attached report";

        // when
        mailService.sendPdfReport(to, pdfBytes, subject, body);

        // then
        verify(emailSender).send(mimeMessage);
    }

    // send PDF report - exception handling
    @Test
    void sendPdfReport_whenCreateMimeMessageFails_shouldThrowException() {
        // given
        when(emailSender.createMimeMessage())
                .thenThrow(new RuntimeException("create message error"));

        // when & then
        assertThrows(RuntimeException.class, () -> mailService.sendPdfReport(
                "admin@example.com",
                new byte[] { 1, 2, 3 },
                "Weekly Report",
                "Body"));
    }

    // send PDF report - exception handling
    @Test
    void sendPdfReport_whenSendFails_shouldThrowException() {
        // given
        doThrow(new RuntimeException("send error"))
                .when(emailSender).send(any(MimeMessage.class));

        // when & then
        assertThrows(RuntimeException.class, () -> mailService.sendPdfReport(
                "admin@example.com",
                new byte[] { 1, 2, 3 },
                "Weekly Report",
                "Body"));
    }
}
