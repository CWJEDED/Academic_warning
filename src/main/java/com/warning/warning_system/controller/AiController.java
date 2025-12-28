package com.warning.warning_system.controller;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.chat.ChatResponse;
import com.warning.warning_system.entity.Attendence;
import com.warning.warning_system.entity.Score;
import com.warning.warning_system.repository.AttendenceRepository;
import com.warning.warning_system.repository.ScoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = "*") // 允许前端跨域
public class AiController {
    private static final String API_KEY = "YOUR_API_KEY".trim();
    private static final String SECRET_KEY = "YOUR_SECRET_KEY".trim();

    // 注入数据库操作类，用于查询学生数据
    @Autowired
    private ScoreRepository scoreRepo;

    @Autowired
    private AttendenceRepository attendenceRepo;

    /**
     * AI 聊天接口
     * @param question 用户的问题
     * @param studentId 当前学生的ID (前端传过来)
     */
    @GetMapping("/chat")
    public String chat(@RequestParam String question, @RequestParam(required = false) Integer studentId) {
        try {
            // 1. 初始化客户端
            Qianfan qianfan = new Qianfan(API_KEY, SECRET_KEY);

            // 2. 构建最终发送给 AI 的提示词 (Prompt)
            StringBuilder finalPrompt = new StringBuilder();

            // === 核心逻辑：关键词检测与数据注入 ===
            // 只有当传入了 studentId，且问题里包含相关关键词时，才去查数据库
            boolean needContext = studentId != null && (
                    question.contains("学习") ||
                            question.contains("成绩") ||
                            question.contains("学分") ||
                            question.contains("挂科") ||
                            question.contains("考勤") ||
                            question.contains("缺勤") ||
                            question.contains("规划") ||
                            question.contains("建议")
            );

            if (needContext) {
                finalPrompt.append("【系统背景数据 - 请基于此数据回答用户】：\n");

                // --- A. 查成绩 ---
                // 注意：需要确保 ScoreRepository 中有 findByStudentId 方法
                List<Score> scores = scoreRepo.findByStudentId(studentId);
                if (!scores.isEmpty()) {
                    finalPrompt.append("该学生的成绩单：[");
                    for (Score s : scores) {
                        finalPrompt.append(s.getSubject()).append(":").append(s.getScores()).append("分; ");
                    }
                    finalPrompt.append("]\n");
                } else {
                    finalPrompt.append("该学生暂无成绩记录。\n");
                }

                // --- B. 查考勤 ---
                // 注意：需要确保 AttendenceRepository 中有 findByStudentId 方法
                List<Attendence> atts = attendenceRepo.findByStudentId(studentId);
                if (!atts.isEmpty()) {
                    finalPrompt.append("该学生的考勤记录：[");
                    for (Attendence a : atts) {
                        // 假设考勤表里有 getDate() 和 getStatus()
                        finalPrompt.append(a.getTime()).append(" ").append(a.getSubjectName()).append("; ");
                    }
                    finalPrompt.append("]\n");
                }

                finalPrompt.append("【指令】：你是该学生的智能学业导师。请根据上述真实数据回答他的问题。如果成绩有挂科(<60)，请给予警示和复习建议；如果考勤不好，请提醒他去上课。\n");
                finalPrompt.append("----------------\n");
            }

            // 3. 加上用户原本的问题
            finalPrompt.append("用户问题：").append(question);

            // 4. 发起请求 (使用免费且速度快的 ERNIE-Speed-128K)
            ChatResponse response = qianfan.chatCompletion()
                    .model("ERNIE-Speed-128K")
                    .addMessage("user", finalPrompt.toString()) // 把拼接好的长文本发给 AI
                    .execute();

            return response.getResult();

        } catch (Exception e) {
            e.printStackTrace();
            return "AI 服务暂时掉线了: " + e.getMessage();
        }
    }
}