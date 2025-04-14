package com.cst438.controller;

import com.cst438.config.TestConfig;
import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class GradeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private TestConfig.RegistrarServiceProxyTest registrarServiceProxy;

    @BeforeEach
    public void setUp() {
        registrarServiceProxy.reset();
        // Clear existing grades (they reference assignments)
        gradeRepository.deleteAll();
        // Clear assignments (they reference sections)
        assignmentRepository.deleteAll();
        // Clear enrollments (they reference sections and users)
        enrollmentRepository.deleteAll();
        // Clear sections (they reference courses and terms)
        sectionRepository.deleteAll();

        // Delete terms with the same year and semester as used in tests
        Term existingTerm = termRepository.findByYearAndSemester(2025, "Fall");
        if (existingTerm != null) {
            termRepository.deleteById(existingTerm.getTermId());
        }
    }

    @Test
    public void testSubmitGrades() throws Exception {
        // Create test data

        // Create term
        Term term = new Term();
        term.setTermId(202501);
        term.setYear(2025);
        term.setSemester("Fall");
        term.setStartDate(Date.valueOf(LocalDate.now().minusDays(30)));
        term.setEndDate(Date.valueOf(LocalDate.now().plusDays(30)));
        term.setAddDate(Date.valueOf(LocalDate.now().minusDays(40)));
        term.setAddDeadline(Date.valueOf(LocalDate.now().minusDays(20)));
        term.setDropDeadline(Date.valueOf(LocalDate.now().plusDays(20)));
        termRepository.save(term);

        // Create course
        Course course = new Course();
        course.setCourseId("CST999");
        course.setTitle("Test Course");
        course.setCredits(3);
        courseRepository.save(course);

        // Create section
        Section section = new Section();
        section.setSecId(1);
        section.setCourse(course);
        section.setTerm(term);
        section.setBuilding("Building1");
        section.setRoom("Room1");
        section.setTimes("MW 10:00-11:50");
        section.setInstructor_email("instructor@example.com");
        sectionRepository.save(section);

        // Create student
        User student = new User();
        student.setId(3);
        student.setName("Test Student");
        student.setEmail("student@example.com");
        student.setType("STUDENT");
        student.setPassword("password123");
        userRepository.save(student);

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setGrade(null);
        enrollmentRepository.save(enrollment);

        // Create assignment
        Assignment assignment = new Assignment();
        assignment.setTitle("Test Assignment");
        assignment.setDueDate(Date.valueOf(LocalDate.now().plusDays(10)));
        assignment.setSection(section);
        assignmentRepository.save(assignment);

        int assignmentId = assignment.getAssignmentId();

        // First, get grades for the assignment to create grade records
        mvc.perform(get("/assignments/" + assignmentId + "/grades"))
                .andExpect(status().isOk());

        // Retrieve the created grade
        Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignmentId);
        assertNotNull(grade, "Grade should be created");

        // Create GradeDTO with updated score
        GradeDTO gradeDTO = new GradeDTO(
                grade.getGradeId(),
                student.getName(),
                student.getEmail(),
                assignment.getTitle(),
                course.getCourseId(),
                section.getSectionNo(),
                95
        );

        List<GradeDTO> grades = new ArrayList<>();
        grades.add(gradeDTO);

        // Test the endpoint to update grades
        mvc.perform(put("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(grades)))
                .andExpect(status().isOk());

        // Verify grade was saved
        Grade savedGrade = gradeRepository.findById(grade.getGradeId()).orElse(null);
        assertNotNull(savedGrade, "Updated grade should exist");
        assertEquals(95, savedGrade.getScore(), "Score should be updated to 95");
    }

    @Test
    public void testGetAssignmentGrades() throws Exception {
        // Create test data

        // Create term
        Term term = new Term();
        term.setTermId(202501);
        term.setYear(2025);
        term.setSemester("Fall");
        term.setStartDate(Date.valueOf(LocalDate.now().minusDays(30)));
        term.setEndDate(Date.valueOf(LocalDate.now().plusDays(30)));
        term.setAddDate(Date.valueOf(LocalDate.now().minusDays(40)));
        term.setAddDeadline(Date.valueOf(LocalDate.now().minusDays(20)));
        term.setDropDeadline(Date.valueOf(LocalDate.now().plusDays(20)));
        termRepository.save(term);

        // Create course
        Course course = new Course();
        course.setCourseId("CST999");
        course.setTitle("Test Course");
        course.setCredits(3);
        courseRepository.save(course);

        // Create section
        Section section = new Section();
        section.setSecId(1);
        section.setCourse(course);
        section.setTerm(term);
        section.setBuilding("Building1");
        section.setRoom("Room1");
        section.setTimes("MW 10:00-11:50");
        section.setInstructor_email("instructor@example.com");
        sectionRepository.save(section);

        // Create student
        User student = new User();
        student.setId(3);
        student.setName("Test Student");
        student.setEmail("student@example.com");
        student.setType("STUDENT");
        student.setPassword("password123");
        userRepository.save(student);

        // Create enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setGrade(null);
        enrollmentRepository.save(enrollment);

        // Create assignment
        Assignment assignment = new Assignment();
        assignment.setTitle("Test Assignment");
        assignment.setDueDate(Date.valueOf(LocalDate.now().plusDays(10)));
        assignment.setSection(section);
        assignmentRepository.save(assignment);

        int assignmentId = assignment.getAssignmentId();

        // Create grade
        Grade grade = new Grade();
        grade.setAssignment(assignment);
        grade.setEnrollment(enrollment);
        grade.setScore(95);
        gradeRepository.save(grade);

        // Test the endpoint
        mvc.perform(get("/assignments/" + assignmentId + "/grades"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].studentName").value("Test Student"))
                .andExpect(jsonPath("$[0].score").value(95));
    }

    // Helper method to convert object to JSON string
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}