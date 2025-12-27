package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "course_teacher")
public class CourseTeacher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "tea_id")
    private Integer teaId;

    @Column(name = "tea_name")
    private String teaName;

    @Column(name = "sub")
    private String subject; // 科目名称
}