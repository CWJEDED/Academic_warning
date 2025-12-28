package com.warning.warning_system.controller;

import com.warning.warning_system.entity.*;
import com.warning.warning_system.repository.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/student") //
@CrossOrigin(origins = "*")
public class StudentController {

    @Autowired private StudentRepository studentRepo;
    @Autowired private StudentCourseRepository scRepo;
    @Autowired private ScoreRepository scoreRepo;
    @Autowired private WarningInformationRepository warningRepo;
    @Autowired private MessageRepository messageRepo;

    // 新增注入
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private CourseTeacherRepository courseTeacherRepo;

    @Value("${app.jwt.secret}")
    private String secret;

    private Integer validateTokenAndGetId(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        try {
            String realToken = token.substring(7);
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Claims claims = Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(keyBytes)).parseClaimsJws(realToken).getBody();
            if (!"student".equals(claims.get("role"))) throw new RuntimeException("权限不足");
            return Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Token无效或过期");
        }
    }

    // 1. 获取个人信息
    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestHeader("Authorization") String token) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            return studentRepo.findById(studentId).map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // 2. 更新个人信息
    @PutMapping("/update")
    public ResponseEntity<?> updateInfo(@RequestHeader("Authorization") String token, @RequestBody Student updatedStudent) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            Student s = studentRepo.findById(studentId).orElseThrow();
            s.setPhone(updatedStudent.getPhone());
            s.setEmail(updatedStudent.getEmail());
            studentRepo.save(s);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("更新失败"); }
    }

    // 3. 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> params) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            String oldPass = params.get("oldPassword");
            String newPass = params.get("newPassword");
            Student s = studentRepo.findById(studentId).orElseThrow();
            if (!s.getPassword().equals(oldPass)) return ResponseEntity.badRequest().body("原密码错误");
            s.setPassword(newPass);
            studentRepo.save(s);
            return ResponseEntity.ok("密码修改成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // 4. 获取已选课程
    @GetMapping("/courses")
    public ResponseEntity<?> getMyCourses(@RequestHeader("Authorization") String token) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            return ResponseEntity.ok(scRepo.findByStudentId(studentId));
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // === 新增接口 4.1: 获取本学院所有开设课程 (可选课程列表) ===
    @GetMapping("/available-courses")
    public ResponseEntity<?> getAvailableCourses(@RequestHeader("Authorization") String token) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            Student s = studentRepo.findById(studentId).orElseThrow();
            Integer collegeId = s.getCollegeId();

            if(collegeId == null) return ResponseEntity.ok(new ArrayList<>());

            // 1. 找到该学院的所有老师
            List<Teacher> teachers = teacherRepo.findByCollegeId(collegeId);
            if(teachers.isEmpty()) return ResponseEntity.ok(new ArrayList<>());

            List<Integer> teaIds = teachers.stream().map(Teacher::getTeacherId).collect(Collectors.toList());

            // 2. 找到这些老师开设的所有课程
            List<CourseTeacher> courses = courseTeacherRepo.findByTeaIdIn(teaIds);

            return ResponseEntity.ok(courses);
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // === 新增接口 4.2: 选课 ===
    @PostMapping("/course/enroll")
    public ResponseEntity<?> enrollCourse(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> params) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            String subject = params.get("subject");
            String teaName = params.get("teaName");

            // 检查是否已选
            if (scRepo.findByStudentIdAndSubjectAndTeaName(studentId, subject, teaName).isPresent()) {
                return ResponseEntity.badRequest().body("你已经选修了该课程");
            }

            Student s = studentRepo.findById(studentId).orElseThrow();

            StudentCourse sc = new StudentCourse();
            sc.setStudentId(studentId);
            sc.setStudentName(s.getStudentName());
            sc.setSubject(subject);
            sc.setTeaName(teaName);

            scRepo.save(sc);
            return ResponseEntity.ok("选课成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("选课失败: " + e.getMessage()); }
    }

    // === 新增接口 4.3: 退课 ===
    @Transactional
    @PostMapping("/course/drop")
    public ResponseEntity<?> dropCourse(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> params) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            String subject = params.get("subject");
            String teaName = params.get("teaName");

            // 1. 检查是否有成绩
            if (scoreRepo.existsByStudentIdAndSubject(studentId, subject)) {
                return ResponseEntity.badRequest().body("该课程已有成绩，无法退课！");
            }

            // 2. 查找并删除选课记录
            Optional<StudentCourse> scOpt = scRepo.findByStudentIdAndSubjectAndTeaName(studentId, subject, teaName);
            if (scOpt.isPresent()) {
                scRepo.delete(scOpt.get());
                return ResponseEntity.ok("退课成功");
            } else {
                return ResponseEntity.badRequest().body("未找到该选课记录");
            }
        } catch (Exception e) { return ResponseEntity.badRequest().body("退课失败: " + e.getMessage()); }
    }

    // 5. 获取成绩
    @GetMapping("/scores")
    public ResponseEntity<?> getMyScores(@RequestHeader("Authorization") String token) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            return ResponseEntity.ok(scoreRepo.findByStudentId(studentId));
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // 获取预警信息
    @GetMapping("/warnings")
    public ResponseEntity<?> getMyWarnings(@RequestHeader("Authorization") String token) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            return ResponseEntity.ok(warningRepo.findByStudentIdOrderBySendTimeDesc(studentId));
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // 获取系统通知
    @GetMapping("/messages")
    public ResponseEntity<?> getMyMessages(@RequestHeader("Authorization") String token) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            return ResponseEntity.ok(messageRepo.findByStudentIdOrderBySendTimeDesc(studentId));
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // 删除预警
    @DeleteMapping("/warnings/{id}")
    public ResponseEntity<?> deleteWarning(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            WarningInformation w = warningRepo.findById(id).orElseThrow();
            if(!w.getStudentId().equals(studentId)) return ResponseEntity.badRequest().body("无权删除");
            warningRepo.deleteById(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("删除失败"); }
    }

    // 删除消息
    @DeleteMapping("/messages/{id}")
    public ResponseEntity<?> deleteMessage(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        try {
            Integer studentId = validateTokenAndGetId(token);
            Message m = messageRepo.findById(id).orElseThrow();
            if(!m.getStudentId().equals(studentId)) return ResponseEntity.badRequest().body("无权删除");
            messageRepo.deleteById(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("删除失败"); }
    }
}
