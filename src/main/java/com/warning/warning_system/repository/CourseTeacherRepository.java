package com.warning.warning_system.repository;

import com.warning.warning_system.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CourseTeacherRepository extends JpaRepository<CourseTeacher, Integer> {
    List<CourseTeacher> findByTeaId(Integer teaId);
}