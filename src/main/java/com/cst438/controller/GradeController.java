package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

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

    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    /**
     instructor lists the grades for an assignment for all enrolled students
     returns the list of grades (ordered by student name) for the assignment
     if there is no grade entity for an enrolled student, a grade entity with null grade is created
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {
        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        // hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found."));

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
            // Build the GradeDTO using grade data
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
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {
        // for each grade in the GradeDTO list, retrieve the grade entity
        // update the score and save the entity
        for (GradeDTO dto : dlist) {
            Grade grade = gradeRepository.findById(dto.gradeId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found for id: " + dto.gradeId()));
            grade.setScore(dto.score());
            gradeRepository.save(grade);
        }
    }

    /**
     * Delete a single grade
     * This effectively resets a student's grade to "not graded"
     * The grade entity remains in the database but with a null score
     */
    @DeleteMapping("/grades/{gradeId}")
    public void deleteGrade(
            @PathVariable("gradeId") int gradeId,
            @RequestParam("instructorEmail") String instructorEmail) {

        // Retrieve the grade
        Grade grade = gradeRepository.findById(gradeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Grade not found with ID: " + gradeId));

        // Verify that the instructor is authorized for this grade's section
        if (!grade.getAssignment().getSection().getInstructorEmail().equals(instructorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "You are not the instructor for this section");
        }

        // Set the score to null (considered "not graded")
        grade.setScore(null);
        gradeRepository.save(grade);
    }
}