package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
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
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    TermRepository termRepository;

    @Test
    public void addAssignment() throws Exception {
        MockHttpServletResponse response;

        // Setup: create section using helper
        Section section = buildAndSaveTestSection();

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Test Assignment",
                "2025-05-01",
                section.getCourse().getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        );

        // POST request
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .param("instructorEmail", section.getInstructorEmail())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());

        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);
        assertNotEquals(0, result.id());
        assertEquals("Test Assignment", result.title());

        // verify in DB
        Assignment a = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(a);
        assertEquals("Test Assignment", a.getTitle());

        // cleanup
        response = mvc.perform(
                        MockMvcRequestBuilders
                                .delete("/assignments/" + result.id())
                                .param("instructorEmail", section.getInstructorEmail()))
                .andReturn()
                .getResponse();

        assertEquals(200, response.getStatus());
        a = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(a);

        // cleanup section and dependencies
        sectionRepository.delete(section);
        courseRepository.delete(section.getCourse());
        termRepository.delete(section.getTerm());
    }

    @Test
    public void addAssignment_withInvalidDueDate_shouldFail() throws Exception {
        MockHttpServletResponse response;

        // Setup: create section using helper
        Section section = buildAndSaveTestSection();

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Invalid Due Date Assignment",
                "2025-01-05", // invalid: before course start date
                section.getCourse().getCourseId(),
                section.getSecId(),
                section.getSectionNo()
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .param("instructorEmail", section.getInstructorEmail())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        String message = response.getErrorMessage();
        assertEquals("Due date must be within the section's term dates", message);

        // cleanup section and dependencies
        sectionRepository.delete(section);
        courseRepository.delete(section.getCourse());
        termRepository.delete(section.getTerm());
    }

    @Test
    public void addAssignment_withInvalidSection_shouldFail() throws Exception {
        MockHttpServletResponse response;

        // Setup: create valid section and use invalid section number
        Section section = buildAndSaveTestSection();

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Invalid Section Assignment",
                "2025-05-01",
                section.getCourse().getCourseId(),
                section.getSecId(),
                12345 // invalid section number
        );

        response = mvc.perform(
                        MockMvcRequestBuilders
                                .post("/assignments")
                                .param("instructorEmail", section.getInstructorEmail())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(assignmentDTO)))
                .andReturn()
                .getResponse();

        assertEquals(400, response.getStatus());
        String message = response.getErrorMessage();
        assertEquals("Section not found: 12345", message);

        // cleanup section and dependencies
        sectionRepository.delete(section);
        courseRepository.delete(section.getCourse());
        termRepository.delete(section.getTerm());
    }

    private Section buildAndSaveTestSection() {
        Course course = new Course();
        course.setCourseId("CST438");
        course.setTitle("Software Engineering");
        course = courseRepository.save(course);

        Optional<Term> existing = StreamSupport.stream(termRepository.findAll().spliterator(), false)
                .filter(t -> t.getYear() == 2025 &&
                        "Spring".equals(t.getSemester()) &&
                        t.getStartDate().equals(Date.valueOf("2025-01-10")) &&
                        t.getEndDate().equals(Date.valueOf("2025-05-15")))
                .findFirst();

        Term term;
        if (existing.isPresent()) {
            term = existing.get();
        } else {
            term = new Term();
            term.setYear(2025);
            term.setSemester("Spring");
            term.setStartDate(Date.valueOf("2025-01-10"));
            term.setEndDate(Date.valueOf("2025-05-15"));
            term.setAddDate(Date.valueOf("2025-01-12"));
            term.setAddDeadline(Date.valueOf("2025-01-20"));
            term.setDropDeadline(Date.valueOf("2025-02-01"));
            term = termRepository.save(term);
        }

        Section section = new Section();
        section.setCourse(course);
        section.setInstructor_email("jgross@csumb.edu");
        section.setTerm(term);
        section.setSecId(1);
        section.setSectionNo(9999);
        section.setBuilding("104");
        section.setRoom("052");
        section.setTimes("MWF 9:00-9:50");

        return sectionRepository.save(section);
    }

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