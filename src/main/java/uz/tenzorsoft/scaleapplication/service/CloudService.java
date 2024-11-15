package uz.tenzorsoft.scaleapplication.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uz.tenzorsoft.scaleapplication.domain.Instances;
import uz.tenzorsoft.scaleapplication.domain.entity.LogEntity;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudService {

    private final LogService logService;

    public boolean login(String phoneNumber, String password) {
        String url = "https://api-scale.mycoal.uz/auth/login";

        // Create request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phoneNumber", phoneNumber);
        requestBody.put("password", password);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "*/*");

        // Create HttpEntity with JSON body
        HttpEntity<String> requestEntity = null;
        try {
            // Convert request body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        // Create RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Debug logging
            System.out.println("URL: " + url);
            System.out.println("Request Headers: " + headers);
            System.out.println("Request Body: " + requestEntity.getBody());

            // Send the POST request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // Check if the response status is 200 OK
            if (response.getStatusCodeValue() == 200) {
                System.out.println("CODE SENT SUCCESSFULLY");
                return true;
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public String verify(String phoneNumber, String password, String code) {
        String url = "https://api-scale.mycoal.uz/auth/verify";

        // Create request body
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("phoneNumber", phoneNumber);
        requestBody.put("password", password);
        requestBody.put("code", code);

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        headers.set("Accept", "*/*");

        // Create HttpEntity with JSON body
        HttpEntity<String> requestEntity = null;
        try {
            // Convert request body to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);
            requestEntity = new HttpEntity<>(jsonRequestBody, headers);
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
            e.printStackTrace();
            return "";
        }

        // Create RestTemplate
        RestTemplate restTemplate = new RestTemplate();

        try {
            // Debug logging
            System.out.println("URL: " + url);
            System.out.println("Request Headers: " + headers);
            System.out.println("Request Body: " + requestEntity.getBody());

            // Send the POST request
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);

            // Check if the response status is 200 OK
            if (response.getStatusCodeValue() == 200) {
                System.out.println("CODE SENT SUCCESSFULLY");
                return response.getBody();
            }
        } catch (HttpClientErrorException e) {
            return "INCORRECT_PASSWORD";
        } catch (Exception e) {
            logService.save(new LogEntity(5L, Instances.truckNumber, e.getMessage()));
            e.printStackTrace();
            return "";
        }

        return "";
    }

}
