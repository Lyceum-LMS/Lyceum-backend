package com.cst438.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.cst438.dto.CourseDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;

public class AdminSecurityTest extends SecurityBaseTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    public void test_1_AdminViewUsers_Success() {
        // Admin can view all users
        ResponseEntity<List<UserDTO>> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<List<UserDTO>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void test_2_AdminAddUser_Success() {
        // Admin can add a new user
        long timestamp = System.currentTimeMillis();
        UserDTO newUser = new UserDTO(
                0, // ID will be assigned by the server
                "Test User " + timestamp,
                "testuser" + timestamp + "@csumb.edu",
                "STUDENT");

        ResponseEntity<UserDTO> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.POST,
                new HttpEntity<>(newUser, adminHeaders),
                UserDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().id() > 0);
    }

    @Test
    public void test_3_AdminViewCourses_Success() {
        // Admin can view all courses
        ResponseEntity<List<CourseDTO>> response = restTemplate.exchange(
                baseUrl + "/courses",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<List<CourseDTO>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    public void test_4_AdminAddCourse_Success() {
        // Admin can add a new course
        // Use a short course ID (max 10 characters) to avoid database constraint violation
        String courseId = "cst" + (System.currentTimeMillis() % 10000);
        CourseDTO newCourse = new CourseDTO(
                courseId,
                "Test Course",
                3);

        ResponseEntity<CourseDTO> response = restTemplate.exchange(
                baseUrl + "/courses",
                HttpMethod.POST,
                new HttpEntity<>(newCourse, adminHeaders),
                CourseDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(courseId, response.getBody().courseId());
    }

    @Test
    public void test_5_AdminViewSections_Success() {
        // Admin can view sections using the open sections endpoint which is available to admin users
        // From the SectionController, we see @PreAuthorize("hasAnyAuthority('SCOPE_ROLE_ADMIN', 'SCOPE_ROLE_INSTRUCTOR','SCOPE_ROLE_STUDENT')")
        ResponseEntity<List<SectionDTO>> response = restTemplate.exchange(
                baseUrl + "/sections/open",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<List<SectionDTO>>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        // Also try the course-specific endpoint with a course ID
        Map<String, String> params = new HashMap<>();
        params.put("year", "2023");
        params.put("semester", "Fall");

        ResponseEntity<List<SectionDTO>> courseResponse = restTemplate.exchange(
                baseUrl + "/courses/cst363/sections?year={year}&semester={semester}",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                new ParameterizedTypeReference<List<SectionDTO>>() {},
                params);

        assertEquals(HttpStatus.OK, courseResponse.getStatusCode());
    }

    @Test
    public void test_6_AdminAddSection_Success() {
        // Admin can add a new section
        // We'll use an existing course from data.sql
        // The SectionDTO constructor args need to match the actual class definition
        SectionDTO newSection = new SectionDTO(
                0,  // secNo will be assigned by the server
                2024,  // year
                "Spring", // semester
                "cst363", // courseId
                "Introduction to Database", // title
                3, // secId
                "052", // building
                "104", // room
                "M W 4:00-5:50", // times
                "Instructor User", // instructorName
                "instructor@csumb.edu"); // instructorEmail

        ResponseEntity<SectionDTO> response = restTemplate.exchange(
                baseUrl + "/sections",
                HttpMethod.POST,
                new HttpEntity<>(newSection, adminHeaders),
                SectionDTO.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().secNo() > 0);
    }

    @Test
    public void test_7_AdminDeleteUser_Success() {
        // First create a user to delete
        long timestamp = System.currentTimeMillis();
        UserDTO newUser = new UserDTO(
                0,
                "Delete User " + timestamp,
                "deleteuser" + timestamp + "@csumb.edu",
                "STUDENT");

        ResponseEntity<UserDTO> createResponse = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.POST,
                new HttpEntity<>(newUser, adminHeaders),
                UserDTO.class);

        assertEquals(HttpStatus.OK, createResponse.getStatusCode());

        // Now delete the user
        int userId = createResponse.getBody().id();

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                baseUrl + "/users/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(adminHeaders),
                Void.class);

        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
    }

    @Test
    public void test_8_StudentCannotAccessAdminEndpoints_Failure() {
        // Student cannot access admin endpoints
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(studentHeaders),
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void test_9_InstructorCannotAccessAdminEndpoints_Failure() {
        // Instructor cannot access admin endpoints
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/users",
                HttpMethod.GET,
                new HttpEntity<>(instructorHeaders),
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void test_10_AdminCannotAccessStudentEndpoints_Failure() {
        // Admin cannot access student-specific endpoints
        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/transcripts",
                HttpMethod.GET,
                new HttpEntity<>(adminHeaders),
                String.class);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }
} 