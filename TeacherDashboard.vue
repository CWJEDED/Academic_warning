<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">👨‍🏫</div>
        <div class="logo-text">教师工作台</div>
      </div>
      <nav>
        <div :class="['nav-item', currentTab === 'profile' ? 'active' : '']" @click="currentTab = 'profile'">
          <span class="icon">👤</span> 个人中心
        </div>
        <div :class="['nav-item', currentTab === 'my-courses' ? 'active' : '']" @click="currentTab = 'my-courses'">
          <span class="icon">📚</span> 所负责课程
        </div>
        <div :class="['nav-item', currentTab === 'attendance' ? 'active' : '']" @click="currentTab = 'attendance'">
          <span class="icon">📅</span> 考勤管理
        </div>
        <div :class="['nav-item', currentTab === 'score' ? 'active' : '']" @click="currentTab = 'score'">
          <span class="icon">💯</span> 成绩管理
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="breadcrumb">首页 / {{ getTabName(currentTab) }}</div>
        <div class="user-area">
          <span class="welcome">欢迎您，<strong>{{ teacherName }}</strong> 老师</span>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </header>

      <div class="content-body">

        <transition name="fade" mode="out-in">

          <div v-if="currentTab === 'profile'" key="profile">
            <div class="page-header">
              <h2>个人资料与安全</h2>
            </div>

            <div class="profile-layout">
              <div class="card info-card">
                <div class="card-header">
                  <h3>📝 基本信息</h3>
                </div>
                <div class="form-grid">
                  <div class="form-item">
                    <label>工号</label>
                    <input type="text" v-model="teacherInfo.teacherId" disabled class="input-field disabled" />
                  </div>
                  <div class="form-item">
                    <label>姓名</label>
                    <input type="text" v-model="teacherInfo.teacherName" class="input-field" />
                  </div>
                  <div class="form-item">
                    <label>性别</label>
                    <select v-model="teacherInfo.gender" class="input-field">
                      <option value="男">男</option>
                      <option value="女">女</option>
                    </select>
                  </div>
                  <div class="form-item">
                    <label>所属学院</label>
                    <input
                      type="text"
                      v-model="teacherInfo.collegeName"
                      disabled
                      class="input-field disabled"
                    />
                  </div>
                  <div class="form-item">
                    <label>手机</label>
                    <input type="text" v-model="teacherInfo.phone" class="input-field" />
                  </div>
                  <div class="form-item">
                    <label>邮箱</label>
                    <input type="text" v-model="teacherInfo.email" class="input-field" />
                  </div>
                  <div class="form-item full-width">
                    <label>密码</label>
                    <input type="text" value="******" disabled class="input-field disabled" />
                    <span class="tip-text">为了安全，密码已隐藏</span>
                  </div>
                </div>
                <div class="card-footer">
                  <button class="primary-btn" @click="updateProfile">保存基本信息</button>
                </div>
              </div>

              <div class="card password-card">
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
                        placeholder="请输入当前密码"
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
                  <button class="warning-btn full-width-btn" @click="changePassword">确认修改密码</button>
                </div>
              </div>
            </div>
          </div>

          <div v-else-if="currentTab === 'my-courses'" key="courses">
            <div class="page-header">
              <h2>我负责的课程</h2>
            </div>
            <div class="courses-grid">
              <div v-for="c in courses" :key="c.id" class="course-card">
                <div class="course-icon">📖</div>
                <div class="course-info">
                  <h3>{{ c.subject }}</h3>
                  <p class="course-id">课程ID: {{ c.id }}</p>
                </div>
                <button class="outline-btn" @click="loadStudentsForView(c.subject)">
                  {{ viewStudentsMap[c.subject] ? '收起名单' : '查看学生' }}
                </button>

                <div v-if="viewStudentsMap[c.subject]" class="student-list-container fade-in">
                  <table class="modern-table small-table">
                    <thead><tr><th>学号</th><th>姓名</th></tr></thead>
                    <tbody>
                    <tr v-for="s in viewStudentsMap[c.subject]" :key="s.id">
                      <td>{{ s.studentId }}</td>
                      <td>{{ s.studentName }}</td>
                    </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
            <div v-if="courses.length === 0" class="empty-state">暂无负责课程</div>
          </div>

          <div v-else-if="currentTab === 'attendance'" key="attendance">
            <div class="page-header">
              <h2>考勤信息管理</h2>
            </div>

            <div class="card form-card">
              <div class="card-header">
                <h3>✏️ 录入缺勤</h3>
              </div>
              <div class="inline-form">
                <select v-model="attForm.subject" @change="loadStudentsForForm('att')" class="input-field">
                  <option disabled value="">选择科目</option>
                  <option v-for="c in courses" :key="c.id" :value="c.subject">{{ c.subject }}</option>
                </select>
                <select v-model="attForm.selectedStudent" :disabled="!attForm.subject" class="input-field">
                  <option disabled value="">选择学生</option>
                  <option v-for="s in formStudents" :key="s.studentId" :value="s">
                    {{ s.studentName }} ({{ s.studentId }})
                  </option>
                </select>
                <input type="datetime-local" v-model="attForm.time" class="input-field" />
                <button class="primary-btn" @click="submitAttendance">提交记录</button>
              </div>
            </div>

            <div class="card table-card">
              <div class="card-header">
                <h3>📜 缺勤记录列表</h3>
              </div>
              <table class="modern-table">
                <thead><tr><th>科目</th><th>学生</th><th>缺勤时间</th><th>操作</th></tr></thead>
                <tbody>
                <tr v-for="item in attendanceList" :key="item.id">
                  <td><span class="tag">{{ item.subjectName }}</span></td>
                  <td>{{ item.studentName }}</td>
                  <td class="text-muted">{{ formatTime(item.time) }}</td>
                  <td>
                    <button class="icon-btn delete" @click="delAttendance(item.id)" title="删除">🗑️</button>
                  </td>
                </tr>
                </tbody>
              </table>
              <div v-if="attendanceList.length === 0" class="empty-state">暂无缺勤记录</div>
            </div>
          </div>

          <div v-else-if="currentTab === 'score'" key="score">
            <div class="page-header">
              <h2>成绩管理</h2>
            </div>

            <div class="card form-card">
              <div class="card-header">
                <h3>✏️ 录入成绩</h3>
              </div>
              <div class="inline-form">
                <select v-model="scoreForm.subject" @change="loadStudentsForForm('score')" class="input-field">
                  <option disabled value="">选择科目</option>
                  <option v-for="c in courses" :key="c.id" :value="c.subject">{{ c.subject }}</option>
                </select>
                <select v-model="scoreForm.selectedStudent" :disabled="!scoreForm.subject" class="input-field">
                  <option disabled value="">选择学生</option>
                  <option v-for="s in formStudents" :key="s.studentId" :value="s">
                    {{ s.studentName }} ({{ s.studentId }})
                  </option>
                </select>
                <input type="number" v-model="scoreForm.scores" min="0" placeholder="分数" class="input-field short-input" />
                <button class="primary-btn" @click="submitScore">录入</button>
              </div>
            </div>

            <div class="card table-card">
              <div class="card-header">
                <h3>📊 成绩列表</h3>
              </div>
              <table class="modern-table">
                <thead><tr><th>科目</th><th>学生</th><th>分数</th><th>学分</th><th>操作</th></tr></thead>
                <tbody>
                <tr v-for="item in scoreList" :key="item.id">
                  <td><b>{{ item.subject }}</b></td>
                  <td>{{ item.studentName }}</td>
                  <td>
                    <div v-if="editScoreId === item.id" class="edit-wrapper">
                      <input type="number" v-model="editScoreVal" min="0" class="input-field mini-input">
                    </div>
                    <span v-else :class="['score-badge', item.scores >= 60 ? 'pass' : 'fail']">
                        {{ item.scores }}
                      </span>
                  </td>
                  <td>{{ item.credit }}</td>
                  <td>
                    <div v-if="editScoreId === item.id" class="action-group">
                      <button class="icon-btn save" @click="saveScoreEdit(item)" title="保存">💾</button>
                      <button class="icon-btn cancel" @click="editScoreId = null" title="取消">❌</button>
                    </div>
                    <div v-else class="action-group">
                      <button class="icon-btn edit" @click="startEdit(item)" title="编辑">✏️</button>
                      <button class="icon-btn delete" @click="delScore(item.id)" title="删除">🗑️</button>
                    </div>
                  </td>
                </tr>
                </tbody>
              </table>
              <div v-if="scoreList.length === 0" class="empty-state">暂无成绩记录</div>
            </div>
          </div>

        </transition>

      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const teacherName = localStorage.getItem('name') || '教师';
