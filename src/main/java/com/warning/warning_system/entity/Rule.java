package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "rule")
public class Rule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "college_name")
    private String collegeName;

    // 缺勤次数阈值
    @Column(name = "attendence")
    private Integer attendence;

    // 挂科数阈值
    @Column(name = "failure")
    private Integer failure;
}