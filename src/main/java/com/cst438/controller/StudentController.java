package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TermRepository termRepository;
    /**
     students lists there enrollments given year and semester value
     returns list of enrollments, may be empty
     logged in user must be the student (assignment 7)
     */
   @GetMapping("/enrollments")
   public List<EnrollmentDTO> getSchedule(
           @RequestParam("year") int year,
           @RequestParam("semester") String semester,
           @RequestParam("studentId") int studentId) {

     // TO-DO
       // verify studentId is valid
       User user = userRepository.findById(studentId).orElse(null);
       if (user==null) {
           throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "studentId invalid ");
       }
       // verify year, semester are valid
       Term term = termRepository.findByYearAndSemester(year, semester);
       if (term == null) {
           throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "year, semester invalid ");
       }
	 //  hint: use enrollment repository method findByYearAndSemesterOrderByCourseId
     //  remove the following line when done
       List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);
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
                   e.getSection().getTerm().getSemester()));
       }
       return  dto_list;
   }

    /**
     students lists there assignments given year and semester value
     returns list of assignments may be empty
     logged in user must be the student (assignment 7)
     */
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {


        // TO-DO remove the following line when done
        // verify studentId is valid
        User user = userRepository.findById(studentId).orElse(null);
        if (user==null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "studentId invalid ");
        }
        // verify year, semester are valid
        Term term = termRepository.findByYearAndSemester(year, semester);
        if (term == null) {
            throw  new ResponseStatusException( HttpStatus.NOT_FOUND, "year, semester invalid ");
        }
        //  return a list of assignments and (if they exist) the assignment grade
        //  for all sections that the student is enrolled for the given year and semester
        //  hint: use the assignment repository method findByStudentIdAndYearAndSemesterOrderByDueDate
        List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        List<AssignmentStudentDTO> dto_list = new ArrayList<>();
        for (Assignment a : assignments) {
            int assignmentId = a.getAssignmentId();
            int sectionNo = a.getSection().getSectionNo();
            int enrollmentId = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId).getEnrollmentId();
            Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollmentId, assignmentId);
            dto_list.add(new AssignmentStudentDTO(
                   assignmentId,
                    a.getTitle(),
                    a.getDueDate(),
                    a.getSection().getCourse().getCourseId(),
                    a.getSection().getSecId(),
                    (grade!=null)? grade.getScore(): null
            ));
        }
        return  dto_list;
    }
}