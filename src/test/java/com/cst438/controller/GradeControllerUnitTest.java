package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@AutoConfigureMockMvc
@SpringBootTest
public class GradeControllerUnitTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    static final LocalDate today = LocalDate.now();

    Term term;
    Course course;
    Section section;
    @Autowired
    TermRepository termRepository;
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    SectionRepository sectionRepository;

    @BeforeEach
    public void setUp() throws Exception {
        term = termRepository.findByYearAndSemester(2024, "Fall");
        course = courseRepository.findById("cst438").get();
        section = sectionRepository.findByLikeCourseIdAndYearAndSemester("cst438", 2024, "Fall").get(0);
    }

    @Test
    public void gradeAssignment() throws Exception {
        // Assignment ID for the request
        int assignmentId = 1;

        // GET Request to Retrieve Assignment Grades
        MockHttpServletResponse gradeGetResponse = mvc.perform(
                MockMvcRequestBuilders
                        .get("/assignments/" + assignmentId + "/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify GET Response Status
        assertEquals(200, gradeGetResponse.getStatus(), "GET request should return status 200");

        // Convert JSON Response to List<GradeDTO>
        List<GradeDTO> gradeDTOList = new ObjectMapper().readValue(
                gradeGetResponse.getContentAsString(), new TypeReference<List<GradeDTO>>() {});

        // Verify Grade DTO List is not empty
        assertFalse(gradeDTOList.isEmpty(), "Grade list should not be empty");

        // Update the retrieved grades with new scores
        Integer updatedScore = 90;
        List<GradeDTO> updatedGradeDTOList = new ArrayList<>();
        for (GradeDTO dto : gradeDTOList) {
            GradeDTO updatedDTO = new GradeDTO(
                    dto.gradeId(), dto.studentName(), dto.studentEmail(), dto.assignmentTitle(),
                    dto.courseId(), dto.sectionId(), updatedScore
            );
            updatedGradeDTOList.add(updatedDTO);
        }

        // PUT Request to Save Updated Grades
        MockHttpServletResponse gradePutResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedGradeDTOList)))
                .andReturn().getResponse();

        // Verify PUT Response Status
        assertEquals(200, gradePutResponse.getStatus(), "PUT request should return status 200");

        // Optionally, retrieve updated grades again to confirm changes
        MockHttpServletResponse gradeVerifyResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/" + assignmentId + "/grades") // Fetch again
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify GET Response Again
        assertEquals(200, gradeVerifyResponse.getStatus(), "GET request should return status 200 after update");

        // Convert JSON Response to List<GradeDTO> and Check Updates
        List<GradeDTO> updatedGrades = new ObjectMapper().readValue(
                gradeVerifyResponse.getContentAsString(),
                new TypeReference<List<GradeDTO>>() {});

        // Ensure all grades have the updated score
        for (GradeDTO grade : updatedGrades) {
            assertEquals(updatedScore, grade.score(), "Score should be updated to 90");
        }
    }

    @Test
    public void gradeAssignmentInvalidId() throws Exception {

        MockHttpServletResponse response;

        // Invalid assignment ID
        int invalidAssignmentId = 99999;

        // GET request to update grades for an invalid ID
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/" + invalidAssignmentId + "/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(404, response.getStatus(), "Status should be 404");

        // Verify error message
        String errorMessage = response.getErrorMessage();
        assertEquals("Assignment not found.", errorMessage);
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
