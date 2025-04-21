package com.cst438.test.utils;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.cst438.dto.LoginDTO;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class SecurityBaseTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    protected String baseUrl;
    protected HttpHeaders adminHeaders;
    protected HttpHeaders studentHeaders;
    protected HttpHeaders instructorHeaders;

    @BeforeEach
    public void setupTest() {
        baseUrl = "http://localhost:" + port;
        
        // Setup headers for each user type
        adminHeaders = getHeadersForUser("admin@csumb.edu", "admin");
        studentHeaders = getHeadersForUser("user@csumb.edu", "user");
        instructorHeaders = getHeadersForUser("instructor@csumb.edu", "instructor");
    }
    
    protected HttpHeaders getHeadersForUser(String email, String password) {
        // Create basic auth header for initial login
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(email, password);
        
        // Get JWT token
        ResponseEntity<LoginDTO> response = restTemplate.exchange(
                baseUrl + "/login", 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                LoginDTO.class);
        
        // Create new headers with JWT token
        HttpHeaders jwtHeaders = new HttpHeaders();
        jwtHeaders.add("Authorization", "Bearer " + response.getBody().jwt());
        return jwtHeaders;
    }
    
    protected <T> ResponseEntity<T> performGetRequest(String endpoint, HttpHeaders headers, Class<T> responseType) {
        return restTemplate.exchange(
                baseUrl + endpoint,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                responseType);
    }
    
    protected <T, R> ResponseEntity<R> performPostRequest(String endpoint, T body, HttpHeaders headers, Class<R> responseType) {
        return restTemplate.exchange(
                baseUrl + endpoint,
                HttpMethod.POST,
                new HttpEntity<>(body, headers),
                responseType);
    }
    
    protected <T, R> ResponseEntity<R> performPutRequest(String endpoint, T body, HttpHeaders headers, Class<R> responseType) {
        return restTemplate.exchange(
                baseUrl + endpoint,
                HttpMethod.PUT,
                new HttpEntity<>(body, headers),
                responseType);
    }
    
    protected <R> ResponseEntity<R> performDeleteRequest(String endpoint, HttpHeaders headers, Class<R> responseType) {
        return restTemplate.exchange(
                baseUrl + endpoint,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                responseType);
    }
} 