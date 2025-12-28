<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">🎓</div>
        <div class="logo-text">学生中心</div>
      </div>
      <nav>
        <div :class="['nav-item', currentTab === 'profile' ? 'active' : '']" @click="currentTab = 'profile'">
          <span class="icon">👤</span> 个人信息
        </div>
        <div :class="['nav-item', currentTab === 'courses' ? 'active' : '']" @click="currentTab = 'courses'">
          <span class="icon">📚</span> 选课信息
        </div>
        <div :class="['nav-item', currentTab === 'scores' ? 'active' : '']" @click="currentTab = 'scores'">
          <span class="icon">📊</span> 成绩查询
        </div>
        <div :class="['nav-item', currentTab === 'messages' ? 'active' : '']" @click="currentTab = 'messages'">
          <span class="icon">🔔</span> 消息中心
          <span class="badge" v-if="messageList.length + warningList.length > 0">{{ messageList.length + warningList.length }}</span>
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="breadcrumb">首页 / {{ getTabName(currentTab) }}</div>
        <div class="user-area">
          <span class="welcome">欢迎，<strong>{{ studentInfo.studentName }}</strong></span>
          <button class="logout-btn" @click="logout">退出</button>
        </div>
      </header>

      <div class="content-body">

        <div v-if="currentTab === 'profile'" class="fade-in">
          <div class="profile-layout">
            <div class="card progress-card">
              <h3>学业进度概览</h3>
              <div class="progress-container">
                <div class="progress-ring" :style="{ '--progress': progressPercentage + '%' }">
                  <div class="inner-circle">
                    <span class="score-text">{{ obtainedCredits }}</span>
                    <span class="label-text">已修学分</span>
                  </div>
                </div>
                <div class="progress-info">
                  <p>总学分要求：<strong>160</strong></p>
                  <p>当前完成度：<strong>{{ progressPercentage.toFixed(1) }}%</strong></p>
                  <div class="progress-bar-bg">
                    <div class="progress-bar-fill" :style="{ width: progressPercentage + '%' }"></div>
                  </div>
                  <p class="tip" v-if="obtainedCredits < 160">加油，还差 {{ (160 - obtainedCredits).toFixed(1) }} 学分！</p>
                  <p class="tip success" v-else>🎉 恭喜，学分已修满！</p>
                  <button class="detail-link-btn" @click="showCreditModal = true">📄 查看明细</button>
                </div>
              </div>
            </div>

            <div class="card info-card">
              <div class="card-header">
                <h3>基本资料</h3>
                <button class="save-btn" @click="updateProfile">保存修改</button>
              </div>
              <div class="info-grid">
                <div class="info-item">
                  <label>学号</label>
                  <span>{{ studentInfo.studentId }}</span>
                </div>
                <div class="info-item">
                  <label>姓名</label>
                  <span>{{ studentInfo.studentName }}</span>
                </div>
                <div class="info-item">
                  <label>性别</label>
                  <span>{{ studentInfo.gender }}</span>
                </div>
                <div class="info-item">
                  <label>班级</label>
                  <span>{{ studentInfo.className }}</span>
                </div>
                <div class="info-item full-width">
                  <label>手机号码</label>
                  <input v-model="studentInfo.phone" type="text" class="input-field">
                </div>
                <div class="info-item full-width">
                  <label>电子邮箱</label>
                  <input v-model="studentInfo.email" type="text" class="input-field">
                </div>
              </div>
            </div>
          </div>

          <div class="card mt-4">
            <h3>安全设置</h3>
            <div class="password-form">
              <div class="form-group">
                <label>当前密码</label>
                <div class="pwd-wrapper">
                  <input
                    v-model="pwdForm.oldPassword"
                    :type="showOldPwd ? 'text' : 'password'"
                    class="input-field big-input"
                  >
                  <span class="eye-icon" @click="showOldPwd = !showOldPwd" title="显示/隐藏密码">
                    {{ showOldPwd ? '🙈' : '👁️' }}
                  </span>
                </div>
              </div>
              <div class="form-group">
                <label>设置新密码</label>
                <div class="pwd-wrapper">
                  <input
                    v-model="pwdForm.newPassword"
                    :type="showNewPwd ? 'text' : 'password'"
                    class="input-field big-input"
                  >
                  <span class="eye-icon" @click="showNewPwd = !showNewPwd" title="显示/隐藏密码">
                    {{ showNewPwd ? '🙈' : '👁️' }}
                  </span>
                </div>
              </div>
              <div class="form-actions">
                <button class="warn-btn big-btn" @click="changePassword">确认修改</button>
              </div>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'courses'" class="fade-in">
          <div class="card">
            <div class="card-header">
              <h3>我的选课</h3>
              <button class="primary-btn icon-btn-text" @click="openCourseSelection">
                ➕ 选课中心
              </button>
            </div>
            <div class="table-responsive">
              <table class="modern-table">
                <thead><tr><th>课程名称</th><th>任课教师</th><th>状态</th></tr></thead>
                <tbody>
                <tr v-for="c in courseList" :key="c.id">
                  <td><span class="course-tag">{{ c.subject }}</span></td>
                  <td>{{ c.teaName }}</td>
                  <td><span class="status-badge success">已选</span></td>
                </tr>
                </tbody>
              </table>
              <div v-if="courseList.length === 0" class="empty-state">📭 暂无选课记录</div>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'scores'" class="fade-in">
          <div class="card">
            <h3>成绩单</h3>
            <div class="table-responsive">
              <table class="modern-table">
                <thead><tr><th>科目</th><th>学分</th><th>分数</th><th>教师</th><th>绩点</th></tr></thead>
                <tbody>
                <tr v-for="s in scoreList" :key="s.id">
                  <td><b>{{ s.subject }}</b></td>
                  <td>{{ s.credit }}</td>
                  <td><span :class="['score-badge', s.scores >= 60 ? 'pass' : 'fail']">{{ s.scores }}</span></td>
                  <td>{{ s.teaName }}</td>
                  <td>{{ calculateGPA(s.scores) }}</td>
                </tr>
                </tbody>
              </table>
              <div v-if="scoreList.length === 0" class="empty-state">📭 暂无成绩记录</div>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'messages'" class="fade-in">
          <div class="toolbar">
            <h2>消息中心</h2>
            <button @click="loadAllMessages(false)" class="refresh-btn">🔄 刷新</button>
          </div>
          <div class="card mb-4">
            <h3 class="section-header blue-header">📢 系统通知</h3>
            <table class="modern-table">
              <thead><tr><th width="20%">标题</th><th>内容</th><th width="20%">时间</th><th width="10%">操作</th></tr></thead>
              <tbody>
              <tr v-for="m in messageList" :key="'m'+m.id">
                <td>{{ m.title }}</td>
                <td class="text-muted">{{ m.description }}</td>
                <td class="text-small">{{ formatDate(m.sendTime) }}</td>
                <td><button class="icon-btn delete" @click="deleteItem('message', m.id)">🗑️</button></td>
              </tr>
              </tbody>
            </table>
            <div v-if="messageList.length === 0" class="empty-state small">暂无系统通知</div>
          </div>
          <div class="card">
            <h3 class="section-header red-header">⚠️ 学业预警</h3>
            <table class="modern-table">
              <thead><tr><th width="20%">标题</th><th width="20%">时间</th><th>操作</th></tr></thead>
              <tbody>
              <tr v-for="w in warningList" :key="'w'+w.id">
                <td class="text-danger fw-bold">{{ w.title }}</td>
                <td class="text-small">{{ formatDate(w.sendTime) }}</td>
                <td>
                  <button class="action-btn-sm" @click="showDetail(w)">查看详情</button>
                  <button class="icon-btn delete" @click="deleteItem('warning', w.id)">🗑️</button>
                </td>
              </tr>
              </tbody>
            </table>
            <div v-if="warningList.length === 0" class="empty-state small">暂无预警信息</div>
          </div>
        </div>

      </div>
    </main>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal fade-in-up">
        <div class="modal-header"><h3>{{ currentDetail.title }}</h3><button class="close-btn" @click="showModal = false">×</button></div>
        <div class="modal-body"><p class="time">📅 {{ formatDate(currentDetail.sendTime) }}</p><div class="message-content">{{ currentDetail.text }}</div></div>
        <div class="modal-footer"><button class="primary-btn" @click="showModal = false">我知道了</button></div>
      </div>
    </div>

    <div v-if="showCreditModal" class="modal-overlay" @click.self="showCreditModal = false">
      <div class="modal fade-in-up" style="width: 800px; max-width: 95%;">
        <div class="modal-header"><h3>🎓 学分修读明细</h3><button class="close-btn" @click="showCreditModal = false">×</button></div>
        <div class="modal-body">
          <div class="table-responsive">
            <table class="modern-table">
              <thead><tr><th>科目名称</th><th>学分</th><th>成绩</th><th>状态</th></tr></thead>
              <tbody>
              <tr v-for="s in scoreList" :key="'cr'+s.id">
                <td class="fw-bold">{{ s.subject }}</td><td>{{ s.credit }}</td>
                <td><span :class="['score-badge', s.scores >= 60 ? 'pass' : 'fail']">{{ s.scores }}</span></td>
                <td><span v-if="s.scores >= 60" class="status-text success">✅ 已获取</span><span v-else class="status-text danger">❌ 未获取</span></td>
              </tr>
              </tbody>
            </table>
          </div>
        </div>
        <div class="modal-footer footer-split">
          <div class="footer-info">已获取学分：<strong class="text-primary">{{ obtainedCredits }}</strong> / 160</div>
          <button class="primary-btn" @click="showCreditModal = false">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="showSelectCourseModal" class="modal-overlay" @click.self="showSelectCourseModal = false">
      <div class="modal fade-in-up" style="width: 700px; max-width: 95%;">
        <div class="modal-header">
          <h3>📖 选课中心 (本学院课程)</h3>
          <button class="close-btn" @click="showSelectCourseModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="table-responsive">
            <table class="modern-table">
              <thead><tr><th>课程名称</th><th>授课教师</th><th>操作</th></tr></thead>
              <tbody>
              <tr v-for="c in availableCourses" :key="c.id">
                <td class="fw-bold">{{ c.subject }}</td>
                <td>{{ c.teaName }}</td>
                <td>
                  <button
                    v-if="isEnrolled(c)"
                    class="danger-btn-sm"
                    @click="dropCourse(c)">
                    退课
                  </button>
                  <button
                    v-else
                    class="primary-btn-sm"
                    @click="enrollCourse(c)">
                    选课
                  </button>
                </td>
              </tr>
              </tbody>
            </table>
            <div v-if="availableCourses.length === 0" class="empty-state">当前暂无可选课程</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="primary-btn" @click="showSelectCourseModal = false">关闭</button>
        </div>
      </div>
    </div>

    <div v-if="showAutoPopup" class="modal-overlay">
      <div class="modal alert-modal fade-in-up">
        <div class="alert-banner"><div class="alert-icon-wrapper">⚠️</div></div>
        <div class="alert-body">
          <h3 class="alert-main-title">新预警提醒</h3>
          <p class="alert-subtitle">系统检测到您近期有新的学业预警，请务必重视：</p>
          <div class="alert-content-box">
            <div class="warning-level-tag">{{ autoPopupData.title }}</div>
            <div class="warning-text">{{ autoPopupData.text }}</div>
          </div>
          <button class="confirm-btn large-btn" @click="showAutoPopup = false">我已知晓，定当努力</button>
        </div>
      </div>
    </div>

    <div class="ai-float-btn" @click="toggleAiChat" title="智能学业助手">
      <span class="ai-emoji">🤖</span>
    </div>

    <transition name="fade-up">
      <div v-if="showAiChat" class="ai-chat-window">
        <div class="ai-header">
          <div class="ai-title"><span>✨ 智能学业助手</span><span class="status-dot"></span></div>
          <button class="ai-close" @click="showAiChat = false">×</button>
        </div>
        <div class="ai-body" ref="chatBodyRef">
          <div v-for="(msg, index) in aiMessages" :key="index" :class="['ai-msg-row', msg.role]">
            <div class="ai-avatar">{{ msg.role === 'user' ? '🧑‍🎓' : '🤖' }}</div>
            <div class="ai-bubble">{{ msg.text }}</div>
          </div>
          <div v-if="aiLoading" class="ai-msg-row ai">
            <div class="ai-avatar">🤖</div>
            <div class="ai-bubble loading-bubble"><span class="dot">.</span><span class="dot">.</span><span class="dot">.</span></div>
          </div>
        </div>
        <div class="ai-footer">
          <input v-model="aiInput" @keyup.enter="sendAiMessage" placeholder="问我关于学业、规划的问题..." class="ai-input" :disabled="aiLoading">
          <button class="ai-send-btn" @click="sendAiMessage" :disabled="aiLoading || !aiInput.trim()">发送</button>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, onMounted, computed, nextTick } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const token = localStorage.getItem('token');
