package com.warning.warning_system.repository;

import com.warning.warning_system.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StudentCourseRepository extends JpaRepository<StudentCourse, Integer> {
    // 查找选了某位老师某门课的学生
    List<StudentCourse> findBySubjectAndTeaName(String subject, String teaName);
    List<StudentCourse> findByStudentId(Integer studentId);
}