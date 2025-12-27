package com.warning.warning_system.controller;

import com.warning.warning_system.entity.Counsellor;
import com.warning.warning_system.entity.WarningInformation;
import com.warning.warning_system.repository.CounsellorRepository;
import com.warning.warning_system.repository.WarningInformationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/counsellor")
@CrossOrigin(origins = "*")
public class CounsellorController {

    @Autowired private CounsellorRepository counsellorRepo;
    @Autowired private WarningInformationRepository warningRepo;

    // 1. 获取辅导员个人信息
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getInfo(@PathVariable Integer id) {
        return counsellorRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.badRequest().build());
    }

    // 2. 修改个人信息 (手机、邮箱等)
    @PutMapping("/update")
    public ResponseEntity<?> updateInfo(@RequestBody Counsellor updatedCounsellor) {
        Counsellor c = counsellorRepo.findById(updatedCounsellor.getCounsellorId()).orElse(null);
        if (c == null) return ResponseEntity.badRequest().body("用户不存在");

        c.setPhone(updatedCounsellor.getPhone());
        c.setEmail(updatedCounsellor.getEmail());
        c.setCounsellorName(updatedCounsellor.getCounsellorName());
        // 学院和ID通常不可变

        counsellorRepo.save(c);
        return ResponseEntity.ok("更新成功");
    }

    // 3. 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> params) {
        Integer id = Integer.parseInt(params.get("id"));
        String oldPass = params.get("oldPassword");
        String newPass = params.get("newPassword");

        Counsellor c = counsellorRepo.findById(id).orElse(null);
        if (c == null) return ResponseEntity.badRequest().body("用户不存在");

        if (!c.getPassword().equals(oldPass)) {
            return ResponseEntity.badRequest().body("原密码错误");
        }

        c.setPassword(newPass);
        counsellorRepo.save(c);
        return ResponseEntity.ok("密码修改成功");
    }

    // 4. 获取本学院的所有预警信息
    @GetMapping("/warnings")
    public ResponseEntity<?> getWarnings(@RequestParam Integer counsellorId,
                                         @RequestParam(required = false) String studentName) {
        Counsellor c = counsellorRepo.findById(counsellorId).orElse(null);
        if (c == null) return ResponseEntity.badRequest().body("辅导员信息错误");

        List<WarningInformation> list;
        if (studentName != null && !studentName.isEmpty()) {
            list = warningRepo.findByCollegeIdAndStudentNameContaining(c.getCollegeId(), studentName);
        } else {
            list = warningRepo.findByCollegeIdOrderBySendTimeDesc(c.getCollegeId());
        }
        return ResponseEntity.ok(list);
    }
}