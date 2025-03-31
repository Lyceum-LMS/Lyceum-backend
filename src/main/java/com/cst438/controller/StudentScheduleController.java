package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;   //HP added

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentScheduleController {

    /**
     students lists their transcript containing all enrollments
     returns list of enrollments in chronological order
     logged in user must be the student (assignment 7)
     example URL  /transcript?studentId=19803
     */
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {

        // TO-DO ✅

        // list course_id, sec_id, title, credit, grade
        // hint: use enrollment repository method findEnrollmentByStudentIdOrderByTermId
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);

        // Rubric: studentID not found ✅
        Optional<User> studentOpt = userRepository.findById(studentId);
        if(studentOpt.isEmpty()) {
//            throw new RuntimeException("Student not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found");
        }
        // Rubric: studentID is invalid
        User student = studentOpt.get();
        if(!(student.getType().equals("STUDENT"))){
//            throw new RuntimeException("StudentId argument indicates user is not a student");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "StudentId argument indicates user is not a student");
        }

        // remove the following line when done
        // return null;

        List<EnrollmentDTO> dto_list = new ArrayList<>();
        for (Enrollment e : enrollments) {
            dto_list.add(new EnrollmentDTO(
                    e.getEnrollmentId(),
                    e.getGrade(),
                    e.getStudent().getId(),
                    e.getStudent().getName(),
                    e.getStudent().getEmail(),
                    e.getSection().getCourse().getCourseId(),
                    e.getSection().getCourse().getTitle(),
                    e.getSection().getSecId(),
                    e.getSection().getSectionNo(),
                    e.getSection().getBuilding(),
                    e.getSection().getRoom(),
                    e.getSection().getTimes(),
                    e.getSection().getCourse().getCredits(),
                    e.getSection().getTerm().getYear(),
                    e.getSection().getTerm().getSemester()
            ));
        }
        return dto_list;
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

        // TO-DO ✅
        Optional<Section> sectionOpt = sectionRepository.findById(sectionNo);

        // check that the Section entity with primary key sectionNo exists
        // Rubric: sectionNo is not found when adding a course ✅
        if (sectionOpt.isEmpty()) {
//            throw new RuntimeException("Section not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Section not found");
        }
        Section section = sectionOpt.get();
        // check that today is between addDate and addDeadline for the section
        // Rubric: adding a course before the addDate or after the addDeadline ✅
        if (!(java.time.LocalDate.now().isAfter(section.getTerm().getAddDate().toLocalDate()) &&
                java.time.LocalDate.now().isBefore(section.getTerm().getAddDeadline().toLocalDate()))) {
//            throw new RuntimeException("Enrollment period is closed for this section");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enrollment period is closed for this section");
        }
        // check that student is not already enrolled into this section
        if (enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId) != null) {
//            throw new RuntimeException("Student already enrolled in this section");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student already enrolled in this section");
        }

        // Rubric: studentID not found ✅
        Optional<User> studentOpt = userRepository.findById(studentId);
        if(studentOpt.isEmpty()) {
//            throw new RuntimeException("Student not found");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found");
        }
        // check if studentID is not a student
        User student = studentOpt.get();
        if(!(student.getType().equals("STUDENT"))) {
//            throw new RuntimeException("StudentId argument indicates user is not a student");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "StudentId argument indicates user is not a student");
        }

        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollment.setGrade(null);
        enrollmentRepository.save(enrollment);

        // remove the following line when done.
        // return null;

        return new EnrollmentDTO(
                enrollment.getEnrollmentId(),
                enrollment.getGrade(),
                student.getId(),
                student.getName(),
                student.getEmail(),
                section.getCourse().getCourseId(),
                section.getCourse().getTitle(),
                section.getSecId(),
                section.getSectionNo(),
                section.getBuilding(),
                section.getRoom(),
                section.getTimes(),
                section.getCourse().getCredits(),
                section.getTerm().getYear(),
                section.getTerm().getSemester()
        );
    }

    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

        // TO-DO ✅

        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        // Rubric: dropping an invalid enrollmentId ✅
        if (enrollmentOpt.isEmpty()) {
            throw new RuntimeException("Enrollment ID invalid");
        }
        Enrollment enrollment = enrollmentOpt.get();
        // check that today is not after the dropDeadline for section
        // Rubric: dropping a course after the dropDeadline date ✅
        Section section = enrollment.getSection();
        if (java.time.LocalDate.now().isAfter(section.getTerm().getDropDeadline().toLocalDate())) {
//            throw new RuntimeException("Drop deadline already passed, unable to drop enrollment");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drop deadline already passed, unable to drop enrollment");
        }
        enrollmentRepository.delete(enrollment);
    }


}