const token = localStorage.getItem('token');
const currentTab = ref('profile');

const api = axios.create({
  baseURL: 'http://localhost:8080/api/teacher',
  headers: { 'Authorization': `Bearer ${token}` }
});

const teacherInfo = ref({});
const passwordForm = ref({ oldPassword: '', newPassword: '' });

// === 密码显示状态控制变量 ===
const showOldPwd = ref(false);
const showNewPwd = ref(false);

const courses = ref([]);
const viewStudentsMap = ref({});
const formStudents = ref([]);
const attendanceList = ref([]);
const scoreList = ref([]);

const attForm = ref({ subject: '', selectedStudent: '', time: '' });
const scoreForm = ref({ subject: '', selectedStudent: '', scores: '', credit: '' });

const editScoreId = ref(null);
const editScoreVal = ref('');

// 辅助方法：获取Tab名称
const getTabName = (tab) => {
  const map = {
    'profile': '个人中心',
    'my-courses': '负责课程',
    'attendance': '考勤管理',
    'score': '成绩管理'
  };
  return map[tab] || '工作台';
};

// === 1. 加载个人信息 ===
const loadProfile = async () => {
  try {
    const res = await api.get('/info');
    teacherInfo.value = res.data;
  } catch (e) { console.error("加载个人信息失败", e); }
};

