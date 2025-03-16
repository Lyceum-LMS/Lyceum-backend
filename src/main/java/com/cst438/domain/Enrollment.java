package com.cst438.domain;

import jakarta.persistence.*;

import java.util.List;
import java.util.Optional;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;

    // sls
	// TODO complete this class
    // add additional attribute for grade
    @Column(name="grade")
    private String grade;

    // create relationship between enrollment and user entities
    @ManyToOne
    @JoinColumn(name="id", nullable=false)
    private User student;
    // create relationship between enrollment and section entities
    @ManyToOne
    @JoinColumn(name="section_no", nullable=false)
    private Section section;

    // add getter/setter methods
    public String getGrade() { return grade; }

    public void setGrade(String grade) { this.grade = grade; }

    public int getEnrollmentId() { return enrollmentId; }

    public User getStudent() { return student; }

    public void setStudent(User user) { this.student = user; }

    public Section getSection() { return section; }

    public void setSection(Section section) { this.section = section; }
}
