package com.warning.warning_system.repository;

import com.warning.warning_system.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Integer> {
    // 根据科目名称查询科目信息
    Optional<Subject> findByName(String name);
    List<Subject> findByNameContaining(String name);
}