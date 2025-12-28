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
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/teacher")
@CrossOrigin(origins = "*")
public class TeacherController {

    @Autowired private CourseTeacherRepository courseRepo;
    @Autowired private StudentCourseRepository scRepo;
    @Autowired private AttendenceRepository attendenceRepo;
    @Autowired private ScoreRepository scoreRepo;
    @Autowired private TeacherRepository teacherRepo;
    @Autowired private StudentRepository studentRepo;
    @Autowired private CollegeRepository collegeRepo;
    @Autowired private SubjectRepository subjectRepo;

    // === 注入业务 Repo ===
    @Autowired private RuleRepository ruleRepo;
    @Autowired private WarningInformationRepository warningRepo;
    @Autowired private MessageRepository messageRepo;

    @Value("${app.jwt.secret}")
    private String secret;

    private Integer getTeacherIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) throw new RuntimeException("未登录");
        try {
            String realToken = token.substring(7);
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Claims claims = Jwts.parser()
                    .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                    .parseClaimsJws(realToken)
                    .getBody();
            if (!"teacher".equals(claims.get("role"))) throw new RuntimeException("权限不足");
            return Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("身份验证失败");
        }
    }

    // === 辅助逻辑：检查考勤预警 (已修改为生成 Message) ===
    private void checkAttendanceWarning(Integer studentId, String studentName, Integer collegeId) {
        // 1. 获取该学院规则
        College col = collegeRepo.findById(collegeId).orElse(null);
        if (col == null) return;

        List<Rule> rules = ruleRepo.findByCollegeNameContaining(col.getCollegeName());
        if (rules.isEmpty()) return;
        Rule rule = rules.get(0);

        // 2. 统计缺勤次数
        long count = attendenceRepo.countByStudentId(studentId);

        // 3. 判断是否触发警告 -> 生成 Message [cite: 17]
        if (count >= rule.getAttendence()) {
            Message msg = new Message();
            msg.setStudentId(studentId);
            msg.setStudentName(studentName);
            msg.setCollegeId(collegeId);
            msg.setTitle("考勤警告"); // 标题
            // 消息内容
            msg.setDescription(studentName + "同学，你好。近期系统监测到你缺勤次数到达了" + count + "次，根据相关学院文件要求，对你进行学业警告，请你认真对待学业。");
            msg.setSendTime(new Date());

            messageRepo.save(msg);
        }
    }

    // === 辅助逻辑：检查成绩预警和消息 ===
    private void checkScoreWarningAndMessage(Integer studentId, String studentName, Integer collegeId, String currentSubject, Double currentScore) {
        // 1. 发送挂科消息 (Message) - 只要不及格就发一条消息 [cite: 17]
        if (currentScore < 60) {
            Message msg = new Message();
            msg.setStudentId(studentId);
            msg.setStudentName(studentName);
            msg.setCollegeId(collegeId);
            msg.setTitle("挂科提醒");
            msg.setDescription(currentSubject + "成绩不理想，请记得按时补考");
            msg.setSendTime(new Date());
            messageRepo.save(msg);
        }

        // 2. 处理分级预警 (Warning) [cite: 17]
        College col = collegeRepo.findById(collegeId).orElse(null);
        if (col == null) return;
        List<Rule> rules = ruleRepo.findByCollegeNameContaining(col.getCollegeName());
        if (rules.isEmpty()) return;
        Rule rule = rules.get(0);

        // 统计所有挂科科目
        List<Score> allScores = scoreRepo.findByStudentId(studentId);
        List<String> failedSubjects = allScores.stream()
                .filter(s -> s.getScores() < 60)
                .map(Score::getSubject)
                .collect(Collectors.toList());

        long failCount = failedSubjects.size();
        int base = rule.getFailure(); // 规则设定的挂科数

        String warningTitle = null;
        if (failCount >= base + 4) warningTitle = "重度预警";
        else if (failCount >= base + 2) warningTitle = "中度预警";
        else if (failCount >= base) warningTitle = "轻度预警";

        if (warningTitle != null) {
            // 查找同名预警进行覆盖，避免重复堆积
            WarningInformation warning = warningRepo.findByStudentIdAndTitle(studentId, warningTitle)
                    .orElse(new WarningInformation());

            String subjectStr = String.join("、", failedSubjects);

            warning.setStudentId(studentId);
            warning.setStudentName(studentName);
            warning.setCollegeId(collegeId);
            warning.setTitle(warningTitle);
            warning.setText(studentName + "同学，你好。近期系统检测到你的 " + subjectStr + " 考试成绩不理想，请你假期努力复习，争取开学前补考成功");
            warning.setSendTime(new Date());

            warningRepo.save(warning);
        }
    }

    // === 1. 获取个人信息 ===
    @GetMapping("/info")
    public ResponseEntity<?> getInfo(@RequestHeader("Authorization") String token) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            Teacher t = teacherRepo.findById(teaId).orElse(null);
            if (t == null) return ResponseEntity.badRequest().body("用户不存在");
            t.setPassword("******");

            // 根据 collegeId 查询并设置 collegeName
            if (t.getCollegeId() != null) {
                collegeRepo.findById(t.getCollegeId())
                        .ifPresent(college -> t.setCollegeName(college.getCollegeName()));
            } else {
                t.setCollegeName("未分配学院");
            }


            return ResponseEntity.ok(t);
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    // === 2. 更新个人信息 ===
    @PutMapping("/update")
    public ResponseEntity<?> updateInfo(@RequestHeader("Authorization") String token, @RequestBody Teacher t) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            Teacher exist = teacherRepo.findById(teaId).orElseThrow();
            exist.setTeacherName(t.getTeacherName());
            exist.setGender(t.getGender());
            exist.setPhone(t.getPhone());
            exist.setEmail(t.getEmail());
            if(t.getCollegeId() != null) exist.setCollegeId(t.getCollegeId());
            teacherRepo.save(exist);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("更新失败"); }
    }

    // === 3. 修改密码 ===
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> params) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            String oldPass = params.get("oldPassword");
            String newPass = params.get("newPassword");
            if (oldPass == null || newPass == null) return ResponseEntity.badRequest().body("参数不全");
            Teacher t = teacherRepo.findById(teaId).orElseThrow();
            if (!t.getPassword().equals(oldPass)) return ResponseEntity.badRequest().body("原密码错误，修改失败");
            t.setPassword(newPass);
            teacherRepo.save(t);
            return ResponseEntity.ok("密码修改成功，请重新登录");
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    // === 4. 考勤管理 (触发消息通知) ===
    @PostMapping("/attendance")
    public ResponseEntity<?> addAttendance(@RequestHeader("Authorization") String token, @RequestBody Attendence att) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            Teacher teacher = teacherRepo.findById(teaId).orElseThrow();
            att.setTeaId(teaId);
            att.setTeaName(teacher.getTeacherName());
            if(att.getTime() == null) att.setTime(new Date());
            attendenceRepo.save(att);

            // --- 触发考勤消息逻辑 ---
            Student s = studentRepo.findById(att.getStudentId()).orElse(null);
            if (s != null && s.getCollegeId() != null) {
                checkAttendanceWarning(s.getStudentId(), s.getStudentName(), s.getCollegeId());
            }
            return ResponseEntity.ok("新增考勤成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("操作失败: " + e.getMessage()); }
    }

    // === 5. 成绩录入 (触发预警/消息) ===
    @PostMapping("/score")
    public ResponseEntity<?> addScore(@RequestHeader("Authorization") String token, @RequestBody Score score) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            Teacher teacher = teacherRepo.findById(teaId).orElseThrow();
            score.setTeaName(teacher.getTeacherName());

            if (score.getStudentId() == null || score.getSubject() == null) {
                throw new RuntimeException("学生ID和科目名称不能为空");
            }
            if (scoreRepo.existsByStudentIdAndSubject(score.getStudentId(), score.getSubject())) {
                throw new RuntimeException("该学生【" + score.getSubject() + "】科目已有成绩，无法重复录入！");
            }

            Optional<Student> sOpt = studentRepo.findById(score.getStudentId());
            Integer collegeId = null;
            if (sOpt.isPresent()) {
                Student s = sOpt.get();
                score.setStudentName(s.getStudentName());
                score.setClassName(s.getClassName());
                collegeId = s.getCollegeId();
                if (collegeId != null) {
                    Optional<College> cOpt = collegeRepo.findById(collegeId);
                    if (cOpt.isPresent()) score.setCollegeName(cOpt.get().getCollegeName());
                }
            } else { throw new RuntimeException("学生不存在"); }

            Optional<Subject> subjectOpt = subjectRepo.findByName(score.getSubject());
            if (subjectOpt.isPresent()) score.setCredit(subjectOpt.get().getCredit());
            else throw new RuntimeException("科目不存在");

            scoreRepo.save(score);

            // --- 触发成绩预警和消息逻辑 ---
            if (collegeId != null) {
                checkScoreWarningAndMessage(score.getStudentId(), score.getStudentName(), collegeId, score.getSubject(), score.getScores());
            }
            return ResponseEntity.ok("成绩录入成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("录入失败: " + e.getMessage());
        }
    }

    // === 6. 成绩更新 (触发预警/消息) ===
    @PutMapping("/score")
    public ResponseEntity<?> updateScore(@RequestHeader("Authorization") String token, @RequestBody Score score) {
        try {
            getTeacherIdFromToken(token);
            Score existing = scoreRepo.findById(score.getId()).orElse(null);
            if (existing != null) {
                existing.setScores(score.getScores());
                scoreRepo.save(existing);

                // --- 触发成绩预警逻辑 ---
                Student s = studentRepo.findById(existing.getStudentId()).orElse(null);
                if (s != null && s.getCollegeId() != null) {
                    checkScoreWarningAndMessage(s.getStudentId(), s.getStudentName(), s.getCollegeId(), existing.getSubject(), existing.getScores());
                }
                return ResponseEntity.ok("更新成功");
            }
            return ResponseEntity.badRequest().body("记录不存在");
        } catch (Exception e) { return ResponseEntity.badRequest().body("更新失败"); }
    }

    // ... 其他接口 (delete, list等) ...
    @GetMapping("/courses")
    public ResponseEntity<?> getMyCourses(@RequestHeader("Authorization") String token) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            return ResponseEntity.ok(courseRepo.findByTeaId(teaId));
        } catch (Exception e) { return ResponseEntity.status(401).body(e.getMessage()); }
    }

    @GetMapping("/students")
    public ResponseEntity<?> getStudentsByCourse(@RequestHeader("Authorization") String token, @RequestParam String subject) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            Teacher teacher = teacherRepo.findById(teaId).orElseThrow();
            return ResponseEntity.ok(scRepo.findBySubjectAndTeaName(subject, teacher.getTeacherName()));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @DeleteMapping("/attendance/{id}")
    public ResponseEntity<?> deleteAttendance(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        try {
            getTeacherIdFromToken(token);
            attendenceRepo.deleteById(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("删除失败"); }
    }

    @GetMapping("/attendance-list")
    public ResponseEntity<?> getAttendanceList(@RequestHeader("Authorization") String token) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            return ResponseEntity.ok(attendenceRepo.findByTeaId(teaId));
        } catch (Exception e) { return ResponseEntity.status(401).build(); }
    }

    @DeleteMapping("/score/{id}")
    public ResponseEntity<?> deleteScore(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        try {
            getTeacherIdFromToken(token);
            scoreRepo.deleteById(id);
            return ResponseEntity.ok("删除成功");
        } catch (Exception e) { return ResponseEntity.badRequest().body("删除失败"); }
    }

    @GetMapping("/score-list")
    public ResponseEntity<?> getScoreList(@RequestHeader("Authorization") String token) {
        try {
            Integer teaId = getTeacherIdFromToken(token);
            Teacher teacher = teacherRepo.findById(teaId).orElseThrow();
            return ResponseEntity.ok(scoreRepo.findByTeaName(teacher.getTeacherName()));
        } catch (Exception e) { return ResponseEntity.status(401).build(); }
    }
}
