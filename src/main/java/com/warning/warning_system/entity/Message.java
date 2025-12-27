package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Date;

@Data
@Entity
@Table(name = "message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "student_id")
    private Integer studentId;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "college_id")
    private Integer collegeId;

    private String title;

    private String description; // 消息内容

    @Column(name = "send_time")
    private Date sendTime;
}