package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "teacher")
public class Teacher {
    /**
     * 教师工号（账号）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "teacher_id")
    private Integer teacherId;

    /**
     * 姓名
     */
    @Column(name = "teacher_name", nullable = false)
    private String teacherName;

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