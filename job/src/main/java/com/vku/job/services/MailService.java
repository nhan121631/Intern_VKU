package com.vku.job.services;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vku.job.config.EnvLoader;

@Service
public class MailService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    // Email này PHẢI là email đã Verified trên Brevo của bạn
    private final String SENDER_EMAIL = "phamphunhan625@gmail.com";
    private final String SENDER_NAME = "Manageme Tasks System";

    @Async
    public void sendVerificationCode(String to, String code) {
        String content = "Your verification code is: <b>" + code + "</b>. It will expire in 10 minutes.";
        sendBrevoApi(to, "Your Verification Code for Registration", content, null, null);
    }

    @Async
    public void sendPasswordResetCode(String to, String code) {
        String content = "Your password reset code is: <b>" + code + "</b>. It will expire in 10 minutes.";
        sendBrevoApi(to, "Your Password Reset Code", content, null, null);
    }

    @Async
    public void sendPdfReport(String to, byte[] pdfBytes, String subject, String body) {
        // Brevo yêu cầu file đính kèm phải mã hóa Base64
        String base64Content = Base64.getEncoder().encodeToString(pdfBytes);
        sendBrevoApi(to, subject, body, base64Content, "weekly-task-report.pdf");
    }

    private void sendBrevoApi(String to, String subject, String htmlContent, String base64File, String fileName) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("api-key", EnvLoader.get("BREVO_API_KEY"));
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("sender", Map.of("name", SENDER_NAME, "email", SENDER_EMAIL));
            requestBody.put("to", List.of(Map.of("email", to)));
            requestBody.put("subject", subject);
            requestBody.put("htmlContent", "<html><body>" + htmlContent + "</body></html>");

            // Xử lý file đính kèm nếu có
            if (base64File != null && fileName != null) {
                requestBody.put("attachment", List.of(
                        Map.of("content", base64File, "name", fileName)));
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            restTemplate.postForEntity(BREVO_API_URL, entity, String.class);

            System.out.println("Brevo API: Sent email to " + to + " [" + subject + "]");
        } catch (Exception e) {
            System.err.println("Brevo API Error: Failed to send email to " + to);
            e.printStackTrace();
        }
    }
}