package com.warning.warning_system.entity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "administrator")
public class Administrator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_id")
    private Integer adminId;

    private String password;
}