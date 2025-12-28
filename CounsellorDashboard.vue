<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">💼</div>
        <div class="logo-text">辅导员工作台</div>
      </div>
      <nav>
        <div
          :class="['nav-item', currentTab === 'profile' ? 'active' : '']"
          @click="currentTab = 'profile'">
          <span class="icon">👤</span> 个人中心
        </div>
        <div
          :class="['nav-item', currentTab === 'warnings' ? 'active' : '']"
          @click="fetchWarnings(); currentTab = 'warnings'">
          <span class="icon">🔔</span> 学生预警
        </div>
        <div
          :class="['nav-item', currentTab === 'college-scores' ? 'active' : '']"
          @click="fetchCollegeScores(); currentTab = 'college-scores'">
          <span class="icon">📊</span> 学生成绩
        </div>
        <div
          :class="['nav-item', currentTab === 'stats' ? 'active' : '']"
          @click="fetchStats(); currentTab = 'stats'">
          <span class="icon">📈</span> 挂科统计
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="breadcrumb">首页 / {{ getTabName(currentTab) }}</div>
        <div class="user-area">
          <span class="welcome">欢迎您，<strong>{{ counsellorInfo.counsellorName }}</strong> 辅导员</span>
          <button class="logout-btn" @click="handleLogout">退出登录</button>
        </div>
      </header>

      <div class="content-body">

        <transition name="fade" mode="out-in">

          <div v-if="currentTab === 'profile'" key="profile">
            <div class="page-title">个人资料管理</div>

            <div class="profile-grid">
              <div class="card info-card">
                <div class="card-header">
                  <h3>📝 基本信息</h3>
                </div>
                <div class="form-grid">
                  <div class="form-item">
                    <label>工号</label>
                    <input type="text" v-model="counsellorInfo.counsellorId" disabled class="input-field disabled" />
                  </div>
                  <div class="form-item">
                    <label>姓名</label>
                    <input type="text" v-model="counsellorInfo.counsellorName" class="input-field" />
                  </div>
                  <div class="form-item">
                    <label>所属学院</label>
                    <input type="text" :value="getCollegeName(counsellorInfo.collegeId)" disabled class="input-field disabled" />
                  </div>
                  <div class="form-item">
                    <label>手机</label>
                    <input type="text" v-model="counsellorInfo.phone" class="input-field" />
                  </div>
                  <div class="form-item full-width">
                    <label>邮箱</label>
                    <input type="text" v-model="counsellorInfo.email" class="input-field full-width-input" />
                  </div>
                </div>
                <div class="card-footer">
                  <button class="btn primary-btn" @click="updateProfile">保存信息修改</button>
                </div>
              </div>

              <div class="card pwd-card">
                <div class="card-header">
                  <h3>🔒 安全设置</h3>
                </div>
                <div class="form-stack">
                  <div class="form-item">
                    <label>原密码</label>
                    <div class="pwd-wrapper">
                      <input
                        :type="showOldPwd ? 'text' : 'password'"
                        v-model="passwordForm.oldPassword"
                        placeholder="请输入原密码"
                        class="input-field pwd-input"
                      />
                      <span class="eye-icon" @click="showOldPwd = !showOldPwd" title="显示/隐藏密码">
                        {{ showOldPwd ? '🙈' : '👁️' }}
                      </span>
                    </div>
                  </div>
                  <div class="form-item">
                    <label>新密码</label>
                    <div class="pwd-wrapper">
                      <input
                        :type="showNewPwd ? 'text' : 'password'"
                        v-model="passwordForm.newPassword"
                        placeholder="请输入新密码"
                        class="input-field pwd-input"
                      />
                      <span class="eye-icon" @click="showNewPwd = !showNewPwd" title="显示/隐藏密码">
                        {{ showNewPwd ? '🙈' : '👁️' }}
                      </span>
                    </div>
                  </div>
                  <button class="btn warning-btn full-width-btn" @click="changePassword">确认修改密码</button>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="currentTab === 'warnings'" key="warnings">
            <div class="page-title">本院学生预警</div>
            <div class="toolbar-card">
              <div class="search-box">
                <input v-model="searchName" placeholder="输入学生姓名搜索..." class="search-input" />
                <button class="icon-btn search-btn" @click="fetchWarnings">🔍</button>
              </div>
              <button @click="triggerSystemUpdate" class="btn refresh-btn">
                <span>🔄</span> 刷新数据
              </button>
            </div>
            <div class="table-card">
              <table class="modern-table">
                <thead><tr><th>预警标题</th><th>学生姓名</th><th>学号</th><th>发送时间</th><th>内容摘要</th><th>操作</th></tr></thead>
                <tbody>
                <tr v-for="item in warningList" :key="item.id">
                  <td><span class="tag warning-tag">{{ item.title }}</span></td>
                  <td><span class="student-name">{{ item.studentName }}</span></td>
                  <td class="text-muted">{{ item.studentId }}</td>
                  <td>{{ formatDate(item.sendTime) }}</td>
                  <td class="text-truncate" :title="item.text">{{ item.text }}</td>
                  <td><button class="btn sm-btn outline-btn" @click="showDetail(item)">查看详情</button></td>
                </tr>
                </tbody>
              </table>
              <div v-if="warningList.length === 0" class="empty-state">📭 暂无预警信息</div>
            </div>
          </div>

          <div v-else-if="currentTab === 'college-scores'" key="college-scores">
            <div class="page-title">学生选课及成绩概括</div>
            <div class="toolbar-card">
              <div class="search-box">
                <input v-model="scoreSearch" placeholder="搜索科目或学生姓名..." class="search-input" />
              </div>
            </div>
            <div class="table-card">
              <table class="modern-table">
                <thead>
                <tr>
                  <th>学号</th>
                  <th>姓名</th>
                  <th>选修课程</th>
                  <th>任课教师</th>
                  <th>成绩</th> </tr>
                </thead>
                <tbody>
                <tr v-for="(item, index) in filteredScores" :key="index">
                  <td>{{ item.studentId }}</td>
                  <td class="student-name">{{ item.studentName }}</td>
                  <td><span class="tag course-tag">{{ item.subject }}</span></td>
                  <td>{{ item.teaName }}</td>
                  <td>
                      <span v-if="item.score !== null" :class="['score-badge', item.score >= 60 ? 'pass' : 'fail']">
                        {{ item.score }}
                      </span>
                    <span v-else class="status-wait">⏳ 成绩还未公布</span>
                  </td>
                </tr>
                </tbody>
              </table>
              <div v-if="filteredScores.length === 0" class="empty-state">📭 暂无相关数据</div>
            </div>
          </div>

          <div v-else-if="currentTab === 'stats'" key="stats">
            <div class="page-title">科目挂科率统计</div>
            <div class="stats-grid">
              <div v-for="(stat, idx) in statList" :key="idx" class="stat-card">
                <div class="stat-header">
                  <span class="subject-title">{{ stat.subject }}</span>
                  <span class="stat-icon">📉</span>
                </div>
                <div class="stat-body">
                  <div class="rate-box">
                    <span class="rate-value" :class="getRateColor(stat.rate)">{{ stat.rate }}%</span>
                    <span class="rate-label">挂科率</span>
                  </div>
                  <div class="count-info">
                    <p>选课人数: <strong>{{ stat.total }}</strong></p>
                    <p>挂科人数: <strong class="text-danger">{{ stat.failed }}</strong></p>
                  </div>
                </div>
              </div>
            </div>
            <div v-if="statList.length === 0" class="empty-state">暂无统计数据</div>
          </div>

        </transition>

      </div>
    </main>

    <div v-if="showModal" class="modal-overlay" @click.self="showModal = false">
      <div class="modal">
        <div class="modal-header"><h3>📌 预警详情</h3><button class="close-icon" @click="showModal = false">×</button></div>
        <div class="modal-body">
          <div class="detail-group">
            <div class="detail-row"><span class="label">标题：</span><span class="value title-value">{{ currentDetail.title }}</span></div>
            <div class="detail-row"><span class="label">学生：</span><span class="value">{{ currentDetail.studentName }} ({{ currentDetail.studentId }})</span></div>
            <div class="detail-row"><span class="label">时间：</span><span class="value">{{ formatDate(currentDetail.sendTime) }}</span></div>
          </div>
          <div class="detail-content"><p>{{ currentDetail.text }}</p></div>
        </div>
        <div class="modal-footer"><button class="btn secondary-btn" @click="showModal = false">关闭</button></div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const currentTab = ref('profile');
