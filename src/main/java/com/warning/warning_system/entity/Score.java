package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "score")
public class Score {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "college_name")
    private String collegeName;

    @Column(name = "class_name")
    private String className;

    @Column(name = "subject")
    private String subject;

    @Column(name = "scores")
    private Double scores;

    @Column(name = "credit")
    private Float credit;

    @Column(name = "tea_name")
    private String teaName;
}