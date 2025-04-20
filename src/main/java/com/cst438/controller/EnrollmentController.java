package com.cst438.controller;


import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    UserRepository userRepository;

    /**
     instructor gets list of enrollments for a section
     list of enrollments returned is in order by student name
     logged in user must be the instructor for the section (assignment 7)
     */
    // a8 sls
    @GetMapping("/sections/{sectionNo}/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo,
            Principal principal) {

        String instructorEmail = principal.getName();

        // TO-DO
		//  hint: use enrollment repository findEnrollmentsBySectionNoOrderByStudentName method
        //  remove the following line when done
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);

        if(!enrollments.isEmpty()){
            Section section = enrollments.get(0).getSection();
            if (!section.getInstructorEmail().equals(instructorEmail)){
                System.out.println("Invalid instructor for enrollment");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Instructor for enrollment");
            }
        }

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
        return  dto_list;
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    /**
     instructor updates enrollment grades
     only the grade attribute of enrollment can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_INSTRUCTOR')")
    public void updateEnrollmentGrade(
            @RequestBody List<EnrollmentDTO> dlist,
            Principal principal) {

        String instructorEmail = principal.getName();

        // TO-DO
        // For each EnrollmentDTO in the list
        //  find the Enrollment entity using enrollmentId
        //  update the grade and save back to database

        for (EnrollmentDTO dto : dlist) {
            Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);

            Section section = e.getSection();

            if (section.getInstructorEmail().equals(instructorEmail)){
                if (e != null) {
                    if (dto.grade() != null){
                        e.setGrade(dto.grade().toUpperCase());
                    } else {
                        e.setGrade(null);
                    }
                    enrollmentRepository.save(e);
                }
            } else {
                System.out.println("Invalid instructor for enrollment");
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid Instructor for enrollment");
            }
        }
    }
}