const currentTab = ref('profile');

const studentInfo = ref({});
const pwdForm = ref({ oldPassword: '', newPassword: '' });
// 修改部分：添加显示状态变量
const showOldPwd = ref(false);
const showNewPwd = ref(false);

const courseList = ref([]);
const scoreList = ref([]);
const warningList = ref([]);
const messageList = ref([]);

const showModal = ref(false);
const showCreditModal = ref(false);
const showSelectCourseModal = ref(false); // 选课弹窗
const availableCourses = ref([]); // 可选课程数据

const currentDetail = ref({});
const showAutoPopup = ref(false);
const autoPopupData = ref({});

// === AI 助手状态 ===
const showAiChat = ref(false);
const aiInput = ref('');
const aiLoading = ref(false);
const chatBodyRef = ref(null);
const aiMessages = ref([
  { role: 'ai', text: '你好！我是你的智能学业助手。我了解你的成绩和考勤情况，有什么可以帮你规划的吗？' }
]);

const api = axios.create({
  baseURL: 'http://localhost:8080/api/student',
  headers: { 'Authorization': `Bearer ${token}` }
});

const obtainedCredits = computed(() => {
  if (!scoreList.value || scoreList.value.length === 0) return 0;
  const sum = scoreList.value
    .filter(item => item.scores >= 60)
    .reduce((total, item) => total + item.credit, 0);
  return parseFloat(sum.toFixed(1));
});

