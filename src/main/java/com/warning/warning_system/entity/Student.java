package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "student")
public class Student {
    /**
     * 学号（账号）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_id")
    private Integer studentId;

    /**
     * 姓名
     */
    @Column(name = "student_name", nullable = false)
    private String studentName;

    /**
     * 性别
     */
    @Column(name = "gender")
    private String gender;

    /**
     * 所属学院ID (关联college表)
     */
    @Column(name = "college_id")
    private Integer collegeId;

    /**
     * 所属班级
     */
    @Column(name = "class_name")
    private String className;

    /**
     * 所属年级
     */
    @Column(name = "grade")
    private String grade;

    /**
     * 手机
     */
    @Column(name = "phone")
    private String phone;

    /**
     * 邮箱
     */
    @Column(name = "email")
    private String email;

    /**
     * 密码 (默认 123456)
     */
    @Column(name = "password", nullable = false)
    private String password;
}