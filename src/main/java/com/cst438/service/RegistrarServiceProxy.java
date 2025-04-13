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

@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

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
            } else if (action.equals("deleteCourse")){
                courseRepository.deleteById(parts[1]);
            } else if (action.equals("updateCourse")){
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                if (c != null) {
                    c.setTitle(dto.title());
                    c.setCredits(dto.credits());
                    courseRepository.save(c);
                } else {
                    throw new RuntimeException("Course with courseId=" + dto.courseId() + " not found.");
                }
            } else if(action.equals("addSection")){ // Section
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = new Section();

                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                Term t = termRepository.findByYearAndSemester(dto.year(), dto.semester());

                s.setSecId(dto.secId());
                s.setBuilding(dto.building());
                s.setRoom(dto.room());
                s.setTimes(dto.times());
                s.setCourse(c);
                s.setTerm(t);
                s.setInstructor_email(dto.instructorEmail());
                s.setSectionNo(dto.secNo());
                sectionRepository.save(s);
            } else if (action.equals("deleteSection")){
                sectionRepository.deleteById(Integer.valueOf(parts[1]));
            } else if (action.equals("updateSection")){
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Section s = sectionRepository.findById(dto.secNo()).orElse(null);

                Course c = courseRepository.findById(dto.courseId()).orElse(null);
                Term t = termRepository.findByYearAndSemester(dto.year(), dto.semester());

                if (s != null) {
                    s.setSecId(dto.secId());
                    s.setBuilding(dto.building());
                    s.setRoom(dto.room());
                    s.setTimes(dto.times());
                    s.setCourse(c);
                    s.setTerm(t);
                    s.setInstructor_email(dto.instructorEmail());
                    sectionRepository.save(s);
                } else {
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

            } else if(action.equals("deleteUser")){

                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                userRepository.deleteById(dto.id());

            } else if(action.equals("updateUser")){

                UserDTO dto = fromJsonString(parts[1], UserDTO.class);
                User u = userRepository.findById(dto.id()).orElse(null);

                u.setName(dto.name());
                u.setEmail(dto.email());
                u.setType(dto.type());

                userRepository.save(u);

            } else if(action.equals("addEnrollment")){   // Enrollment
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = new Enrollment();

                User u = userRepository.findById(dto.studentId()).orElse(null);
                Section s = sectionRepository.findById((dto.sectionNo())).orElse(null);

                e.setEnrollmentId(dto.enrollmentId());
                e.setStudent(u);
                e.setSection(s);
                if (dto.grade() == null) {
                    e.setGrade(null);
                } else {
                    e.setGrade(dto.grade());
                }

                enrollmentRepository.save(e);

            } else if(action.equals("deleteEnrollment")){
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                enrollmentRepository.deleteById(dto.enrollmentId());
            } else if(action.equals("updateEnrollment")){
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);

                User u = userRepository.findById(dto.studentId()).orElse(null);
                Section s = sectionRepository.findById((dto.sectionNo())).orElse(null);

                e.setStudent(u);
                e.setSection(s);
                if (dto.grade() == null) {
                    e.setGrade(null);
                } else {
                    e.setGrade(dto.grade());
                }

                enrollmentRepository.save(e);
            }
        } catch (Exception e) {
            System.out.println("Exception in receivedFromRegistrar +" + e.getMessage());
        }
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