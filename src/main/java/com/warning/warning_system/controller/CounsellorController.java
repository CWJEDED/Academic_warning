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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/counsellor")
@CrossOrigin(origins = "*")
public class CounsellorController {

    @Autowired private CounsellorRepository counsellorRepo;
    @Autowired private WarningInformationRepository warningRepo;

    // 注入用于统计和查询的 Repository
    @Autowired private StudentRepository studentRepo;
    @Autowired private ScoreRepository scoreRepo;
    @Autowired private StudentCourseRepository scRepo;
    @Autowired private CollegeRepository collegeRepo;

    @Value("${app.jwt.secret}")
    private String secret;

    // 辅助方法：验证 Token 并获取辅导员ID
    private Integer validateTokenAndGetId(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("未登录");
        }
        try {
            String realToken = token.substring(7);
            byte[] keyBytes = Decoders.BASE64.decode(secret);
            Claims claims = Jwts.parser().setSigningKey(Keys.hmacShaKeyFor(keyBytes)).parseClaimsJws(realToken).getBody();
            if (!"counsellor".equals(claims.get("role"))) throw new RuntimeException("权限不足");
            return Integer.parseInt(claims.getSubject());
        } catch (Exception e) {
            throw new RuntimeException("Token无效或过期");
        }
    }

    // 前端调用路径是 /info/{id}，这里保持路径不变，但增加Token校验
    @GetMapping("/info/{id}")
    public ResponseEntity<?> getInfo(@RequestHeader("Authorization") String token, @PathVariable Integer id) {
        try {
            // 1. 验证 Token
            Integer tokenUserId = validateTokenAndGetId(token);

            // 2. 安全校验：确保Token里的ID和请求的ID一致，防止查别人
            if (!tokenUserId.equals(id)) {
                return ResponseEntity.status(403).body("无权查看此用户信息");
            }

            return counsellorRepo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.badRequest().build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 2. 更新个人信息 [已添加Token验证]
    @PutMapping("/update")
    public ResponseEntity<?> updateInfo(@RequestHeader("Authorization") String token, @RequestBody Counsellor c) {
        try {
            Integer tokenUserId = validateTokenAndGetId(token);

            // 安全校验：只能修改自己的信息
            if (!tokenUserId.equals(c.getCounsellorId())) {
                return ResponseEntity.status(403).body("无权修改他人信息");
            }

            Counsellor exist = counsellorRepo.findById(c.getCounsellorId()).orElseThrow();
            exist.setCounsellorName(c.getCounsellorName());
            exist.setPhone(c.getPhone());
            exist.setEmail(c.getEmail());
            // CollegeId 一般不在此处修改，保持原样
            counsellorRepo.save(exist);
            return ResponseEntity.ok("更新成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("更新失败: " + e.getMessage());
        }
    }

    // 3. 修改密码
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, Object> params) {
        try {
            Integer tokenConsellorId = validateTokenAndGetId(token);
            String oldPass = (String) params.get("oldPassword");
            String newPass = (String) params.get("newPassword");
            Counsellor c = counsellorRepo.findById(tokenConsellorId).orElseThrow();
            if (!c.getPassword().equals(oldPass)) return ResponseEntity.badRequest().body("原密码错误");
            c.setPassword(newPass);
            counsellorRepo.save(c);
            return ResponseEntity.ok("修改成功");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. 获取本院学生预警信息 [已添加Token验证]
    //@RequestParam(required = false) String studentName可选的请求参数，用于按学生姓名模糊搜索预警信息（非必填）。
    @GetMapping("/warnings")
    public ResponseEntity<?> getWarnings(@RequestHeader("Authorization") String token, @RequestParam Integer counsellorId, @RequestParam(required = false) String studentName) {
        try {
            Integer tokenUserId = validateTokenAndGetId(token);

            if (!tokenUserId.equals(counsellorId)) {
                return ResponseEntity.status(403).body("无权查看");
            }

            Counsellor c = counsellorRepo.findById(counsellorId).orElseThrow();
            Integer collegeId = c.getCollegeId();
            if (collegeId == null) return ResponseEntity.ok(new ArrayList<>());

            List<WarningInformation> list = warningRepo.findByCollegeIdOrderBySendTimeDesc(collegeId);

            // 如果有搜索参数，进行过滤
            if (studentName != null && !studentName.trim().isEmpty()) {
                list = list.stream()
                        .filter(w -> w.getStudentName().contains(studentName))
                        .collect(Collectors.toList());
            }
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. 获取所有学院列表 [已添加Token验证]
    @GetMapping("/colleges")
    public ResponseEntity<?> getAllColleges(@RequestHeader("Authorization") String token) {
        try {
            validateTokenAndGetId(token); // 仅需验证登录状态
            return ResponseEntity.ok(collegeRepo.findAll());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }

    // 6. 获取本学院所有学生的选课及成绩 [原本已有验证，保持不变]
    @GetMapping("/college-scores")
    public ResponseEntity<?> getCollegeScores(@RequestHeader("Authorization") String token) {
        try {
            Integer counsellorId = validateTokenAndGetId(token);
            Counsellor c = counsellorRepo.findById(counsellorId).orElseThrow();
            Integer collegeId = c.getCollegeId();

            if (collegeId == null) return ResponseEntity.ok(new ArrayList<>());

            // 1. 找到该学院所有学生
            List<Student> students = studentRepo.findByCollegeId(collegeId);
            if (students.isEmpty()) return ResponseEntity.ok(new ArrayList<>());

            List<Integer> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());

            // 2. 获取选课记录
            List<StudentCourse> courses = scRepo.findByStudentIdIn(studentIds);

            // 3. 获取成绩记录
            List<Score> scores = scoreRepo.findByStudentIdIn(studentIds);

            // 4. 构建成绩映射 Map
            Map<String, Double> scoreMap = new HashMap<>();
            if (scores != null) {
                for (Score s : scores) {
                    // Key: studentId-subject
                    String key = s.getStudentId() + "-" + s.getSubject();
                    if (s.getScores() != null) {
                        scoreMap.put(key, s.getScores());
                    }
                }
            }

            // 5. 组装最终结果
            List<Map<String, Object>> result = new ArrayList<>();
            for (StudentCourse sc : courses) {
                Map<String, Object> item = new HashMap<>();
                item.put("studentId", sc.getStudentId());
                item.put("studentName", sc.getStudentName());
                item.put("subject", sc.getSubject());
                item.put("teaName", sc.getTeaName());

                String key = sc.getStudentId() + "-" + sc.getSubject();
                if (scoreMap.containsKey(key)) {
                    item.put("score", scoreMap.get(key));
                } else {
                    item.put("score", null);
                }
                result.add(item);
            }
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("获取数据失败: " + e.getMessage());
        }
    }

    // 7. 挂科率统计 [原本已有验证，保持不变]
    @GetMapping("/failure-stats")
    public ResponseEntity<?> getFailureStats(@RequestHeader("Authorization") String token) {
        try {
            Integer counsellorId = validateTokenAndGetId(token);
            Counsellor c = counsellorRepo.findById(counsellorId).orElseThrow();
            Integer collegeId = c.getCollegeId();

            if (collegeId == null) return ResponseEntity.ok(new ArrayList<>());

            List<Student> students = studentRepo.findByCollegeId(collegeId);
            List<Integer> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());

            if (studentIds.isEmpty()) return ResponseEntity.ok(new ArrayList<>());

            List<Score> scores = scoreRepo.findByStudentIdIn(studentIds);

            Map<String, List<Score>> bySubject = scores.stream()
                    .collect(Collectors.groupingBy(Score::getSubject));

            List<Map<String, Object>> stats = new ArrayList<>();

            for (Map.Entry<String, List<Score>> entry : bySubject.entrySet()) {
                String subject = entry.getKey();
                List<Score> list = entry.getValue();

                long total = list.size();
                long failed = list.stream()
                        .filter(s -> s.getScores() != null && s.getScores() < 60)
                        .count();

                double rate = total == 0 ? 0.0 : (double) failed / total * 100;

                Map<String, Object> dto = new HashMap<>();
                dto.put("subject", subject);
                dto.put("total", total);
                dto.put("failed", failed);
                dto.put("rate", String.format("%.2f", rate));
                stats.add(dto);
            }

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("统计失败");
        }
    }
}
