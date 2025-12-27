<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo">学生个人中心</div>
      <nav>
        <div :class="['nav-item', currentTab === 'profile' ? 'active' : '']" @click="currentTab = 'profile'">
          个人信息
        </div>
        <div :class="['nav-item', currentTab === 'courses' ? 'active' : '']" @click="currentTab = 'courses'">
          选课信息
        </div>
        <div :class="['nav-item', currentTab === 'scores' ? 'active' : '']" @click="currentTab = 'scores'">
          成绩查询
        </div>
        <div :class="['nav-item', currentTab === 'messages' ? 'active' : '']" @click="currentTab = 'messages'">
          消息中心
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <span>欢迎您，{{ studentInfo.studentName }} 同学</span>
        <button class="logout-btn" @click="logout">退出</button>
      </header>

      <div class="content-body">

        <div v-if="currentTab === 'profile'" class="profile-section">
          <h2>个人信息概览</h2>
          <div class="credit-progress-card">
            <div class="progress-ring" :style="{ '--progress': progressPercentage + '%' }">
              <div class="inner-circle">
                <span class="score-text">{{ obtainedCredits }}</span>
                <span class="label-text">已修学分</span>
              </div>
            </div>
            <div class="progress-info">
              <h3>学业进度</h3>
              <p>目标学分：<strong>160</strong></p>
              <p>当前进度：<strong>{{ progressPercentage.toFixed(1) }}%</strong></p>
              <p class="tip" v-if="obtainedCredits < 160">加油，还差 {{ (160 - obtainedCredits).toFixed(1) }} 学分！</p>
              <p class="tip success" v-else>恭喜，学分已修满！</p>
            </div>
          </div>
          <div class="card">
            <div class="info-grid">
              <p><strong>学号：</strong>{{ studentInfo.studentId }}</p>
              <p><strong>姓名：</strong>{{ studentInfo.studentName }}</p>
              <p><strong>性别：</strong>{{ studentInfo.gender }}</p>
              <p><strong>班级：</strong>{{ studentInfo.className }}</p>
              <p><strong>年级：</strong>{{ studentInfo.grade }}</p>
            </div>
            <div class="edit-area">
              <div class="form-item">
                <label>手机：</label>
                <input v-model="studentInfo.phone" type="text">
              </div>
              <div class="form-item">
                <label>邮箱：</label>
                <input v-model="studentInfo.email" type="text">
              </div>
              <button class="save-btn" @click="updateProfile">保存修改</button>
            </div>
          </div>
          <h3>修改密码</h3>
          <div class="card">
            <div class="form-item">
              <label>原密码：</label>
              <input v-model="pwdForm.oldPassword" type="password">
            </div>
            <div class="form-item">
              <label>新密码：</label>
              <input v-model="pwdForm.newPassword" type="password">
            </div>
            <button class="warn-btn" @click="changePassword">确认修改</button>
          </div>
        </div>

        <div v-if="currentTab === 'courses'">
          <h2>我的选课</h2>
          <table class="data-table">
            <thead><tr><th>课程名称</th><th>任课教师</th></tr></thead>
            <tbody>
            <tr v-for="c in courseList" :key="c.id">
              <td>{{ c.subject }}</td>
              <td>{{ c.teaName }}</td>
            </tr>
            </tbody>
          </table>
          <p v-if="courseList.length === 0" class="no-data">暂无选课记录</p>
        </div>

        <div v-if="currentTab === 'scores'">
          <h2>我的成绩</h2>
          <table class="data-table">
            <thead><tr><th>科目</th><th>学分</th><th>分数</th><th>教师</th></tr></thead>
            <tbody>
            <tr v-for="s in scoreList" :key="s.id">
              <td>{{ s.subject }}</td>
              <td>{{ s.credit }}</td>
              <td :class="{'fail-score': s.scores < 60}">{{ s.scores }}</td>
              <td>{{ s.teaName }}</td>
            </tr>
            </tbody>
          </table>
          <p v-if="scoreList.length === 0" class="no-data">暂无成绩记录</p>
        </div>

        <div v-if="currentTab === 'messages'">
          <h2>消息中心</h2>
          <div class="toolbar">
            <button @click="loadAllMessages" class="refresh-btn">刷新消息</button>
          </div>

          <div class="section-title">系统通知</div>
          <table class="data-table">
            <thead><tr><th>标题</th><th>内容</th><th>发送时间</th><th>操作</th></tr></thead>
            <tbody>
            <tr v-for="m in messageList" :key="'m'+m.id">
              <td>{{ m.title }}</td>
              <td>{{ m.description }}</td>
              <td>{{ formatDate(m.sendTime) }}</td>
              <td><button class="del-btn" @click="deleteItem('message', m.id)">删除</button></td>
            </tr>
            </tbody>
          </table>
          <p v-if="messageList.length === 0" class="no-data">暂无系统通知</p>

          <br/>

          <div class="section-title" style="color:red">学业预警</div>
          <table class="data-table">
            <thead><tr><th>标题</th><th>发送时间</th><th>操作</th></tr></thead>
            <tbody>
            <tr v-for="w in warningList" :key="'w'+w.id">
              <td style="color:red; font-weight:bold;">{{ w.title }}</td>
              <td>{{ formatDate(w.sendTime) }}</td>
              <td>
                <button class="view-btn" @click="showDetail(w)">查看详情</button>
                <button class="del-btn" @click="deleteItem('warning', w.id)">删除</button>
              </td>
            </tr>
            </tbody>
          </table>
          <p v-if="warningList.length === 0" class="no-data">暂无预警信息</p>
        </div>

      </div>
    </main>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <h3>{{ currentDetail.title }}</h3>
        <p class="time">时间：{{ formatDate(currentDetail.sendTime) }}</p>
        <div class="modal-content">
          {{ currentDetail.text }}
        </div>
        <button @click="showModal = false">关闭</button>
      </div>
    </div>

    <div v-if="showAutoPopup" class="modal-overlay">
      <div class="modal alert-modal">
        <h3 style="color:red">⚠️ 新预警提醒</h3>
        <p>系统检测到您近期有新的学业预警：</p>
        <div class="alert-content">
          <strong>{{ autoPopupData.title }}</strong><br>
          {{ autoPopupData.text }}
        </div>
        <button @click="showAutoPopup = false">我已知晓</button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const token = localStorage.getItem('token');