// === 2. 更新个人信息 ===
const updateProfile = async () => {
  try {
    await api.put('/update', teacherInfo.value);
    alert("保存成功");
  } catch (e) { alert("保存失败"); }
};

// === 3. 修改密码 ===
const changePassword = async () => {
  if(!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    return alert("请输入原密码和新密码");
  }
  try {
    await api.post('/change-password', passwordForm.value);
    alert("密码修改成功，请重新登录");
    logout();
  } catch (e) {
    alert("修改失败：" + (e.response?.data || "未知错误"));
  }
};

// === 业务逻辑 ===
const fetchCourses = async () => {
  try {
    const res = await api.get('/courses');
    courses.value = res.data;
  } catch (e) { console.error(e); }
};

const loadStudentsForView = async (subject) => {
  if (viewStudentsMap.value[subject]) {
    viewStudentsMap.value[subject] = null;
    return;
  }
  const res = await api.get(`/students?subject=${subject}`);
  viewStudentsMap.value[subject] = res.data;
};

const loadStudentsForForm = async (type) => {
  const subject = type === 'att' ? attForm.value.subject : scoreForm.value.subject;
  const res = await api.get(`/students?subject=${subject}`);
  formStudents.value = res.data;
};

const fetchAttendanceList = async () => {
  const res = await api.get('/attendance-list');
  attendanceList.value = res.data;
};

const formatToBackendTime = (date) => {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, '0');
  const d = String(date.getDate()).padStart(2, '0');
  const h = String(date.getHours()).padStart(2, '0');
  const min = String(date.getMinutes()).padStart(2, '0');
  const s = String(date.getSeconds()).padStart(2, '0');
  return `${y}-${m}-${d} ${h}:${min}:${s}`;
};

const submitAttendance = async () => {
  if (!attForm.value.selectedStudent) return alert("请选择学生");
  const dateObj = attForm.value.time ? new Date(attForm.value.time) : new Date();
  const formattedTime = formatToBackendTime(dateObj);

  try {
    await api.post('/attendance', {
      subjectName: attForm.value.subject,
      studentId: attForm.value.selectedStudent.studentId,
      studentName: attForm.value.selectedStudent.studentName,
      time: formattedTime
    });
    alert("添加成功");
    fetchAttendanceList();
  } catch (err) { alert("添加失败"); }
};

const delAttendance = async (id) => {
  if(confirm('确认删除?')) {
    await api.delete(`/attendance/${id}`);
    fetchAttendanceList();
  }
};

