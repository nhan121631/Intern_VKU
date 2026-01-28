package com.vku.job.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.vku.job.config.EnvLoader;
import com.vku.job.services.MailService;

@ExtendWith(MockitoExtension.class)
public class MailServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private MailService mailService;

    private MockedStatic<EnvLoader> envLoaderStatic;

    @BeforeEach
    void setUp() throws Exception {
        // Mock EnvLoader static to return a dummy API key
        envLoaderStatic = Mockito.mockStatic(EnvLoader.class);
        envLoaderStatic.when(() -> EnvLoader.get("BREVO_API_KEY")).thenReturn("dummy-api-key");

        // use real MailService instance and inject mocked RestTemplate via reflection
        mailService = new MailService();
        Field restField = MailService.class.getDeclaredField("restTemplate");
        restField.setAccessible(true);
        restField.set(mailService, restTemplate);

        // default successful response (lenient to avoid unnecessary stubbing errors)
        Mockito.lenient()
                .when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("ok", HttpStatus.OK));
    }

    @AfterEach
    void tearDown() {
        if (envLoaderStatic != null) {
            envLoaderStatic.close();
        }
    }

    // ====== SEND VERIFICATION CODE TESTS ======

    @Test
    void sendVerificationCode_success() {
        // given
        String to = "test@example.com";
        String code = "123456";

        // when
        mailService.sendVerificationCode(to, code);

        // then
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("https://api.brevo.com/v3/smtp/email"), captor.capture(),
                eq(String.class));

        Object body = captor.getValue().getBody();
        assertTrue(body instanceof java.util.Map);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) body;
        java.util.List<?> toList = (java.util.List<?>) map.get("to");
        java.util.Map<?, ?> toEntry = (java.util.Map<?, ?>) toList.get(0);
        assertEquals(to, toEntry.get("email"));
        assertEquals("Your Verification Code for Registration", map.get("subject"));
        assertTrue(((String) map.get("htmlContent")).contains(code));
    }

    @Test
    void sendVerificationCode_whenException_shouldNotThrow() {
        // given
        String to = "test@example.com";
        String code = "123456";

        doThrow(new RuntimeException("error"))
                .when(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        // when & then (no exception expected)
        assertDoesNotThrow(() -> mailService.sendVerificationCode(to, code));

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    // ====== SEND PASSWORD RESET CODE TESTS ======

    @Test
    void sendPasswordResetCode_success() {
        // given
        String to = "test@example.com";
        String code = "654321";

        // when
        mailService.sendPasswordResetCode(to, code);

        // then
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("https://api.brevo.com/v3/smtp/email"), captor.capture(),
                eq(String.class));

        Object body = captor.getValue().getBody();
        assertTrue(body instanceof java.util.Map);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) body;
        java.util.List<?> toList = (java.util.List<?>) map.get("to");
        java.util.Map<?, ?> toEntry = (java.util.Map<?, ?>) toList.get(0);
        assertEquals(to, toEntry.get("email"));
        assertEquals("Your Password Reset Code", map.get("subject"));
        assertTrue(((String) map.get("htmlContent")).contains(code));
    }

    @Test
    void sendPasswordResetCode_whenException_shouldNotThrow() {
        // given
        String to = "test@example.com";
        String code = "654321";

        doThrow(new RuntimeException("error"))
                .when(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        // when & then (no exception expected)
        assertDoesNotThrow(() -> mailService.sendPasswordResetCode(to, code));

        verify(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));
    }

    // ====== SEND PDF REPORT TESTS ======

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
        ArgumentCaptor<HttpEntity> captor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq("https://api.brevo.com/v3/smtp/email"), captor.capture(),
                eq(String.class));

        Object requestBody = captor.getValue().getBody();
        assertTrue(requestBody instanceof java.util.Map);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> map = (java.util.Map<String, Object>) requestBody;
        assertEquals(subject, map.get("subject"));
        assertTrue(map.containsKey("attachment"));
        java.util.List<?> attachments = (java.util.List<?>) map.get("attachment");
        java.util.Map<?, ?> att = (java.util.Map<?, ?>) attachments.get(0);
        assertEquals("weekly-task-report.pdf", att.get("name"));
    }

    @Test
    void sendPdfReport_whenPostFails_shouldNotThrow() {
        // given
        doThrow(new RuntimeException("send error"))
                .when(restTemplate).postForEntity(anyString(), any(HttpEntity.class), eq(String.class));

        // when & then: MailService catches exceptions, so no exception should be thrown
        assertDoesNotThrow(() -> mailService.sendPdfReport(
                "admin@example.com",
                new byte[] { 1, 2, 3 },
                "Weekly Report",
                "Body"));
    }
}
