package com.cst438.domain;

import jakarta.persistence.*;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;
	
	// TODO complete this class
    // add additional attribute for grade
    private String grade;
    // create relationship between enrollment and user entities
    @ManyToOne
    @JoinColumn(name="user_id", nullable=false)
    private User student;
    // create relationship between enrollment and section entities
    @ManyToOne
    @JoinColumn(name="section_no", nullable = false)
    private Section section;

    // add getter/setter methods
    public int getEnrollmentId(){
        return enrollmentId;
    }
    public void setEnrollmentId(int enrollmentId){
        this.enrollmentId = enrollmentId;
    }
    public String getGrade() {
        return grade;
    }
    public void setGrade(String grade) {
        this.grade = grade;
    }
    public User getStudent() {
        return student;
    }
    public void setStudent(User student) {
        this.student = student;
    }
    public Section getSection() {
        return section;
    }
    public void setSection(Section section) {
        this.section = section;
    }
}
