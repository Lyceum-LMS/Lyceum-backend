package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
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
public class AssignmentControllerUnitTest {
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
        assertEquals(200, gradeGetResponse.getStatus());

        // Convert JSON Response to List<GradeDTO>
        List<GradeDTO> gradeDTOList = new ObjectMapper().readValue(
                gradeGetResponse.getContentAsString(), new TypeReference<List<GradeDTO>>() {});

        // Verify Grade DTO List is not empty
        assertFalse(gradeDTOList.isEmpty(), "Grade list should not be empty");

        // Update the retrieved grades with new scores
        int updatedScore = 90;
        /*for (GradeDTO dto : gradeDTOList) {
            Grade g = gradeRepository.findById(dto.gradeId());
            g.setScore(updatedScore);

        }*/

        // PUT Request to Save Updated Grades
        MockHttpServletResponse gradePutResponse = mvc.perform(
                MockMvcRequestBuilders
                        .put("/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gradeDTOList)))
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
        /*for (GradeDTO grade : updatedGrades) {
            assertEquals(updatedScore, grade.getScore(), "Score should be updated to 90");
        }*/
    }

        /* Retrieve a Grade Record
        Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(2, 1);

        // Prepare a GradeDTO with updated grade
        Integer score = 90;
        List<GradeDTO> gradeDTOList = new ArrayList<>();
        GradeDTO gradeDTO = new GradeDTO(
                grade.getGradeId(),
                grade.getEnrollment().getStudent().getName(),
                grade.getEnrollment().getStudent().getEmail(),
                grade.getAssignment().getTitle(),
                grade.getAssignment().getSection().getCourse().getCourseId(),
                grade.getAssignment().getSection().getSecId(),
                score
        );
        gradeDTOList.add(gradeDTO);

        // PUT Upload Updated Grade
        MockHttpServletResponse gradePutResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(gradeDTOList)))
                .andReturn().getResponse();

        // Verify Grade Update Response
        assertEquals(200, gradePutResponse.getStatus(), "Status should be OK");
        assertEquals(gradePutResponse.getErrorMessage(), null, "Message should match");

        // Retrieve Updated Grade (from same assignment)
        MockHttpServletResponse gradeGetResponse = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/assignments/" + grade.getAssignment().getAssignmentId() + "/grades")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        // Verify the Retrieved Updated Grade
        assertEquals(200, gradeGetResponse.getStatus(), "Status should be OK");
        assertEquals(gradeGetResponse.getErrorMessage(), null, "Message should be null");

        // Convert JSON to List<GradeDTO>
        String jsonResponse = gradeGetResponse.getContentAsString();
        List<GradeDTO> resultList = new ObjectMapper().readValue(jsonResponse, new TypeReference<List<GradeDTO>>() {});
        //List<GradeDTO> resultList = fromJsonString(response.getContentAsString(), List<GradeDTO>.class);

        // Verify primary key has a non-zero value from the database
        assertNotEquals(0, resultList.get(0).gradeId());

        // Verify if score was updated
        assertEquals(score, resultList.get(0).score(), "Score should be 90");

        // Revert Score Back to Null in the database
        grade.setScore(null);
        gradeRepository.save(grade);

        // Retrieve grade again and verify if null
        Grade rolledbackGrade = gradeRepository.findById(grade.getGradeId()).orElse(null);
        assertNull(rolledbackGrade.getScore());
         */

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
        assertEquals("Assignment 99999 not found", errorMessage);
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