const progressPercentage = computed(() => {
  const total = 160;
  let percent = (obtainedCredits.value / total) * 100;
  if (percent > 100) percent = 100;
  return percent;
});

const calculateGPA = (score) => {
  if (score >= 90) return '4.0';
  if (score >= 85) return '3.7';
  if (score >= 82) return '3.3';
  if (score >= 78) return '3.0';
  if (score >= 75) return '2.7';
  if (score >= 72) return '2.3';
  if (score >= 68) return '2.0';
  if (score >= 64) return '1.5';
  if (score >= 60) return '1.0';
  return '0.0';
};

const getTabName = (tab) => {
  const map = { 'profile': '个人信息', 'courses': '选课信息', 'scores': '成绩查询', 'messages': '消息中心' };
  return map[tab] || '首页';
};

// API Methods
const loadProfile = async () => {
  try {
    const res = await api.get('/info');
    studentInfo.value = res.data;
  } catch (e) { console.error(e); }
};

const updateProfile = async () => {
  try {
    await api.put('/update', studentInfo.value);
    alert("保存成功");
  } catch (e) { alert("保存失败"); }
};

const changePassword = async () => {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) return alert("请输入密码");
  try {
    await api.post('/change-password', pwdForm.value);
    alert("密码修改成功，请重新登录");
    logout();
  } catch (e) { alert(e.response?.data || "修改失败"); }
};

