package com.warning.warning_system.repository;
import com.warning.warning_system.entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Integer> {
    List<Teacher> findByTeacherNameContaining(String name);
}