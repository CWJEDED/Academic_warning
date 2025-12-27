package com.warning.warning_system.repository;

import com.warning.warning_system.entity.Counsellor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CounsellorRepository extends JpaRepository<Counsellor, Integer> {
    List<Counsellor> findByCounsellorNameContaining(String name);
}