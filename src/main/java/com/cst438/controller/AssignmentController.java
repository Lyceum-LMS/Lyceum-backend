package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    GradeRepository gradeRepository;

    /**
     instructor lists assignments for a section.
     Assignment data is returned ordered by due date.
     logged in user must be the instructor for the section (assignment 7)
     */
    // Get assignments for a section
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo, // Section number
            @RequestParam("instructorEmail") String instructorEmail) { // Add instructorEmail parameter
        // Fetch the section
        Section section = sectionRepository.findById(secNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        // Validate that the logged-in user is the instructor for the section
        if (!section.getInstructorEmail().equals(instructorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the instructor for this section");
        }

        // Fetch assignments for the section, ordered by due date
        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        List<AssignmentDTO> assignmentDTOs = new ArrayList<>();

        for (Assignment a : assignments) {
            assignmentDTOs.add(new AssignmentDTO(
                    a.getAssignmentId(),
                    a.getTitle(),
                    a.getDueDate().toString(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    a.getSection().getSectionNo()
            ));
        }

        return assignmentDTOs;
    }

    /**
     instructor creates an assignment for a section.
     Assignment data with primary key is returned.
     logged in user must be the instructor for the section (assignment 7)
     */
    // Create an assignment
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto,
            @RequestParam("instructorEmail") String instructorEmail) { // Add instructorEmail parameter
        // Get the section
        Section section = sectionRepository.findById(dto.secNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Section not found: " + dto.secNo()));

        // Validate that the logged-in user is the instructor for the section
        if (!section.getInstructorEmail().equals(instructorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the instructor for this section");
        }

        // Create the assignment
        Assignment assignment = new Assignment();
        assignment.setTitle(dto.title());

        // Convert string date to Date object
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dueDate = new Date(dateFormat.parse(dto.dueDate()).getTime());
            assignment.setDueDate(dueDate);
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid date format. Use yyyy-MM-dd");
        }

        assignment.setSection(section);

        // Validate due date is within term dates
        if (assignment.getDueDate().before(section.getTerm().getStartDate()) ||
                assignment.getDueDate().after(section.getTerm().getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Due date must be within the section's term dates");
        }

        // Save the assignment
        assignment = assignmentRepository.save(assignment);

        // Return the DTO
        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate().toString(),
                assignment.getSection().getCourse().getCourseId(),
                assignment.getSection().getSecId(),
                assignment.getSection().getSectionNo()
        );
    }

    /**
     instructor updates an assignment for a section.
     only title and dueDate may be changed
     updated assignment data is returned
     logged in user must be the instructor for the section (assignment 7)
     */
    // Update an assignment
    @PutMapping("/assignments/{id}")
    public AssignmentDTO updateAssignment(
            @PathVariable("id") int assignmentId,
            @RequestBody AssignmentDTO dto,
            @RequestParam("instructorEmail") String instructorEmail) { // Add instructorEmail parameter
        // Get the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Assignment not found: " + assignmentId));

        // Validate that the logged-in user is the instructor for the section
        if (!assignment.getSection().getInstructorEmail().equals(instructorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the instructor for this section");
        }

        // Update the assignment
        assignment.setTitle(dto.title());

        // Convert string date to Date object
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Date dueDate = new Date(dateFormat.parse(dto.dueDate()).getTime());
            assignment.setDueDate(dueDate);
        } catch (ParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid date format. Use yyyy-MM-dd");
        }

        // Validate due date is within term dates
        if (assignment.getDueDate().before(assignment.getSection().getTerm().getStartDate()) ||
                assignment.getDueDate().after(assignment.getSection().getTerm().getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Due date must be within the section's term dates");
        }

        // Save the assignment
        assignment = assignmentRepository.save(assignment);

        // Return the DTO
        return new AssignmentDTO(
                assignment.getAssignmentId(),
                assignment.getTitle(),
                assignment.getDueDate().toString(),
                assignment.getSection().getCourse().getCourseId(),
                assignment.getSection().getSecId(),
                assignment.getSection().getSectionNo()
        );
    }

    /**
     instructor deletes an assignment for a section.
     logged in user must be the instructor for the section (assignment 7)
     */
    @DeleteMapping("/assignments/{id}")
    public void deleteAssignment(
            @PathVariable("id") int assignmentId,
            @RequestParam("instructorEmail") String instructorEmail) { // Add instructorEmail parameter
        // Get the assignment
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Assignment not found: " + assignmentId));

        // Validate that the logged-in user is the instructor for the section
        if (!assignment.getSection().getInstructorEmail().equals(instructorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the instructor for this section");
        }

        // Check for existing grades before deletion to maintain referential integrity
        // This explicit check is necessary due to the foreign key constraint in the database schema
        // where grade.assignment_id references assignment(assignment_id) with implicit ON DELETE RESTRICT
        // This prevents orphaned records and provides a more user-friendly error message than a raw SQL constraint violation
        List<Grade> existingGrades = assignment.getGrades();
        if (existingGrades != null && !existingGrades.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot delete assignment with ID " + assignmentId + " because it has " +
                            existingGrades.size() + " associated grades. Delete the grades first to maintain referential integrity.");
        }

        // Delete the assignment now that we've verified no grades reference it
        assignmentRepository.delete(assignment);
    }
}