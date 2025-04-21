package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class GradeController {
    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    /**
     instructor lists the grades for an assignment for all enrolled students
     returns the list of grades (ordered by student name) for the assignment
     if there is no grade entity for an enrolled student, a grade entity with null grade is created
     logged in user must be the instructor for the section (assignment 7)
     */
    // a8 sls
    @GetMapping("/assignments/{assignmentId}/grades")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<GradeDTO> getAssignmentGrades(
            @PathVariable("assignmentId") int assignmentId,
            Principal principal) {

        String instructorEmail = principal.getName();

        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found."));

        // validate assignment belongs to Instructor
        if (!assignment.getSection().getInstructorEmail().equals(instructorEmail)) {
            System.out.println("Invalid Instructor for assignment");
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not the instructor for this assignment's section");
        }

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(assignment.getSection().getSectionNo());

        List<GradeDTO> gradeDTOList = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignmentId);
            if (grade == null) {
                grade = new Grade();
                grade.setEnrollment(enrollment);
                grade.setAssignment(assignment);
                grade = gradeRepository.save(grade);
            }
            GradeDTO dto = new GradeDTO(
                    grade.getGradeId(),
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    assignment.getTitle(),
                    assignment.getSection().getCourse().getCourseId(),
                    assignment.getSection().getSectionNo(),
                    grade.getScore()
            );
            gradeDTOList.add(dto);
        }

        return gradeDTOList;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    /**
     instructor updates one or more assignment grades
     only the score attribute of grade entity can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    // a8 sls
    @PutMapping("/grades")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateGrades(
            @RequestBody List<GradeDTO> dlist,
            Principal principal) {

        String instructorEmail = principal.getName();

        // Since list is passed to the method and not generated inside this method,
        // validate each assignment belongs to logged in Instructor
        for (GradeDTO dto : dlist) {
            Grade grade = gradeRepository.findById(dto.gradeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found for id: " + dto.gradeId()));

            // verify grade belongs to logged in Instructor
            Assignment assignment = grade.getAssignment();

            if (!assignment.getSection().getInstructorEmail().equals(instructorEmail)){
                System.out.println("Invalid Instructor for assignment");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Instructor for assignment");
            }

            grade.setScore(dto.score());
            gradeRepository.save(grade);
        }
    }

}