package com.warning.warning_system.repository;

import com.warning.warning_system.entity.WarningInformation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WarningInformationRepository extends JpaRepository<WarningInformation, Integer> {
    // 1. 辅导员查询用
    List<WarningInformation> findByCollegeIdOrderBySendTimeDesc(Integer collegeId);

    // 2. 辅导员搜索用
    List<WarningInformation> findByCollegeIdAndStudentNameContaining(Integer collegeId, String studentName);

    // 3. 学生端查询用
    List<WarningInformation> findByStudentIdOrderBySendTimeDesc(Integer studentId);

    // 4. === 缺失的方法：教师端触发预警去重用 ===
    Optional<WarningInformation> findByStudentIdAndTitle(Integer studentId, String title);
}