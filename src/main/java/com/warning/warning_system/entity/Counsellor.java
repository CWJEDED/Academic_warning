package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "counsellor")
public class Counsellor {
    /**
     * 辅导员工号（账号）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "counsellor_id")
    private Integer counsellorId;

    /**
     * 姓名
     */
    @Column(name = "counsellor_name", nullable = false)
    private String counsellorName;

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