const showModal = ref(false);
const currentDetail = ref({});

const counsellorInfo = ref({});
const passwordForm = ref({ oldPassword: '', newPassword: '' });
// === 新增：密码显示状态 ===
const showOldPwd = ref(false);
const showNewPwd = ref(false);

const warningList = ref([]);
const searchName = ref('');
const colleges = ref([]);

const allScores = ref([]);
const scoreSearch = ref('');
const statList = ref([]);

const getTokenId = () => {
  const token = localStorage.getItem('token');
  if (!token) return null;
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(window.atob(base64));
    return payload.sub;
  } catch (e) { return null; }
};

const currentUserId = getTokenId();

// 1. 创建 axios 实例
const api = axios.create({ baseURL: 'http://localhost:8080/api/counsellor' });

// 2. 关键修改：添加请求拦截器，每次请求自动带上 Token
api.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    // 后端 @RequestHeader("Authorization") 需要的就是这个
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, error => {
  return Promise.reject(error);
});

const getTabName = (tab) => {
  // 修改点 3: 映射表中的文字也同步修改，以便面包屑显示正确
  const map = { 'profile': '个人资料', 'warnings': '预警管理', 'college-scores': '学生成绩', 'stats': '挂科统计' };
  return map[tab] || '工作台';
};

const loadColleges = async () => {
  try {
    const res = await api.get('/colleges');
    colleges.value = res.data;
  } catch (e) { console.error(e); }
};

