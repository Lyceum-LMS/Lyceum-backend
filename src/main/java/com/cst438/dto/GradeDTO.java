package com.cst438.dto;
/*
 * Data Transfer Object for student's score for an assignment
 */
public record GradeDTO(
        int gradeId,
        com.cst438.domain.User studentName,
        com.cst438.domain.Section studentEmail,
        String assignmentTitle,
        int courseId,
        com.cst438.domain.Section sectionId,
        Integer score
) {

}
