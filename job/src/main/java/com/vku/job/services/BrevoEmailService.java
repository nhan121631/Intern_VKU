package com.vku.job.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.vku.job.config.EnvLoader;

@Service
public class BrevoEmailService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void sendEmail(String toEmail, String subject, String content) {
        // 1. Chuẩn bị Header với API Key từ file môi trường
        HttpHeaders headers = new HttpHeaders();
        headers.set("api-key", EnvLoader.get("BREVO_API_KEY"));
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 2. Chuẩn bị Body (JSON) theo chuẩn API của Brevo
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sender", Map.of(
                "name", "Hệ thống Quản lý Task",
                "email", "phamphunhan625@gmail.com" // THAY BẰNG GMAIL ĐÃ VERIFY CỦA BẠN
        ));

        // Phần 'to' là danh sách người nhận, giúp bạn gửi cho nhiều user khác nhau
        requestBody.put("to", List.of(Map.of("email", toEmail)));

        requestBody.put("subject", subject);
        requestBody.put("htmlContent", "<html><body>" + content + "</body></html>");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 3. Gọi API Brevo
        try {
            String url = "https://api.brevo.com/v3/smtp/email";
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            // Log kết quả để bạn dễ debug ở Local
            System.out.println("Gửi mail thành công tới: " + toEmail);
            System.out.println("Phản hồi từ Brevo: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Lỗi gửi mail tới " + toEmail + ": " + e.getMessage());
        }
    }
}