package com.cst438.controller;

import com.cst438.dto.GradeDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class GradeController {

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

        // TODO remove the following line when done

        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        // hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.


        return null;
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

        // TODO

        // for each grade in the GradeDTO list, retrieve the grade entity
        // update the score and save the entity

    }

}
