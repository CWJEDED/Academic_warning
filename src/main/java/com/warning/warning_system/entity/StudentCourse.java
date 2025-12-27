package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "student_course")
public class StudentCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "subject")
    private String subject;

    @Column(name = "tea_name")
    private String teaName;
}