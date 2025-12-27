package com.warning.warning_system.repository;

import com.warning.warning_system.entity.Rule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RuleRepository extends JpaRepository<Rule, Integer> {
    List<Rule> findByCollegeNameContaining(String collegeName);
}