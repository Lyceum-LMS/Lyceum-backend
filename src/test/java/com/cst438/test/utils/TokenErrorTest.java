package com.cst438.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class TokenErrorTest extends SecurityBaseTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_1_MissingToken_Unauthorized() {
        // Make request with no Authorization header
        HttpHeaders headers = new HttpHeaders();
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void test_2_InvalidToken_Unauthorized() {
        // Make request with invalid JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer invalid.token.here");
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void test_3_MalformedToken_Unauthorized() {
        // Make request with malformed JWT token
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer malformedtoken");
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void test_4_TokenWithWrongFormat_Unauthorized() {
        // Make request with wrong format - not using "Bearer "
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "JWT " + adminHeaders.getFirst("Authorization").substring(7));
        
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void test_5_InsufficientScope_Forbidden() {
        // Make request with valid token but insufficient scope
        // Student tries to access admin endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(studentHeaders),
                String.class);
        
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
} 