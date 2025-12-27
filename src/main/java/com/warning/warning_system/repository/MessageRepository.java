package com.warning.warning_system.repository;

import com.warning.warning_system.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByStudentIdOrderBySendTimeDesc(Integer studentId);
}