const fetchScoreList = async () => {
  const res = await api.get('/score-list');
  scoreList.value = res.data;
};

const submitScore = async () => {
  if (!scoreForm.value.selectedStudent) return alert("请选择学生");

  if (scoreForm.value.scores < 0) {
    return alert("分数不能为负数！");
  }

  try {
    await api.post('/score', {
      subject: scoreForm.value.subject,
      studentId: scoreForm.value.selectedStudent.studentId,
      scores: scoreForm.value.scores
    });
    alert("录入成功");
    fetchScoreList();
    scoreForm.value.scores = '';
    scoreForm.value.selectedStudent = '';
  } catch (error) {
    if (error.response && error.response.data) alert(error.response.data);
    else alert("录入失败");
  }
};

const startEdit = (item) => {
  editScoreId.value = item.id;
  editScoreVal.value = item.scores;
};

const saveScoreEdit = async (item) => {
  if (editScoreVal.value < 0) {
    return alert("分数不能为负数！");
  }

  await api.put('/score', { id: item.id, scores: editScoreVal.value });
  editScoreId.value = null;
  fetchScoreList();
};

const delScore = async (id) => {
  if(confirm('确认删除?')) {
    await api.delete(`/score/${id}`);
    fetchScoreList();
  }
};

const logout = () => { localStorage.clear(); router.push('/'); };
const formatTime = (t) => new Date(t).toLocaleString();

onMounted(() => {
  if (!token) { router.push('/'); return; }
  loadProfile();
  fetchCourses();
  fetchAttendanceList();
  fetchScoreList();
});
</script>

<style scoped>
/* 全局布局 */
.dashboard-container {
  display: flex;
  height: 100vh;
  font-family: 'Segoe UI', 'Roboto', Helvetica, Arial, sans-serif;
  background-color: #f3f4f6;
  color: #333;
}

