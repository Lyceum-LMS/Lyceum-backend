package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.SectionRepository;
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

import static org.junit.jupiter.api.Assertions.*;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class StudentScheduleControllerEnrollSectionUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void enrollCourse() throws Exception {

        MockHttpServletResponse response;

        // create DTO with the data for enrolling in a course.
        // the primary key, enrollmentId, is set to 0. it will be
        // set by the database when the section is inserted.
        // grade will be null until instructor enters grade.
        EnrollmentDTO enrollment = new EnrollmentDTO(
                0,null,3,"thomas edison","tedison@csumb.edu",
                "cst338","Software Design",1,6,"052",
                "100","M W 10:00-11:50",4,2025,"Spring"

        );
        // DTO returned from Postman for POST http://localhost:8080/enrollments/sections/6?studentId=3
        EnrollmentDTO enrollmentPostman = new EnrollmentDTO(
                10005, null, 3, "thomas edison", "tedison@csumb.edu",
                "cst338", "Software Design", 1, 6, "052", "100",
                "M W 10:00-11:50", 4, 2025, "Spring"
        );


        // issue a http POST request to SpringTestServer
        // specify MediaType for request and response data
        // convert section to String data and set as request content
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + enrollment.sectionNo() + "?studentId=" + enrollment.studentId())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);

        // primary key should have a non zero value from the database
        assertNotEquals(0, result.enrollmentId());

        // check other fields of the DTO for expected values
        assertEquals("cst338", result.courseId());

        // check the database
        Enrollment e = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNotNull(e);
        assertEquals("cst338", e.getSection().getCourse().getCourseId());

        // clean up after test. issue http DELETE request for section
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/enrollments/"+result.enrollmentId()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        // check database for delete
        e = enrollmentRepository.findById(result.enrollmentId()).orElse(null);
        assertNull(e);  // enrollment should not be found after delete
    } // enrollCourse

    @Test
    public void enrollCourseFailsAlreadyEnrolled() throws Exception {
        MockHttpServletResponse response;

        // Postman GET http://localhost:8080/enrollments?year=2025&semester=Spring&studentId=3
        EnrollmentDTO enrollment = new EnrollmentDTO(
                3,null,3,"thomas edison","tedison@csumb.edu",
                "cst438", "Software Engineering", 1, 10,"052","222",
                "T Th 12:00-1:50", 4, 2025,"Spring"

        );

        // sectionNo: 10, fails
        // returns jakarta.servlet.ServletException: Request processing failed: java.lang.RuntimeException:
        // Student already enrolled in this section
        // POST http://localhost:8080/enrollments/sections/10?studentId=3
        // STATUS: 500, message: "Student already enrolled in this section"

        // solution #1
        Exception exception = assertThrows(Exception.class, () -> {
            mvc.perform(MockMvcRequestBuilders
                            .post("/enrollments/sections/" + enrollment.sectionNo() + "?studentId=" + enrollment.studentId())
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(asJsonString(enrollment)))
                    .andReturn()
                    .getResponse();
        });

        String message = exception.getCause().getMessage(); // unwraps RuntimeException
        assertTrue(message.contains("Student already enrolled in this section"));

        // solution #2
        try{
            response = mvc.perform(
                            MockMvcRequestBuilders
                                    .post("/enrollments/sections/" + enrollment.sectionNo() + "?studentId=" + enrollment.studentId())
                                    .accept(MediaType.APPLICATION_JSON)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(asJsonString(enrollment)))
                    .andReturn()
                    .getResponse();
        }  catch (Exception ex) {
            Throwable root = ex.getCause();
            // check the response code for 500 meaning ERROR
            // "Student already enrolled in this section"
            assertNotNull(root);
            assertTrue(root.getMessage().contains("Student already enrolled in this section"));
        }
    } // enrollCourseFailsAlreadyEnrolled()

//    @Test
//    public void addSectionFailsBadCourse( ) throws Exception {
//
//        MockHttpServletResponse response;
//
//        // course id cst599 does not exist.
//        SectionDTO section = new SectionDTO(
//                0,
//                2024,
//                "Spring",
//                "cst599",
//                "",
//                1,
//                "052",
//                "104",
//                "W F 1:00-2:50 pm",
//                "Joshua Gross",
//                "jgross@csumb.edu"
//        );
//
//        // issue the POST request
//        response = mvc.perform(
//                        MockMvcRequestBuilders
//                                .post("/sections")
//                                .accept(MediaType.APPLICATION_JSON)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(asJsonString(section)))
//                .andReturn()
//                .getResponse();
//
//        // response should be 404, the course cst599 is not found
//        assertEquals(404, response.getStatus());
//
//        // check the expected error message
//        String message = response.getErrorMessage();
//        assertEquals("course not found cst599", message);
//
//    } // addSectionFailsBadCourse

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
