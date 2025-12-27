package com.warning.warning_system.repository;

import com.warning.warning_system.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollegeRepository extends JpaRepository<College, Integer> {
    List<College> findByCollegeNameContaining(String name);
}