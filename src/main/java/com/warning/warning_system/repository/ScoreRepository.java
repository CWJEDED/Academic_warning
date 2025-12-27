package com.warning.warning_system.repository;

import com.warning.warning_system.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ScoreRepository extends JpaRepository<Score, Integer> {
    List<Score> findByTeaName(String teaName);
    boolean existsByStudentIdAndSubject(Integer studentId, String subject);
    List<Score> findByStudentId(Integer studentId);
}