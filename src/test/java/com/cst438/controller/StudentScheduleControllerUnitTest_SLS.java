package com.cst438.controller;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.domain.SectionRepository;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/*
 * example of unit test to add a section to an existing course
 */

@AutoConfigureMockMvc
@SpringBootTest
public class StudentScheduleControllerUnitTest_SLS {

    @Autowired
    MockMvc mvc;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void enrollCourse() throws Exception {

        /*
            Unit test to enroll into a section
            Unit test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
            The request is successful and the test asserts that the returned status code is 200 (ok)
            and that the returned EnrollmentDTO data has expected data.
         */

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
    public void enrollCourseFailsDuplicateCourse() throws Exception {
        /*
            Unit test to enroll that fails due to duplicate course
            Unit test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
            The request is unsuccessful because the student is already enrolled.
            There are assert statements on the returned status code and error message.
         */
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
        // STATUS: 400, message: "Student already enrolled in this section"
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + enrollment.sectionNo() + "?studentId=" + enrollment.studentId())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for 400 meaning BAD_REQUEST
        assertEquals(400, response.getStatus());
        assertTrue(Objects.requireNonNull(response.getErrorMessage()).contains("Student already enrolled in this section"));

    } // enrollCourseFailsDuplicateCourse()

    @Test
    public void  enrollCourseFailsBadSecNo() throws Exception {
        /*
            Unit test to enroll that has bad section number
            Unit test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
            The request is unsuccessful due to an invalid section number.
            The tests contain assert statements for a bad status code and error message.
         */
        MockHttpServletResponse response;

        // Postman GET http://localhost:8080/enrollments?year=2025&semester=Spring&studentId=3
        EnrollmentDTO enrollment = new EnrollmentDTO(
                3,null,3,"thomas edison","tedison@csumb.edu",
                "cst438", "Software Engineering", 1, 99,"052","222",
                "T Th 12:00-1:50", 4, 2025,"Spring"

        );

        // sectionNo:99, fails
        // returns jakarta.servlet.ServletException: Request processing failed: java.lang.RuntimeException:
        // Bad secNo
        // POST http://localhost:8080/enrollments/sections/10?studentId=3
        // STATUS: 400, message: "Section not found"
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + enrollment.sectionNo() + "?studentId=" + enrollment.studentId())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for 400 meaning BAD_REQUEST
        assertEquals(400, response.getStatus());
        assertTrue(Objects.requireNonNull(response.getErrorMessage()).contains("Section not found"));

    } // enrollCourseFailsBadSecNo()

    @Test
    public void  enrollCourseFailsPastDeadline() throws Exception {
        /*
            Unit test to enroll into a course that is past add deadline
            Unit test invokes REST api POST /enrollments/sections/{sectionNo}?studentId={id}.
            The request is unsuccessful because the date is past the add deadline for the section.
            The test has assert statements that check for bad status code and error message.
         */
        MockHttpServletResponse response;

        // Postman GET http://localhost:8080/enrollments?year=2025&semester=Spring&studentId=3
        EnrollmentDTO enrollment = new EnrollmentDTO(
                0,null,3,"thomas edison","tedison@csumb.edu",
                "cst438", "Software Engineering", 1, 5,"052","222",
                "T Th 12:00-1:50", 4, 2025,"Spring"

        );

        // sectionNo:5, fails
        // returns jakarta.servlet.ServletException: Request processing failed: java.lang.RuntimeException:
        // Bad secNo
        // POST http://localhost:8080/enrollments/sections/5?studentId=3
        // STATUS: 400, message: "Enrollment period is closed for this section"
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/enrollments/sections/" + enrollment.sectionNo() + "?studentId=" + enrollment.studentId())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollment)))
                .andReturn()
                .getResponse();

        // check the response code for 400 meaning BAD_REQUEST
        assertEquals(400, response.getStatus());
        assertTrue(Objects.requireNonNull(response.getErrorMessage()).contains("Enrollment period is closed for this section"));

    } // enrollCourseFailsPastDeadline()

    @Test
    public void  enrollCourseUpdateGrade() throws Exception {
        /*
            Unit test to enroll into a course that is past add deadline
            Unit test invokes REST api GET for the url /sections/{sectionNo}/enrollments.
            Update the returned list of EnrollmentDTO objects with grades and the invokes
            PUT /enrollments with a body containing the updates EnrollmentDTO objects.
            The request is successful, and the test contains asserts for the status code.
         */
        MockHttpServletResponse response;

        // Postman GET http://localhost:8080/sections/8/enrollments
        EnrollmentDTO enrollment1 = new EnrollmentDTO(
                2,null,3,"thomas edison","tedison@csumb.edu",
                "cst363", "Introduction to Database", 1, 8,"052","104",
                "M W 10:00-11:50", 4,2025,"Spring"

        );

        EnrollmentDTO enrollment2 = new EnrollmentDTO(
                5, "C", 6, "bart simpson", "bsimpson@csumb.edu",
                "cst363", "Introduction to Database", 1, 8,"052","104",
                "M W 10:00-11:50", 4,2025,"Spring"
        );

        EnrollmentDTO enrollment3 = new EnrollmentDTO(
                4, "A", 5, "lisa simpson", "lsimpson@csumb.edu",
                "cst363", "Introduction to Database", 1, 8,"052","104",
                "M W 10:00-11:50", 4,2025,"Spring"
        );

        List<EnrollmentDTO> enrollments = null;

        // invoke GET http://localhost:8080/sections/8/enrollments
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/sections/" + enrollment1.sectionNo() + "/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollments)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        List<EnrollmentDTO> result = fromJsonListString(response.getContentAsString(), EnrollmentDTO.class);
        assertFalse(result.isEmpty());
        assertEquals("cst363", result.get(0).courseId());

        List<EnrollmentDTO> updated = result.stream()
                .map(e -> {
                    String newGrade = switch (e.email()) {
                        case "tedison@csumb.edu" -> "";
                        case "lsimpson@csumb.edu" -> "A";
                        case "bsimpson@csumb.edu" -> "B";
                        default -> e.grade(); // keep the same
                    };
                    return new EnrollmentDTO(
                            e.enrollmentId(), newGrade, e.studentId(), e.name(), e.email(),
                            e.courseId(), e.title(), e.sectionId(), e.sectionNo(), e.building(), e.room(),
                            e.times(), e.credits(), e.year(), e.semester()
                    );
                })
                .toList();

        // invoke PUT http://localhost:8080/enrollments
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .put("/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(updated)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // verify records have been updated
        // invoke GET http://localhost:8080/sections/8/enrollments
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .get("/sections/" + enrollment1.sectionNo() + "/enrollments")
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(enrollments)))
                .andReturn()
                .getResponse();

        // check the response code for 200 meaning OK
        assertEquals(200, response.getStatus());

        // return data converted from String to DTO
        result = fromJsonListString(response.getContentAsString(), EnrollmentDTO.class);
        assertFalse(result.isEmpty());
        assertEquals("bart simpson", result.get(0).name());
        assertEquals("lisa simpson", result.get(1).name());
        assertEquals("thomas edison", result.get(2).name());
        assertEquals("B", result.get(0).grade());
        assertEquals("A", result.get(1).grade());;
        assertEquals("", result.get(2).grade());

    } // enrollCourseUpdateGrade()

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

    public static <T> List<T> fromJsonListString(String json, Class<T> clazz) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
        return mapper.readValue(json, type);
    }


} // StudentScheduleControllerUnitTest_SLS
