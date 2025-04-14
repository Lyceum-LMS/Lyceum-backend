package com.cst438.controller;

import com.cst438.config.TestConfig;
import com.cst438.domain.*;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class AssignmentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AssignmentRepository assignmentRepository;

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

    @Autowired
    private GradeRepository gradeRepository;

    @BeforeEach
    public void setUp() {
        registrarServiceProxy.reset();
        // Clear grades first (they reference assignments)
        gradeRepository.deleteAll();
        // Clear assignments for clean tests
        assignmentRepository.deleteAll();
        // Clear enrollments for clean tests
        enrollmentRepository.deleteAll();
        // Clear sections for clean tests
        sectionRepository.deleteAll();
        
        // Delete terms with the same year and semester as used in tests
        Term existingTerm = termRepository.findByYearAndSemester(2025, "Fall");
        if (existingTerm != null) {
            termRepository.deleteById(existingTerm.getTermId());
        }
    }

    @Test
    public void testGetStudentAssignments() throws Exception {
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
        student.setId(3); // Using ID 3 to match the hardcoded studentId in the frontend
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

        // Test the endpoint
        mvc.perform(get("/assignments?studentId=3&year=2025&semester=Fall"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].title").value("Test Assignment"))
           .andExpect(jsonPath("$[0].courseId").value("CST999"));
    }

    @Test
    public void testGetSectionsForInstructor() throws Exception {
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

        // Test the endpoint
        mvc.perform(get("/sections?instructorEmail=instructor@example.com&year=2025&semester=Fall"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].courseId").value("CST999"))
           .andExpect(jsonPath("$[0].secId").value(1))
           .andExpect(jsonPath("$[0].instructorEmail").value("instructor@example.com"));
    }
    
    @Test
    public void testGetStudentAssignmentsInvalidStudent() throws Exception {
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
        
        // Test with invalid student ID
        mvc.perform(get("/assignments?studentId=99999&year=2025&semester=Fall"))
           .andExpect(status().isNotFound())
           .andExpect(status().reason(containsString("studentId invalid")));
    }
    
    @Test
    public void testGetSectionsForInstructorInvalidTerm() throws Exception {
        // Test with invalid year and semester - for this specific endpoint, 
        mvc.perform(get("/sections?instructorEmail=instructor@example.com&year=9999&semester=Invalid"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$").isArray())
           .andExpect(jsonPath("$").isEmpty());
    }
} 