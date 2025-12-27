package com.warning.warning_system.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "subject")
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name; // 科目名称

    @Column(name = "college_name")
    private String collegeName;

    @Column(name = "credit")
    private Float credit; // 学分 (新增字段)
}