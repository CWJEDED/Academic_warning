package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "warning_information")
public class WarningInformation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String text;

    @Column(name = "send_time")
    private Date sendTime;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "college_id")
    private Integer collegeId;
}