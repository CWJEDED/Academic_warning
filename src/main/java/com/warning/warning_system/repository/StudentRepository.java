package com.warning.warning_system.repository;
import com.warning.warning_system.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Integer> {

    List<Student> findByStudentNameContaining(String name);
}