const loadCourses = async () => {
  const res = await api.get('/courses');
  courseList.value = res.data;
};

// === 选课相关逻辑 ===
const openCourseSelection = async () => {
  try {
    const res = await api.get('/available-courses');
    availableCourses.value = res.data;
    showSelectCourseModal.value = true;
  } catch (e) {
    alert("获取可选课程列表失败");
  }
};

// 判断该课程是否已选
const isEnrolled = (course) => {
  // 对比科目名称和老师姓名
  return courseList.value.some(my => my.subject === course.subject && my.teaName === course.teaName);
};

const enrollCourse = async (course) => {
  if(!confirm(`确定要选修【${course.subject}】吗？`)) return;
  try {
    await api.post('/course/enroll', { subject: course.subject, teaName: course.teaName });
    alert("选课成功！");
    loadCourses(); // 刷新已选列表
  } catch (e) {
    alert(e.response?.data || "选课失败");
  }
};

const dropCourse = async (course) => {
  if(!confirm(`确定要退掉【${course.subject}】吗？`)) return;
  try {
    await api.post('/course/drop', { subject: course.subject, teaName: course.teaName });
    alert("退课成功！");
    loadCourses(); // 刷新已选列表
  } catch (e) {
    alert(e.response?.data || "退课失败"); // 后端会返回“已有成绩无法退课”
  }
};

