package com.cst438.controller;

import com.cst438.dto.EnrollmentDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class StudentScheduleController {

    /**
     students lists their transcript containing all enrollments
     returns list of enrollments in chronological order
     logged in user must be the student (assignment 7)
     example URL  /transcript?studentId=19803
     */
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {

        // TODO

        // list course_id, sec_id, title, credit, grade
        // hint: use enrollment repository method findEnrollmentByStudentIdOrderByTermId
        // remove the following line when done
        return null;
    }


    /**
     students enrolls into a section of a course
     returns the enrollment data including primary key
     logged in user must be the student (assignment 7)
     */
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId ) {

        // TODO

        // check that the Section entity with primary key sectionNo exists
        // check that today is between addDate and addDeadline for the section
        // check that student is not already enrolled into this section
        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.

        // remove the following line when done.
        return null;

    }


    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

        // TODO
        // check that today is not after the dropDeadline for section
    }


}
