package com.cst438.domain;

import jakarta.persistence.*;

// TODO complete this class ✅ Done
// add additional attribute for score that is nullable ✅ Done
// add relationship between grade and assignment entities ✅ Done
// add relationship between grade and enrollment entities ✅ Done
// add getter/setter methods ✅ Done

@Entity
@Table(name="grade")
public class Grade {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="grade_id")
    private int gradeId;
    private Integer score;
    // Many-to-one relationship to assignment
    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;
    // Many-to-one relationship to enrollment
    @ManyToOne
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    // getters and setters
    public int getGradeId() {
        return gradeId;
    }

    public void setGradeId(int gradeId) {
        this.gradeId = gradeId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

}