const loadScores = async () => {
  const res = await api.get('/scores');
  scoreList.value = res.data;
};

const loadAllMessages = async (isLogin = false) => {
  const resW = await api.get('/warnings');
  warningList.value = resW.data;
  if (isLogin) checkRecentWarnings(resW.data);
  const resM = await api.get('/messages');
  messageList.value = resM.data;
};

const deleteItem = async (type, id) => {
  if(!confirm("确定删除吗？")) return;
  try {
    if(type === 'warning') await api.delete(`/warnings/${id}`);
    else await api.delete(`/messages/${id}`);
    loadAllMessages();
  } catch(e) { alert("删除失败"); }
};

const checkRecentWarnings = (list) => {
  if (!list || list.length === 0) return;
  const now = new Date();
  const threeDaysAgo = new Date();
  threeDaysAgo.setDate(now.getDate() - 3);
  const recent = list.find(w => new Date(w.sendTime) >= threeDaysAgo);
  if (recent) {
    autoPopupData.value = recent;
    showAutoPopup.value = true;
  }
};

const showDetail = (item) => {
  currentDetail.value = item;
  showModal.value = true;
};

// === AI 助手逻辑 ===
const toggleAiChat = () => {
  showAiChat.value = !showAiChat.value;
  if(showAiChat.value) scrollToBottom();
};

