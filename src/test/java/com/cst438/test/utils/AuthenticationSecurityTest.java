package com.cst438.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cst438.dto.LoginDTO;

public class AuthenticationSecurityTest extends SecurityBaseTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_1_StudentAuthentication_Success() {
        // Create basic auth header for student
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("user@csumb.edu", "user");
        
        // Attempt login
        ResponseEntity<LoginDTO> response = restTemplate.exchange(
                baseUrl + "/login", 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                LoginDTO.class);
        
        // Assert successful login
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().jwt());
        assertEquals("STUDENT", response.getBody().role());
    }
    
    @Test
    public void test_2_InstructorAuthentication_Success() {
        // Create basic auth header for instructor
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("instructor@csumb.edu", "instructor");
        
        // Attempt login
        ResponseEntity<LoginDTO> response = restTemplate.exchange(
                baseUrl + "/login", 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                LoginDTO.class);
        
        // Assert successful login
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().jwt());
        assertEquals("INSTRUCTOR", response.getBody().role());
    }
    
    @Test
    public void test_3_AdminAuthentication_Success() {
        // Create basic auth header for admin
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin@csumb.edu", "admin");
        
        // Attempt login
        ResponseEntity<LoginDTO> response = restTemplate.exchange(
                baseUrl + "/login", 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                LoginDTO.class);
        
        // Assert successful login
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().jwt());
        assertEquals("ADMIN", response.getBody().role());
    }
    
    @Test
    public void test_4_InvalidCredentials_Failure() {
        // Create basic auth header with invalid credentials
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth("admin@csumb.edu", "wrongpassword");
        
        // Attempt login
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/login", 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                String.class);
        
        // Assert failed login
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    
    @Test
    public void test_5_MissingAuthorization_Failure() {
        // Create empty headers (no authorization)
        HttpHeaders headers = new HttpHeaders();
        
        // Attempt to access protected endpoint
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users", 
                HttpMethod.GET, 
                new HttpEntity<>(headers), 
                String.class);
        
        // Assert unauthorized
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
} 