package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@SpringBootTest
public class StudentScheduleControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TermRepository termRepository;

    /**
     * Federico Marquez Murrieta and Shannon Scire actively participated in the development of this test file.
     */

    /**
     * Helper method to ensure a term's enrollment period is open
     */
    private void ensureTermEnrollmentOpen(Term term) {
        // Set dates to allow enrollment today
        term.setAddDate(Date.valueOf(LocalDate.now().minusDays(5)));
        term.setAddDeadline(Date.valueOf(LocalDate.now().plusDays(30)));
        termRepository.save(term);
    }

    /**
     * Helper method to restore a term's original enrollment dates
     */
    private void restoreTermDates(Term term, Date originalAddDate, Date originalAddDeadline) {
        term.setAddDate(originalAddDate);
        term.setAddDeadline(originalAddDeadline);
        termRepository.save(term);
    }

    /**
     * Unit test to enroll into a section
     *
     * Test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
     * The request is successful and the test asserts that the returned status code
     * is 200 (ok) and that the returned EnrollmentDTO data has expected data.
     */
    @Test
    public void enrollInSectionSuccess() throws Exception {
        MockHttpServletResponse response;

        // Get a student from the database (from data.sql)
        User student = userRepository.findByEmail("tedison@csumb.edu");
        assertNotNull(student, "Student not found for testing");
        assertEquals("STUDENT", student.getType());

        // Find a section that the student is not already enrolled in
        // Using section 4 from data.sql (CST363 section 2 in Fall 2024)
        int sectionNo = 4;
        Section section = sectionRepository.findById(sectionNo).orElseThrow();

        // Save original term dates
        Term term = section.getTerm();
        Date originalAddDate = term.getAddDate();
        Date originalAddDeadline = term.getAddDeadline();

        try {
            // Ensure enrollment period is open
            ensureTermEnrollmentOpen(term);

            // Ensure student is not already enrolled in this section
            Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, student.getId());
            if (existingEnrollment != null) {
                enrollmentRepository.delete(existingEnrollment);
            }

            // Attempt to enroll the student in the section
            response = mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/enrollments/sections/" + sectionNo + "?studentId=" + student.getId())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();

            // Check response is OK
            assertEquals(200, response.getStatus());

            // Parse response to EnrollmentDTO
            EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

            // Verify enrollment data
            assertNotEquals(0, result.enrollmentId());
            assertEquals(student.getId(), result.studentId());
            assertEquals(student.getName(), result.name());
            assertEquals(student.getEmail(), result.email());
            assertEquals(sectionNo, result.sectionNo());

            // Clean up - delete the enrollment
            Enrollment enrollment = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
            if (enrollment != null) {
                enrollmentRepository.delete(enrollment);
            }
        } finally {
            // Restore original term dates
            restoreTermDates(term, originalAddDate, originalAddDeadline);
        }
    }

    /**
     * Unit test to enroll that fails due to duplicate course
     *
     * Test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
     * The request is unsuccessful because the student is already enrolled.
     * There are assert statements on the returned status code and error message.
     */
    @Test
    public void enrollDuplicateFailure() throws Exception {
        MockHttpServletResponse response;

        // Get a student from the database
        User student = userRepository.findByEmail("tedison@csumb.edu");
        assertNotNull(student, "Student not found for testing");

        // Use section 9 from data.sql (CST363 section 2 in Spring 2025)
        int sectionNo = 9;
        Section section = sectionRepository.findById(sectionNo).orElseThrow();

        // Save original term dates
        Term term = section.getTerm();
        Date originalAddDate = term.getAddDate();
        Date originalAddDeadline = term.getAddDeadline();

        // First, clean up any existing enrollments for this student in this section
        Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, student.getId());
        if (existingEnrollment != null) {
            enrollmentRepository.delete(existingEnrollment);
        }

        // Manually create and save an enrollment first to set up duplicate condition
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setGrade(null);
        enrollment = enrollmentRepository.save(enrollment);

        try {
            // Ensure enrollment period is open
            ensureTermEnrollmentOpen(term);

            // Now try to enroll the same student in the same section again via API
            response = mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/enrollments/sections/" + sectionNo + "?studentId=" + student.getId())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();

            // Expected to get a 400 Bad Request (with ResponseStatusException)
            assertEquals(400, response.getStatus());

            // Error message should indicate duplicate enrollment
            assertTrue(response.getErrorMessage().contains("Student already enrolled in this section"),
                    "Error message should indicate already enrolled");

        } finally {
            // Clean up - delete the test enrollment
            enrollmentRepository.delete(enrollment);

            // Restore original term dates
            restoreTermDates(term, originalAddDate, originalAddDeadline);
        }
    }

    /**
     * Unit test to enroll that has bad section number
     *
     * Test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
     * The request is unsuccessful due to an invalid section number.
     * The tests contain assert statements for a bad status code and error message.
     */
    @Test
    public void enrollInvalidSectionFailure() throws Exception {
        MockHttpServletResponse response;

        // Get a student from the database (from data.sql)
        User student = userRepository.findByEmail("tedison@csumb.edu");
        assertNotNull(student, "Student not found for testing");

        // Use an invalid section number that doesn't exist
        int invalidSectionNo = 99999;

        // Attempt to enroll the student in the invalid section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + invalidSectionNo + "?studentId=" + student.getId())
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        // Expected to get a 400 Bad Request (with ResponseStatusException)
        assertEquals(400, response.getStatus());

        // Error message should indicate section not found
        assertTrue(response.getErrorMessage().contains("Section not found"),
                "Error message should indicate section not found");
    }

    /**
     * Unit test to enroll into a course that is past add deadline
     *
     * Test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
     * The request is unsuccessful because the date is past the add deadline for the section.
     * The test has assert statements that check for bad status code and error message.
     */
    @Test
    public void enrollPastDeadlineFailure() throws Exception {
        MockHttpServletResponse response;

        // Get a student from the database (from data.sql)
        User student = userRepository.findByEmail("tedison@csumb.edu");
        assertNotNull(student, "Student not found for testing");

        // Find an existing section to use
        // Using section 4 from data.sql (CST363 section 2 in Fall 2024)
        int sectionNo = 4;
        Section section = sectionRepository.findById(sectionNo).orElseThrow();

        // Save original term dates
        Term term = section.getTerm();
        Date originalAddDate = term.getAddDate();
        Date originalAddDeadline = term.getAddDeadline();

        // Ensure student is not already enrolled in this section
        Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, student.getId());
        if (existingEnrollment != null) {
            enrollmentRepository.delete(existingEnrollment);
        }

        try {
            // Set both add date and add deadline to be in the past
            LocalDate pastDate = LocalDate.now().minusDays(30);
            LocalDate pastDeadline = LocalDate.now().minusDays(10);
            term.setAddDate(Date.valueOf(pastDate));
            term.setAddDeadline(Date.valueOf(pastDeadline));
            termRepository.save(term);

            // Attempt to enroll the student in the section
            response = mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/enrollments/sections/" + sectionNo + "?studentId=" + student.getId())
                                    .accept(MediaType.APPLICATION_JSON))
                    .andReturn()
                    .getResponse();

            // Expect 400 Bad Request (with ResponseStatusException)
            assertEquals(400, response.getStatus());

            // Error message should indicate enrollment period is closed
            assertTrue(response.getErrorMessage().contains("Enrollment period is closed"),
                    "Error message should indicate enrollment period is closed");

        } finally {
            // Restore original term dates
            restoreTermDates(term, originalAddDate, originalAddDeadline);
        }
    }

    // Helper methods for JSON conversion
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T fromJsonString(String str, Class<T> valueType) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}