/* 侧边栏 */
.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 15px rgba(0, 0, 0, 0.1);
  z-index: 10;
}
.logo-area {
  height: 80px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.logo-icon { font-size: 28px; }
.logo-text { font-size: 20px; font-weight: 700; letter-spacing: 1px; }

.nav-item {
  padding: 16px 24px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 15px;
  border-left: 4px solid transparent;
  color: #94a3b8;
}
.nav-item:hover {
  background-color: rgba(255, 255, 255, 0.05);
  color: #fff;
}
.nav-item.active {
  background-color: rgba(59, 130, 246, 0.1);
  border-left-color: #3b82f6;
  color: #fff;
  font-weight: 600;
}
.icon { font-size: 18px; width: 24px; text-align: center; }

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  height: 64px;
  background: white;
  padding: 0 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
  z-index: 5;
}
.breadcrumb { color: #6b7280; font-size: 14px; }
.user-area { display: flex; align-items: center; gap: 20px; }
.welcome { font-size: 14px; color: #374151; }
.logout-btn {
  background: white;
  border: 1px solid #e5e7eb;
  color: #ef4444;
  padding: 6px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}
.logout-btn:hover { background: #fef2f2; border-color: #fecaca; }

.content-body {
  padding: 30px;
  overflow-y: auto;
  flex: 1;
}
.page-header { margin-bottom: 24px; }
.page-header h2 { font-size: 24px; font-weight: 600; color: #1f2937; margin: 0; }

/* 卡片样式 */
.card {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03);
  margin-bottom: 24px;
  transition: transform 0.2s;
}
.card-header {
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f3f4f6;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-header h3 { font-size: 18px; color: #374151; margin: 0; font-weight: 600; }
.card-footer { margin-top: 20px; text-align: right; }

/* 表单元素 */
.input-field {
  width: 100%;
  max-width: 360px; /* 限制最大宽度，美化显示 */
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.2s;
  background: #fff;
}
.input-field:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}
.input-field.disabled { background-color: #f9fafb; color: #9ca3af; cursor: not-allowed; }
.short-input { width: 100px; }
.mini-input { padding: 4px 8px; width: 60px; text-align: center; }

/* 个人中心布局 */
.profile-layout {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 24px;
}
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}
.full-width { grid-column: span 2; }
.form-item { display: flex; flex-direction: column; gap: 6px; }
.form-item label { font-size: 13px; font-weight: 500; color: #6b7280; }
.tip-text { font-size: 12px; color: #9ca3af; margin-top: 4px; }

.form-stack { display: flex; flex-direction: column; gap: 15px; }

/* 按钮样式 */
.primary-btn {
  background: #3b82f6; color: white; border: none;
  padding: 10px 20px; border-radius: 6px; cursor: pointer;
  font-weight: 500; transition: background 0.2s;
}
.primary-btn:hover { background: #2563eb; }

.warning-btn {
  background: #f59e0b; color: white; border: none;
  padding: 10px 20px; border-radius: 6px; cursor: pointer;
  font-weight: 500; transition: background 0.2s;
}
.warning-btn:hover { background: #d97706; }

.outline-btn {
  background: transparent; border: 1px solid #3b82f6; color: #3b82f6;
  padding: 6px 12px; border-radius: 6px; cursor: pointer; font-size: 13px;
  transition: all 0.2s;
}
.outline-btn:hover { background: #eff6ff; }

.full-width-btn { width: 100%; max-width: 360px; }

/* 课程卡片网格 */
.courses-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
}
.course-card {
  background: white; border-radius: 12px; padding: 20px;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05);
  border-top: 4px solid #3b82f6;
  position: relative;
  display: flex; flex-direction: column; gap: 15px;
}
.course-icon { font-size: 32px; margin-bottom: -10px; }
.course-info h3 { margin: 0; font-size: 18px; color: #1f2937; }
.course-id { font-size: 12px; color: #9ca3af; margin: 4px 0 0 0; }
.student-list-container {
  margin-top: 10px;
  background: #f9fafb;
  border-radius: 8px;
  padding: 10px;
  max-height: 200px;
  overflow-y: auto;
}

/* 考勤与成绩表单 */
.inline-form {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

/* 现代表格 */
.modern-table {
  width: 100%;
  border-collapse: separate;
  border-spacing: 0;
}
.modern-table th {
  background: #f8fafc;
  color: #475569;
  font-weight: 600;
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #e2e8f0;
  font-size: 14px;
}
.modern-table td {
  padding: 14px 16px;
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
  font-size: 14px;
}
.modern-table tr:hover td { background-color: #f8fafc; }
.small-table th, .small-table td { padding: 8px 10px; font-size: 13px; }

/* 状态标签与按钮 */
.tag {
  background: #e0f2fe; color: #0284c7;
  padding: 4px 8px; border-radius: 4px; font-size: 12px; font-weight: 500;
}
.text-muted { color: #94a3b8; font-size: 13px; }
.score-badge {
  display: inline-block; width: 32px; height: 32px;
  line-height: 32px; text-align: center; border-radius: 50%;
  font-weight: 700; font-size: 13px;
}
.score-badge.pass { background: #dcfce7; color: #166534; }
.score-badge.fail { background: #fee2e2; color: #991b1b; }

.icon-btn {
  background: none; border: none; cursor: pointer; font-size: 16px;
  padding: 4px; border-radius: 4px; transition: background 0.2s;
  margin-right: 5px;
}
.icon-btn:hover { background: #f3f4f6; }
.empty-state { text-align: center; color: #9ca3af; padding: 40px 0; font-style: italic; }

/* 动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.3s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.fade-in { animation: fadeIn 0.4s ease-out; }
@keyframes fadeIn { from { opacity: 0; transform: translateY(5px); } to { opacity: 1; transform: translateY(0); } }

/* 响应式 */
@media (max-width: 1024px) {
  .profile-layout { grid-template-columns: 1fr; }
  .courses-grid { grid-template-columns: 1fr; }
}

/* === 修改：修复眼睛图标位置并隐藏默认图标 === */
.pwd-wrapper {
  position: relative;
  width: 100%;
  max-width: 360px; /* 关键：限制包装容器宽度与输入框一致 */
}

/* 给带有图标的输入框添加右侧内边距 */
.pwd-input {
  padding-right: 40px;
}

.eye-icon {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%); /* 关键：垂直居中 */
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
