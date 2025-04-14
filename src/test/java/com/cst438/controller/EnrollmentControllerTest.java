package com.cst438.controller;

import com.cst438.config.TestConfig;
import com.cst438.config.TestConfig.RegistrarServiceProxyTest;
import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@Transactional
public class EnrollmentControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private TermRepository termRepository;

    @Autowired
    private RegistrarServiceProxyTest registrarServiceProxy;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private AssignmentRepository assignmentRepository;

    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void setUp() {
        registrarServiceProxy.reset();
        // Clear grades first (they reference enrollments and assignments)
        gradeRepository.deleteAll();
        // Clear assignments (they reference sections)
        assignmentRepository.deleteAll();
        // Clear enrollments (they reference sections and users)
        enrollmentRepository.deleteAll();
        // Clear sections (they reference courses and terms)
        sectionRepository.deleteAll();
        
        // Delete terms with the same year and semester as used in tests
        Term existingSpringTerm = termRepository.findByYearAndSemester(2025, "Spring");
        if (existingSpringTerm != null) {
            termRepository.deleteById(existingSpringTerm.getTermId());
        }
    }

    @Test
    public void testGetEnrollments() throws Exception {
        // Create test data - term
        Term term = new Term();
        term.setTermId(202501);
        term.setYear(2025);
        term.setSemester("Spring");
        term.setStartDate(Date.valueOf("2025-01-20"));
        term.setEndDate(Date.valueOf("2025-05-15"));
        term.setAddDate(Date.valueOf("2024-12-01"));
        term.setAddDeadline(Date.valueOf("2025-02-01"));
        term.setDropDeadline(Date.valueOf("2025-03-15"));
        termRepository.save(term);
        
        // Create test data - course
        Course course = new Course();
        course.setCourseId("CST438");
        course.setTitle("Software Engineering");
        course.setCredits(3);
        courseRepository.save(course);
        
        // Create test data - section
        Section section = new Section();
        section.setSecId(1);
        section.setCourse(course);
        section.setTerm(term);
        section.setBuilding("Building1");
        section.setRoom("Room1");
        section.setTimes("MW 10:00AM-11:50AM");
        section.setInstructor_email("instructor@example.com");
        sectionRepository.save(section);

        // Create test data - user
        User student = new User();
        student.setName("Test Student");
        student.setEmail("student@example.com");
        student.setType("STUDENT");
        student.setPassword("password123");
        userRepository.save(student);

        // Create test data - enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setGrade("A");
        enrollmentRepository.save(enrollment);

        // Test the endpoint
        mvc.perform(get("/sections/" + section.getSectionNo() + "/enrollments"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[0].grade").value("A"))
           .andExpect(jsonPath("$[0].studentId").value(student.getId()))
           .andExpect(jsonPath("$[0].name").value("Test Student"));
    }

    @Test
    public void testUpdateEnrollmentGrade() throws Exception {
        // Create test data
        Term term = new Term();
        term.setTermId(202501);
        term.setYear(2025);
        term.setSemester("Spring");
        term.setStartDate(Date.valueOf("2025-01-20"));
        term.setEndDate(Date.valueOf("2025-05-15"));
        term.setAddDate(Date.valueOf("2024-12-01"));
        term.setAddDeadline(Date.valueOf("2025-02-01"));
        term.setDropDeadline(Date.valueOf("2025-03-15"));
        termRepository.save(term);

        Course course = new Course();
        course.setCourseId("CST438");
        course.setTitle("Software Engineering");
        course.setCredits(3);
        courseRepository.save(course);

        Section section = new Section();
        section.setSecId(1);
        section.setCourse(course);
        section.setTerm(term);
        section.setBuilding("Building1");
        section.setRoom("Room1");
        section.setTimes("MW 10:00AM-11:50AM");
        section.setInstructor_email("instructor@example.com");
        sectionRepository.save(section);

        User student = new User();
        student.setName("Test Student");
        student.setEmail("student@example.com");
        student.setType("STUDENT");
        student.setPassword("password123");
        userRepository.save(student);

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setGrade(null);
        enrollmentRepository.save(enrollment);

        // Create DTO for updating grade
        List<EnrollmentDTO> enrollmentDTOList = new ArrayList<>();
        enrollmentDTOList.add(new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                "B",
                student.getId(),
                student.getName(),
                student.getEmail(),
                course.getCourseId(),
                course.getTitle(),
                section.getSecId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                course.getCredits(),
                term.getYear(),
                term.getSemester()
        ));

        // Test updating grade
        mvc.perform(put("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(enrollmentDTOList)))
                .andExpect(status().isOk());

        // Verify the grade was updated in the database
        Enrollment updatedEnrollment = enrollmentRepository.findById(enrollment.getEnrollmentId()).orElse(null);
        assertNotNull(updatedEnrollment);
        assertEquals("B", updatedEnrollment.getGrade());

        // Verify that a message was sent to the Registrar service
        assertEquals(1, registrarServiceProxy.getMessageCount());
        assertNotNull(registrarServiceProxy.getLastMessageContent());
        assertTrue(registrarServiceProxy.getLastMessageContent().contains("updateEnrollmentGrade"));
    }
    
    @Test
    public void testUpdateMultipleEnrollmentGrades() throws Exception {
        // Create test data
        Term term = new Term();
        term.setTermId(202501);
        term.setYear(2025);
        term.setSemester("Spring");
        term.setStartDate(Date.valueOf("2025-01-20"));
        term.setEndDate(Date.valueOf("2025-05-15"));
        term.setAddDate(Date.valueOf("2024-12-01"));
        term.setAddDeadline(Date.valueOf("2025-02-01"));
        term.setDropDeadline(Date.valueOf("2025-03-15"));
        termRepository.save(term);

        Course course = new Course();
        course.setCourseId("CST438");
        course.setTitle("Software Engineering");
        course.setCredits(3);
        courseRepository.save(course);

        Section section = new Section();
        section.setSecId(1);
        section.setCourse(course);
        section.setTerm(term);
        section.setBuilding("Building1");
        section.setRoom("Room1");
        section.setTimes("MW 10:00AM-11:50AM");
        section.setInstructor_email("instructor@example.com");
        sectionRepository.save(section);

        User student1 = new User();
        student1.setName("Test Student 1");
        student1.setEmail("student1@example.com");
        student1.setType("STUDENT");
        student1.setPassword("password123");
        userRepository.save(student1);
        
        User student2 = new User();
        student2.setName("Test Student 2");
        student2.setEmail("student2@example.com");
        student2.setType("STUDENT");
        student2.setPassword("password123");
        userRepository.save(student2);

        Enrollment enrollment1 = new Enrollment();
        enrollment1.setStudent(student1);
        enrollment1.setSection(section);
        enrollment1.setGrade(null);
        enrollmentRepository.save(enrollment1);
        
        Enrollment enrollment2 = new Enrollment();
        enrollment2.setStudent(student2);
        enrollment2.setSection(section);
        enrollment2.setGrade(null);
        enrollmentRepository.save(enrollment2);

        // Create DTOs for updating grades
        List<EnrollmentDTO> enrollmentDTOList = new ArrayList<>();
        
        enrollmentDTOList.add(new EnrollmentDTO(
                enrollment1.getEnrollmentId(),
                "A",
                student1.getId(),
                student1.getName(),
                student1.getEmail(),
                course.getCourseId(),
                course.getTitle(),
                section.getSecId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                course.getCredits(),
                term.getYear(),
                term.getSemester()
        ));
        
        enrollmentDTOList.add(new EnrollmentDTO(
                enrollment2.getEnrollmentId(),
                "C",
                student2.getId(),
                student2.getName(),
                student2.getEmail(),
                course.getCourseId(),
                course.getTitle(),
                section.getSecId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                course.getCredits(),
                term.getYear(),
                term.getSemester()
        ));

        // Test updating grades
        mvc.perform(put("/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(enrollmentDTOList)))
                .andExpect(status().isOk());

        // Verify the grades were updated in the database
        Enrollment updatedEnrollment1 = enrollmentRepository.findById(enrollment1.getEnrollmentId()).orElse(null);
        Enrollment updatedEnrollment2 = enrollmentRepository.findById(enrollment2.getEnrollmentId()).orElse(null);
        
        assertNotNull(updatedEnrollment1);
        assertNotNull(updatedEnrollment2);
        assertEquals("A", updatedEnrollment1.getGrade());
        assertEquals("C", updatedEnrollment2.getGrade());

        // Verify that messages were sent to the Registrar service
        assertEquals(2, registrarServiceProxy.getMessageCount());
        List<String> messages = registrarServiceProxy.getMessagesSent();
        assertEquals(2, messages.size());
    }
} 