const scrollToBottom = () => {
  nextTick(() => {
    if (chatBodyRef.value) {
      chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
  });
};

const sendAiMessage = async () => {
  const question = aiInput.value.trim();
  if (!question) return;

  aiMessages.value.push({ role: 'user', text: question });
  aiInput.value = '';
  aiLoading.value = true;
  scrollToBottom();

  try {
    const sid = studentInfo.value.studentId;
    const res = await axios.get('http://localhost:8080/api/ai/chat', {
      params: {
        question: question,
        studentId: sid
      }
    });
    aiMessages.value.push({ role: 'ai', text: res.data });
  } catch (e) {
    console.error("AI Error:", e);
    aiMessages.value.push({ role: 'ai', text: "AI 连接失败，请检查网络或后端控制台。" });
  } finally {
    aiLoading.value = false;
    scrollToBottom();
  }
};

const logout = () => { localStorage.clear(); router.push('/'); };
const formatDate = (t) => t ? new Date(t).toLocaleString() : '';

onMounted(() => {
  if (!token) { router.push('/'); return; }
  loadProfile();
  loadCourses();
  loadScores();
  loadAllMessages(true);
});
</script>

<style scoped>
/* 全局布局 */
.dashboard-container { display: flex; height: 100vh; font-family: 'Inter', sans-serif; background-color: #f3f4f6; color: #333; }
.sidebar { width: 240px; background: linear-gradient(180deg, #1f2937 0%, #111827 100%); color: white; display: flex; flex-direction: column; box-shadow: 4px 0 10px rgba(0,0,0,0.1); }
.logo-area { height: 80px; display: flex; align-items: center; justify-content: center; gap: 10px; border-bottom: 1px solid rgba(255,255,255,0.1); }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 20px; font-weight: bold; }
.nav-item { padding: 16px 24px; cursor: pointer; transition: 0.3s; display: flex; align-items: center; gap: 12px; font-size: 15px; border-left: 4px solid transparent; }
.nav-item:hover { background-color: rgba(255,255,255,0.05); }
.nav-item.active { background-color: rgba(255,255,255,0.1); border-left-color: #3b82f6; font-weight: 500; }
.badge { background-color: #ef4444; color: white; padding: 2px 8px; border-radius: 10px; font-size: 12px; margin-left: auto; }

.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.header { height: 64px; background: white; padding: 0 30px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 2px 4px rgba(0,0,0,0.05); z-index: 10; }
.breadcrumb { color: #6b7280; font-size: 14px; }
.user-area { display: flex; align-items: center; gap: 20px; }
.logout-btn { background: white; border: 1px solid #e5e7eb; color: #ef4444; padding: 6px 16px; border-radius: 6px; cursor: pointer; }
.logout-btn:hover { background: #fef2f2; border-color: #fecaca; }

.content-body { padding: 30px; overflow-y: auto; flex: 1; }
.card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); margin-bottom: 24px; }
.card h3 { margin: 0 0 20px 0; font-size: 18px; color: #111827; border-left: 4px solid #3b82f6; padding-left: 12px; }

.profile-layout { display: grid; grid-template-columns: 350px 1fr; gap: 24px; }
.progress-container { display: flex; flex-direction: column; align-items: center; gap: 20px; padding: 20px 0; }
.progress-ring { width: 140px; height: 140px; border-radius: 50%; background: conic-gradient(#3b82f6 var(--progress, 0%), #e5e7eb 0deg); display: flex; justify-content: center; align-items: center; position: relative; }
.progress-ring::before { content: ""; position: absolute; width: 110px; height: 110px; background: white; border-radius: 50%; }
.inner-circle { position: relative; z-index: 1; display: flex; flex-direction: column; align-items: center; }
.score-text { font-size: 28px; font-weight: 800; color: #3b82f6; }
.label-text { font-size: 12px; color: #9ca3af; }
.detail-link-btn { margin-top: 15px; background: white; border: 1px solid #3b82f6; color: #3b82f6; padding: 6px 16px; border-radius: 20px; cursor: pointer; font-size: 13px; }
.detail-link-btn:hover { background: #eff6ff; }

.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.full-width { grid-column: span 2; }
.info-item { display: flex; flex-direction: column; gap: 6px; }
.info-item label { color: #6b7280; font-size: 13px; }
.info-item span { font-weight: 500; font-size: 15px; color: #111827; }
.input-field { padding: 10px; border: 1px solid #d1d5db; border-radius: 6px; font-size: 14px; }
.input-field:focus { outline: none; border-color: #3b82f6; }
.card-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.save-btn { background: #10b981; color: white; border: none; padding: 8px 16px; border-radius: 6px; cursor: pointer; }

/* 安全设置样式优化 */
.password-form { display: grid; grid-template-columns: 1fr 1fr auto; gap: 20px; align-items: end; }
.form-group { display: flex; flex-direction: column; gap: 8px; }
/* 包装器用于定位图标 */
.pwd-wrapper { position: relative; display: flex; align-items: center; }
.big-input { height: 42px; padding: 0 40px 0 15px; width: 100%; box-sizing: border-box; } /* 增加右侧内边距 */
.eye-icon {
  position: absolute; right: 10px; cursor: pointer;
  user-select: none; font-size: 18px; color: #9ca3af;
  transition: color 0.2s;
}
.eye-icon:hover { color: #3b82f6; }

.big-btn { height: 42px; padding: 0 24px; border-radius: 6px; background: #f59e0b; color: white; border: none; cursor: pointer;}

.modern-table { width: 100%; border-collapse: separate; border-spacing: 0; }
.modern-table th { background: #f9fafb; color: #374151; font-weight: 600; padding: 12px 16px; text-align: left; border-bottom: 1px solid #e5e7eb; }
.modern-table td { padding: 14px 16px; border-bottom: 1px solid #f3f4f6; color: #4b5563; }
.course-tag { background: #eff6ff; color: #3b82f6; padding: 4px 8px; border-radius: 4px; font-size: 13px; }
.status-badge { padding: 4px 10px; border-radius: 12px; font-size: 12px; }
.status-badge.success { background: #d1fae5; color: #065f46; }
.score-badge { display: inline-block; width: 32px; height: 32px; line-height: 32px; text-align: center; border-radius: 50%; font-weight: bold; font-size: 13px; }
.score-badge.pass { background: #dcfce7; color: #059669; }
.score-badge.fail { background: #fee2e2; color: #b91c1c; }

.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); backdrop-filter: blur(2px); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal { background: white; width: 500px; max-width: 90%; max-height: 85vh; border-radius: 12px; display: flex; flex-direction: column; overflow: hidden; }
.modal-header { padding: 16px 24px; border-bottom: 1px solid #f3f4f6; display: flex; justify-content: space-between; align-items: center; flex-shrink: 0; }
.modal-body { padding: 24px; overflow-y: auto; flex: 1; }
.modal-footer { padding: 16px 24px; background: #f9fafb; text-align: right; flex-shrink: 0; }
.primary-btn { background: #3b82f6; color: white; border: none; padding: 8px 20px; border-radius: 6px; cursor: pointer; }
.footer-split { display: flex; justify-content: space-between; align-items: center; }
.status-text { font-size: 13px; font-weight: 500; }
.status-text.success { color: #166534; }
.status-text.danger { color: #dc2626; }

/* === AI Widget Styles === */
.ai-float-btn {
  position: fixed; bottom: 30px; right: 30px;
  width: 60px; height: 60px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 50%;
  display: flex; justify-content: center; align-items: center;
  box-shadow: 0 4px 15px rgba(118, 75, 162, 0.4);
  cursor: pointer; z-index: 2000;
  transition: transform 0.3s;
  animation: pulse 2s infinite;
}
.ai-float-btn:hover { transform: scale(1.1); }
.ai-emoji { font-size: 30px; }

@keyframes pulse {
  0% { box-shadow: 0 0 0 0 rgba(118, 75, 162, 0.7); }
  70% { box-shadow: 0 0 0 15px rgba(118, 75, 162, 0); }
  100% { box-shadow: 0 0 0 0 rgba(118, 75, 162, 0); }
}

.ai-chat-window {
  position: fixed; bottom: 100px; right: 30px;
  width: 350px; height: 500px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 30px rgba(0,0,0,0.15);
  display: flex; flex-direction: column;
  z-index: 2000;
  overflow: hidden;
  border: 1px solid #eee;
}
.ai-header {
  padding: 15px; background: linear-gradient(135deg, #667eea, #764ba2);
  color: white; display: flex; justify-content: space-between; align-items: center;
}
.ai-title { font-weight: bold; display: flex; align-items: center; gap: 8px; }
.status-dot { width: 8px; height: 8px; background: #4ade80; border-radius: 50%; }
.ai-close { background: none; border: none; color: white; font-size: 20px; cursor: pointer; }

.ai-body {
  flex: 1; padding: 15px; overflow-y: auto; background: #f9fafb;
  display: flex; flex-direction: column; gap: 12px;
}
.ai-msg-row { display: flex; gap: 10px; max-width: 85%; }
.ai-msg-row.user { align-self: flex-end; flex-direction: row-reverse; }
.ai-avatar { font-size: 20px; background: #fff; width: 32px; height: 32px; border-radius: 50%; display: flex; align-items: center; justify-content: center; box-shadow: 0 2px 4px rgba(0,0,0,0.05); }
.ai-bubble {
  padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5; box-shadow: 0 1px 2px rgba(0,0,0,0.05);
}
.ai-msg-row.ai .ai-bubble { background: white; color: #333; border-top-left-radius: 2px; }
.ai-msg-row.user .ai-bubble { background: #667eea; color: white; border-top-right-radius: 2px; }

.loading-bubble .dot { animation: dotBlink 1.4s infinite; opacity: 0; font-weight: bold; font-size: 16px;}
.loading-bubble .dot:nth-child(1) { animation-delay: 0s; }
.loading-bubble .dot:nth-child(2) { animation-delay: 0.2s; }
.loading-bubble .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes dotBlink { 0% { opacity: 0; } 50% { opacity: 1; } 100% { opacity: 0; } }

.ai-footer { padding: 12px; background: white; border-top: 1px solid #eee; display: flex; gap: 8px; }
.ai-input { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 20px; outline: none; }
.ai-input:focus { border-color: #667eea; }
.ai-send-btn { background: #667eea; color: white; border: none; padding: 6px 16px; border-radius: 20px; cursor: pointer; font-size: 13px; }
.ai-send-btn:disabled { background: #ccc; cursor: not-allowed; }

.fade-up-enter-active, .fade-up-leave-active { transition: all 0.3s ease; }
.fade-up-enter-from, .fade-up-leave-to { opacity: 0; transform: translateY(20px); }

/* === 优化后的预警弹窗样式 === */
.alert-modal {
  width: 600px !important; /* 加宽 */
  max-width: 90%;
  overflow: hidden;
  border-radius: 16px;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.25);
  display: flex;
  flex-direction: column;
}
.alert-banner {
  height: 100px;
  background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
  display: flex; align-items: center; justify-content: center; position: relative;
}
.alert-icon-wrapper {
  font-size: 56px; background: white; width: 80px; height: 80px;
  border-radius: 50%; display: flex; align-items: center; justify-content: center;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05); position: absolute; bottom: -40px;
}
.alert-body { padding: 50px 40px 30px; text-align: center; }
.alert-main-title { font-size: 24px; color: #1f2937; margin: 10px 0 5px; font-weight: 700; }
.alert-subtitle { color: #6b7280; font-size: 14px; margin-bottom: 20px; }
.alert-content-box {
  background: #fff1f2; border: 1px solid #ffe4e6; border-radius: 12px;
  padding: 20px; margin-bottom: 25px; text-align: left;
}
.warning-level-tag {
  display: inline-block; background: #ef4444; color: white; padding: 4px 12px;
  border-radius: 6px; font-size: 14px; font-weight: bold; margin-bottom: 12px;
}
.warning-text { color: #7f1d1d; font-size: 15px; line-height: 1.6; text-align: justify; }
.confirm-btn.large-btn {
  width: 100%; padding: 12px; font-size: 16px;
  background: linear-gradient(to right, #ef4444, #dc2626); color: white; border: none;
  border-radius: 6px; cursor: pointer; transition: transform 0.1s ease;
}
.confirm-btn.large-btn:active { transform: scale(0.98); }

/* 选课弹窗专用样式 */
.primary-btn-sm {
  background: #3b82f6; color: white; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-size: 13px;
}
.danger-btn-sm {
  background: #ef4444; color: white; border: none; padding: 4px 12px; border-radius: 4px; cursor: pointer; font-size: 13px;
}
.icon-btn-text {
  font-size: 14px; display: flex; align-items: center; gap: 5px;
}

/* 响应式 */
@media (max-width: 1000px) {
  .profile-layout { grid-template-columns: 1fr; }
  .password-form { grid-template-columns: 1fr; }
  .form-actions { justify-content: flex-start; }
}

/* === 新增：隐藏浏览器默认的密码查看图标 === */

/* 针对 Chrome, Safari, Edge (Chromium内核) */
.input-field::-webkit-password-reveal-button,
.input-field::-webkit-credentials-auto-fill-button {
  display: none !important;
  -webkit-appearance: none;
}

/* 针对旧版 Edge 和 IE */
.input-field::-ms-reveal,
.input-field::-ms-clear {
  display: none !important;
}
</style>