const currentTab = ref('profile');

const studentInfo = ref({});
const pwdForm = ref({ oldPassword: '', newPassword: '' });
const courseList = ref([]);
const scoreList = ref([]);
const warningList = ref([]);
const messageList = ref([]);

const showModal = ref(false);
const currentDetail = ref({});
const showAutoPopup = ref(false);
const autoPopupData = ref({});

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

// API 方法
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

const loadScores = async () => {
  const res = await api.get('/scores');
  scoreList.value = res.data;
};

const loadAllMessages = async () => {
  // 加载预警
  const resW = await api.get('/warnings');
  warningList.value = resW.data;
  checkRecentWarnings(resW.data);

  // 加载通知
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

  const recent = list.find(w => {
    const sendTime = new Date(w.sendTime);
    return sendTime >= threeDaysAgo;
  });

  if (recent) {
    autoPopupData.value = recent;
    showAutoPopup.value = true;
  }
};

const showDetail = (item) => {
  currentDetail.value = item;
  showModal.value = true;
};

const logout = () => { localStorage.clear(); router.push('/'); };
const formatDate = (t) => t ? new Date(t).toLocaleString() : '';

onMounted(() => {
  if (!token) { router.push('/'); return; }
  loadProfile();
  loadCourses();
  loadScores();
  loadAllMessages(); // 初始化加载消息
});
</script>

<style scoped>
.dashboard-container { display: flex; height: 100vh; font-family: 'Segoe UI', sans-serif; }
.sidebar { width: 220px; background: #001529; color: white; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; font-size: 18px; font-weight: bold; background: #002140; }
.nav-item { padding: 15px 20px; cursor: pointer; transition: 0.3s; }
.nav-item:hover, .nav-item.active { background: #1890ff; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f0f2f5; }
.header { height: 60px; background: white; padding: 0 20px; display: flex; justify-content: flex-end; align-items: center; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
.logout-btn { background: #ff4d4f; color: white; border: none; padding: 5px 15px; border-radius: 4px; cursor: pointer; margin-left: 15px; }

.content-body { padding: 20px; overflow-y: auto; flex: 1; }
.card { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 15px; margin-bottom: 20px; }
.form-item { margin-bottom: 10px; display: flex; align-items: center; }
.form-item label { width: 80px; font-weight: 500;}
.form-item input { padding: 8px; border: 1px solid #ddd; border-radius: 4px; flex: 1; max-width: 300px; }
.save-btn { background: #52c41a; color: white; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; }
.warn-btn { background: #faad14; color: white; border: none; padding: 8px 20px; border-radius: 4px; cursor: pointer; }
.refresh-btn { background: #722ed1; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; margin-bottom: 10px; }

.section-title { font-weight: bold; margin: 15px 0 10px 0; border-left: 4px solid #1890ff; padding-left: 10px; font-size: 16px; }
.data-table { width: 100%; border-collapse: collapse; background: white; }
.data-table th, .data-table td { padding: 12px; border-bottom: 1px solid #eee; text-align: left; }
.no-data { text-align: center; color: #999; margin-top: 20px; }
.view-btn { background: #1890ff; color: white; border: none; padding: 4px 10px; border-radius: 2px; cursor: pointer; margin-right: 5px;}
.del-btn { background: #ff4d4f; color: white; border: none; padding: 4px 10px; border-radius: 2px; cursor: pointer; }
.fail-score { color: red; font-weight: bold; }

.credit-progress-card { background: white; border-radius: 8px; padding: 20px; margin-bottom: 20px; display: flex; align-items: center; gap: 30px; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.progress-ring { width: 120px; height: 120px; border-radius: 50%; background: conic-gradient(#1890ff var(--progress), #f0f2f5 0deg); display: flex; justify-content: center; align-items: center; position: relative; }
.progress-ring::before { content: ""; position: absolute; width: 100px; height: 100px; background: white; border-radius: 50%; }
.inner-circle { position: relative; z-index: 1; display: flex; flex-direction: column; align-items: center; }
.score-text { font-size: 24px; font-weight: bold; color: #1890ff; }
.label-text { font-size: 12px; color: #666; }
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 999; }
.modal { background: white; padding: 25px; border-radius: 8px; width: 400px; max-width: 90%; }
.modal-content { background: #f9f9f9; padding: 15px; border-radius: 4px; margin: 15px 0; min-height: 80px; }
.alert-modal { border-top: 5px solid red; }
</style>