const getCollegeName = (id) => {
  const c = colleges.value.find(item => item.collegeId === id);
  return c ? c.collegeName : id;
};

const loadProfile = async () => {
  if (!currentUserId) return;
  try {
    const res = await api.get(`/info/${currentUserId}`);
    counsellorInfo.value = res.data;
  } catch (err) {}
};

const updateProfile = async () => {
  try { await api.put('/update', counsellorInfo.value); alert("保存成功"); } catch (err) { alert("保存失败"); }
};

const changePassword = async () => {
  if (!passwordForm.value.oldPassword) return alert("请输入密码");
  try {
    await api.post('/change-password', { id: currentUserId, ...passwordForm.value });
    alert("修改成功"); handleLogout();
  } catch (err) { alert("修改失败"); }
};

const fetchWarnings = async () => {
  try {
    const res = await api.get('/warnings', { params: { counsellorId: currentUserId, studentName: searchName.value } });
    warningList.value = res.data;
  } catch (err) { console.error(err); }
};

const triggerSystemUpdate = () => { alert("系统预警信息已刷新！"); fetchWarnings(); };

const fetchCollegeScores = async () => {
  try {
    const res = await api.get('/college-scores');
    allScores.value = res.data;
  } catch (e) { alert("获取成绩数据失败"); }
};

const filteredScores = computed(() => {
  if (!scoreSearch.value) return allScores.value;
  const k = scoreSearch.value.toLowerCase();
  return allScores.value.filter(s =>
    (s.subject && s.subject.includes(k)) ||
    (s.studentName && s.studentName.includes(k))
  );
});

const fetchStats = async () => {
  try {
    const res = await api.get('/failure-stats');
    statList.value = res.data;
  } catch (e) { alert("获取统计数据失败"); }
};

const getRateColor = (rate) => {
  if (rate >= 30) return 'text-danger-dark';
  if (rate >= 10) return 'text-warning-dark';
  return 'text-success-dark';
};

