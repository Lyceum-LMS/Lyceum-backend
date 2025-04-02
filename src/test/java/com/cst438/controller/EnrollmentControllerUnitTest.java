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
public class EnrollmentControllerUnitTest {

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
     * Unit test to update enrollment grade
     *
     * Test invokes REST api GET for the url /sections/{sectionNo}/enrollments.
     * Update the returned list of EnrollmentDTO objects with grades and then
     * invokes PUT /enrollments with a body containing the updated EnrollmentDTO objects.
     * The request is successful, and the test contains asserts for the status code.
     */
    @Test
    public void updateFinalGradesSuccess() throws Exception {
        MockHttpServletResponse response;

        // Find a section with enrollments (section 1 from data.sql)
        int sectionNo = 1;

        // Get the list of enrollments for this section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/sections/" + sectionNo + "/enrollments")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // Parse response to array of EnrollmentDTO
        EnrollmentDTO[] enrollments = fromJsonString(response.getContentAsString(), EnrollmentDTO[].class);
        assertNotEquals(0, enrollments.length, "No enrollments found for testing");

        // Update the grade for each enrollment
        for (int i = 0; i < enrollments.length; i++) {
            // Set different grades for each student
            enrollments[i] = new EnrollmentDTO(
                    enrollments[i].enrollmentId(),
                    (i == 0) ? "A" : (i == 1) ? "B" : "C",
                    enrollments[i].studentId(),
                    enrollments[i].name(),
                    enrollments[i].email(),
                    enrollments[i].courseId(),
                    enrollments[i].title(),
                    enrollments[i].sectionId(),
                    enrollments[i].sectionNo(),
                    enrollments[i].building(),
                    enrollments[i].room(),
                    enrollments[i].times(),
                    enrollments[i].credits(),
                    enrollments[i].year(),
                    enrollments[i].semester()
            );
        }

        // Submit the updated enrollment grades
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollments)))
                .andReturn()
                .getResponse();

        // Check response is OK
        assertEquals(200, response.getStatus());

        // Verify grades were saved
        for (EnrollmentDTO enrollmentDTO : enrollments) {
            Enrollment enrollment = enrollmentRepository.findById(enrollmentDTO.enrollmentId()).orElse(null);
            assertNotNull(enrollment);
            assertEquals(enrollmentDTO.grade(), enrollment.getGrade());

            // Reset the grade to original value
            enrollment.setGrade(null);
            enrollmentRepository.save(enrollment);
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