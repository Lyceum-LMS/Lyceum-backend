package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() { return new Queue("gradebook_service", true); }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    CourseRepository courseRepository;

    @Autowired
    SectionRepository sectionRepository;

    @Autowired
    TermRepository termRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        //TODO implement this message
        try{
            System.out.println("receive from Registrar " + message);
            String[] parts = message.split(" ", 2);
            String action = parts[0];

            if(action.equals("addCourse")){ // Course

                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = new Course();

                c.setCourseId(dto.courseId());
                c.setTitle(dto.title());
                c.setCredits(dto.credits());
                courseRepository.save(c);
                System.out.print("addCourse: " + dto.courseId() +  " course added");

            } else if (action.equals("deleteCourse")){

                courseRepository.deleteById(parts[1]);
                System.out.print("deleteCourse: " + parts[1] +  " course deleted");

            } else if (action.equals("updateCourse")){

                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = courseRepository.findById(dto.courseId()).orElse(null);

                if (c != null) {
                    c.setTitle(dto.title());
                    c.setCredits(dto.credits());
                    courseRepository.save(c);
                    System.out.print("updateCourse: " + dto.courseId() + " course updated");
                } else {
                    System.out.print("ERROR: updateCourse failed - courseId= " + dto.courseId() + " not found.");
                    throw new RuntimeException("Course with courseId=" + dto.courseId() + " not found.");
                }

            } else if(action.equals("addSection")){ // Section
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = new Section();

                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                Term t = termRepository.findByYearAndSemester(dto.year(), dto.semester());

                s.setSectionNo(dto.secNo()); // set primary key
                s.setSecId(dto.secId());
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());
                s.setCourse(c);
                s.setTerm(t);
                s.setInstructor_email(dto.instructorEmail());
                sectionRepository.save(s);
                System.out.print("addSection: " + dto.secNo() +  " section added");

            } else if (action.equals("deleteSection")){

                sectionRepository.deleteById(Integer.valueOf(parts[1]));
                System.out.print("deleteSection: " + parts[1] +  " section deleted");

            } else if (action.equals("updateSection")){

                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = sectionRepository.findById(dto.secNo()).orElse(null);

                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                Term t = termRepository.findByYearAndSemester(dto.year(), dto.semester());

                if (s != null) {  // check if databases are out of sync;
                    s.setSecId(dto.secId());
                    s.setBuilding(dto.building());
                    s.setRoom(dto.room());
                    s.setTimes(dto.times());
//                    s.setCourse(c);  // not necessary
//                    s.setTerm(t);  // not necessary
                    s.setInstructor_email(dto.instructorEmail());
                    sectionRepository.save(s);
                    System.out.print("updateSection: " + dto.secNo() + " section updated");
                } else { // if null, you can throw exception or fix by adding Section
                    System.out.print("ERROR: updateSection failed - secNo=" + dto.secNo() + " not found.");
                    throw new RuntimeException("Section with secNo=" + dto.secNo() + " not found.");
                }

            } else if(action.equals("addUser")){   // User

                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User u = new User();

                u.setId(dto.id());
                u.setName(dto.name());
                u.setEmail(dto.email());
                u.setType(dto.type());

                userRepository.save(u);
                System.out.print("addUser: " + dto.name() +  " user added");

            } else if(action.equals("deleteUser")){

                userRepository.deleteById(Integer.valueOf(parts[1]));
                System.out.print("deleteUser: " + parts[1] +  " user deleted");

            } else if(action.equals("updateUser")){

                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User u = userRepository.findById(dto.id()).orElse(null);

                if (u != null){
                    u.setName(dto.name());
                    u.setEmail(dto.email());
                    u.setType(dto.type());

                    userRepository.save(u);
                    System.out.print("updateUser: " + dto.name() +  " user added");
                } else {
                    System.out.print("ERROR: updateUser failed - user= " + dto.name() + " not found.");
                    throw new RuntimeException("User with id=" + dto.id() + " not found.");
                }

            } else if(action.equals("addEnrollment")){   // Enrollment

                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = new Enrollment();

                User u = userRepository.findById(dto.studentId()).orElse(null);
                Section s = sectionRepository.findById((dto.sectionNo())).orElse(null);

                e.setEnrollmentId(dto.enrollmentId());
                e.setGrade(dto.grade());

                e.setStudent(u);
                e.setSection(s);

                enrollmentRepository.save(e);
                System.out.print("addEnrollment: " + dto.enrollmentId() +  " enrollment added");

            } else if(action.equals("dropEnrollment")){

                enrollmentRepository.deleteById(Integer.valueOf(parts[1]));
                System.out.print("dropEnrollment: " + parts[1] +  " enrollment deleted");

            }
        } catch (Exception e) {
            System.out.println("Exception in receivedFromRegistrar +" + e.getMessage());
        }
    } //

    public void sendFinalGrade(EnrollmentDTO enrollment) {
        String msg = "updateEnrollment " + asJsonString(enrollment);
        sendMessage(msg);
        System.out.println("Sent message to Registrar " + msg);
    }

    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}