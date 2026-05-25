package com.smd.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @SuppressWarnings("unchecked")
    public String generateContent(String prompt) {
        String maskedKey = apiKey != null && apiKey.length() > 8
                ? apiKey.substring(0, 8) + "..." : "(empty)";
        log.info("[Gemini] Calling API — url={} key={} promptLength={}",
                apiUrl, maskedKey, prompt != null ? prompt.length() : 0);

        try {
            if (apiKey == null || apiKey.isBlank() || apiKey.equals("YOUR_GEMINI_API_KEY_HERE")) {
                log.error("[Gemini] API key is missing or is still the placeholder value");
                return fallback();
            }

            String url = apiUrl + "?key=" + apiKey;

            Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of(
                    "parts", List.of(Map.of("text", prompt))
                ))
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            log.info("[Gemini] Sending POST request to Gemini API...");

            Map<String, Object> response = restTemplate.postForObject(
                url,
                new HttpEntity<>(requestBody, headers),
                Map.class
            );

            log.info("[Gemini] Response received: {}", response != null ? response.keySet() : "null");

            if (response == null) {
                log.error("[Gemini] Response is null");
                return fallback();
            }

            List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
            if (candidates == null || candidates.isEmpty()) {
                log.error("[Gemini] No candidates in response. Full response: {}", response);
                return fallback();
            }

            Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
            if (content == null) {
                log.error("[Gemini] content is null in candidate[0]. Candidate: {}", candidates.get(0));
                return fallback();
            }

            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
            if (parts == null || parts.isEmpty()) {
                log.error("[Gemini] parts is empty. Content: {}", content);
                return fallback();
            }

            String text = (String) parts.get(0).get("text");
            if (text == null || text.isBlank()) {
                log.error("[Gemini] text is blank. Part: {}", parts.get(0));
                return fallback();
            }

            log.info("[Gemini] Success — returned {} chars", text.length());
            return text;

        } catch (HttpClientErrorException e) {
            log.error("[Gemini] HTTP {} client error — body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return fallback();
        } catch (HttpServerErrorException e) {
            log.error("[Gemini] HTTP {} server error — body: {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
            return fallback();
        } catch (Exception e) {
            log.error("[Gemini] Unexpected error: {}", e.getMessage(), e);
            return fallback();
        }
    }

    private String fallback() {
        return "AI Summary tạm thời không khả dụng. Vui lòng thử lại sau.";
    }
}