const handleLogout = () => { localStorage.clear(); router.push('/'); };
const showDetail = (item) => { currentDetail.value = item; showModal.value = true; };
const formatDate = (str) => str ? new Date(str).toLocaleString() : '';

onMounted(() => {
  if (!localStorage.getItem('token')) { router.push('/'); return; }
  loadColleges();
  loadProfile();
});
</script>

<!--suppress CssInvalidPseudoSelector -->
<style scoped>
/* 全局布局 */
.dashboard-container { display: flex; height: 100vh; font-family: 'Segoe UI', 'Roboto', sans-serif; background-color: #f1f5f9; color: #334155; }
.sidebar { width: 240px; background: linear-gradient(180deg, #312e81 0%, #1e1b4b 100%); color: white; display: flex; flex-direction: column; box-shadow: 4px 0 15px rgba(0, 0, 0, 0.1); }
.logo-area { height: 80px; display: flex; align-items: center; justify-content: center; gap: 12px; border-bottom: 1px solid rgba(255, 255, 255, 0.1); }
.logo-icon { font-size: 28px; }
.logo-text { font-size: 18px; font-weight: 700; letter-spacing: 1px; }

.nav-item { padding: 16px 24px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 12px; font-size: 15px; border-left: 4px solid transparent; color: #a5b4fc; }
.nav-item:hover { background-color: rgba(255, 255, 255, 0.1); color: white; }
.nav-item.active { background-color: rgba(99, 102, 241, 0.2); border-left-color: #818cf8; color: white; font-weight: 600; }

.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.header { height: 64px; background: white; padding: 0 32px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); z-index: 5; }
.breadcrumb { font-size: 14px; color: #64748b; }
.user-area { display: flex; gap: 20px; align-items: center; }
.welcome { font-size: 14px; color: #334155; }
.logout-btn { padding: 6px 16px; border: 1px solid #fee2e2; background: white; color: #ef4444; border-radius: 6px; cursor: pointer; font-size: 13px; transition: all 0.2s; }
.logout-btn:hover { background: #fef2f2; }

.content-body { padding: 24px; overflow-y: auto; flex: 1; }
.page-title { font-size: 24px; font-weight: 600; color: #1e293b; margin-bottom: 20px; }

/* 卡片 */
.card { background: white; border-radius: 12px; padding: 24px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); transition: transform 0.2s; }
.card-header { margin-bottom: 20px; padding-bottom: 10px; border-bottom: 1px solid #f1f5f9; }
.card-header h3 { font-size: 18px; color: #1e293b; margin: 0; }
.card-footer { margin-top: 20px; text-align: right; }

.profile-grid { display: grid; grid-template-columns: 2fr 1fr; gap: 24px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.full-width { grid-column: span 2; }
.form-stack { display: flex; flex-direction: column; gap: 15px; }
.form-item { display: flex; flex-direction: column; }
.form-item label { display: block; margin-bottom: 6px; font-size: 14px; font-weight: 500; color: #64748b; }
.input-field { width: 100%; max-width: 320px; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 8px; font-size: 14px; box-sizing: border-box; }
.full-width-input { max-width: 100%; }
.input-field.disabled { background-color: #f8fafc; color: #94a3b8; cursor: not-allowed; }

.btn { padding: 10px 20px; border: none; border-radius: 8px; cursor: pointer; font-weight: 500; transition: all 0.2s; font-size: 14px; }
.primary-btn { background: #4f46e5; color: white; }
.warning-btn { background: #f59e0b; color: white; }
.secondary-btn { background: #e2e8f0; color: #475569; }
.refresh-btn { background: #10b981; color: white; display: flex; align-items: center; gap: 6px; }
.full-width-btn { width: 100%; max-width: 320px; }

/* 统计卡片网格 */
.stats-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(280px, 1fr)); gap: 20px; }
.stat-card { background: white; border-radius: 12px; padding: 20px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); border-top: 4px solid #6366f1; cursor: pointer; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-3px); box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); }
.stat-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.subject-title { font-weight: bold; font-size: 16px; color: #1f2937; }
.stat-icon { font-size: 24px; }
.stat-body { display: flex; justify-content: space-between; align-items: center; }
.rate-box { text-align: center; }
.rate-value { font-size: 28px; font-weight: 800; display: block; }
.rate-label { font-size: 12px; color: #64748b; }
.count-info p { font-size: 13px; margin: 4px 0; color: #4b5563; }
.text-danger { color: #ef4444; }
.text-danger-dark { color: #b91c1c; }
.text-warning-dark { color: #b45309; }
.text-success-dark { color: #15803d; }

/* 表格与工具栏 */
.toolbar-card { background: white; padding: 16px 24px; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.search-box { display: flex; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 4px; }
.search-input { border: none; background: transparent; padding: 6px 12px; outline: none; font-size: 14px; width: 220px; }
.icon-btn { background: white; border: 1px solid #e2e8f0; border-radius: 6px; padding: 0 12px; cursor: pointer; }
.table-card { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table th { background: #f8fafc; padding: 16px 24px; text-align: left; font-weight: 600; color: #475569; border-bottom: 1px solid #e2e8f0; }
.modern-table td { padding: 16px 24px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 14px; }
.tag { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.warning-tag { background: #fee2e2; color: #ef4444; }
.course-tag { background: #e0f2fe; color: #0284c7; }
.score-badge { display: inline-block; width: 32px; text-align: center; border-radius: 50%; font-weight: bold; font-size: 12px; line-height: 32px; }
.score-badge.pass { background: #dcfce7; color: #166534; }
.score-badge.fail { background: #fee2e2; color: #991b1b; }
.status-wait { color: #9ca3af; font-size: 12px; font-style: italic; }
.student-name { font-weight: 600; color: #1e293b; }
.text-muted { color: #94a3b8; font-size: 13px; }
.text-truncate { max-width: 300px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; color: #64748b; }
.outline-btn { background: transparent; border: 1px solid #6366f1; color: #6366f1; padding: 6px 12px; border-radius: 6px; font-size: 13px; }
.empty-state { padding: 40px; text-align: center; color: #94a3b8; }

/* 弹窗 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); backdrop-filter: blur(2px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: white; width: 500px; border-radius: 12px; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); overflow: hidden; }
.modal-header { padding: 20px 24px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; }
.close-icon { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.modal-body { padding: 24px; }
.detail-group { background: #f8fafc; padding: 15px; border-radius: 8px; margin-bottom: 20px; }
.detail-row { display: flex; margin-bottom: 8px; font-size: 14px; }
.detail-row .label { width: 60px; color: #64748b; }
.detail-row .value { font-weight: 500; color: #334155; }
.title-value { color: #ef4444; font-weight: 700; }
.detail-content { line-height: 1.6; color: #334155; font-size: 15px; }
.modal-footer { padding: 16px 24px; background: #f8fafc; text-align: right; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }

@media (max-width: 1024px) { .profile-grid { grid-template-columns: 1fr; } .stats-grid { grid-template-columns: 1fr; } }

/* === 修改：修复眼睛图标位置并隐藏默认图标 === */
.pwd-wrapper {
  position: relative;
  width: 100%;
  max-width: 320px; /* 限制最大宽度，与输入框一致 */
}

/* 给带有图标的输入框添加右侧内边距 */
.pwd-input {
  padding-right: 40px;
}

.eye-icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%); /* 垂直居中 */
  cursor: pointer;
  user-select: none;
  font-size: 18px;
  color: #9ca3af;
  transition: color 0.2s;
  z-index: 10;
}
.eye-icon:hover { color: #3b82f6; }

/* === 隐藏浏览器默认的密码查看图标 === */
.input-field::-webkit-password-reveal-button,
.input-field::-webkit-credentials-auto-fill-button {
  display: none !important;
  -webkit-appearance: none;
}
.input-field::-ms-reveal,
.input-field::-ms-clear {
  display: none !important;
}
</style>
