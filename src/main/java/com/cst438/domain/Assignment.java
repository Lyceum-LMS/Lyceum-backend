package com.cst438.domain;

import jakarta.persistence.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

@Entity
public class Assignment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="assignment_id")
    private int assignmentId;

    // sls
    // TODO  complete this class
    // add additional attributes for title, dueDate
    @Column(name="title")
    private String title;
    @Column(name="due_date")
    private Date dueDate;

    // add relationship between assignment and section entities
    @ManyToOne
    @JoinColumn(name="section_no", nullable=false)
    private Section section;

    @OneToMany(mappedBy="assignment")
    private List<Grade> grades;

    // add getter and setter methods
    public String getTitle() { return title; }

    public void setTitle(String title) { this.title = title; }

    public int getAssignmentId() { return assignmentId; }
    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Section getSection() { return section; }

    public void setSection(Section section) { this.section = section; }

    public List<Grade> getGrades() { return grades; }

    public void setGrades(List<Grade> grades) { this.grades = grades; }
}