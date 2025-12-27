package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "attendence")
public class Attendence {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "sub_name")
    private String subjectName;

    @Column(name = "tea_id")
    private Integer teaId;

    @Column(name = "tea_name")
    private String teaName;

    @Column(name = "time")
    private Date time;
}