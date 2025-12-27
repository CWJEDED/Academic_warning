<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo">教师工作台</div>
      <nav>
        <div :class="['nav-item', currentTab === 'profile' ? 'active' : '']" @click="currentTab = 'profile'">
          个人中心
        </div>
        <div :class="['nav-item', currentTab === 'my-courses' ? 'active' : '']" @click="currentTab = 'my-courses'">
          所负责课程
        </div>
        <div :class="['nav-item', currentTab === 'attendance' ? 'active' : '']" @click="currentTab = 'attendance'">
          考勤信息管理
        </div>
        <div :class="['nav-item', currentTab === 'score' ? 'active' : '']" @click="currentTab = 'score'">
          成绩管理
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <span>欢迎您，{{ teacherName }} 老师</span>
        <button class="logout-btn" @click="logout">退出</button>
      </header>

      <div class="content-body">

        <div v-if="currentTab === 'profile'">
          <h2>个人中心</h2>

          <div class="card">
            <h3>基本信息</h3>
            <div class="form-row-grid">
              <div class="form-group">
                <label>工号 (不可修):</label>
                <input type="text" v-model="teacherInfo.teacherId" disabled />
              </div>
              <div class="form-group">
                <label>姓名:</label>
                <input type="text" v-model="teacherInfo.teacherName" />
              </div>
              <div class="form-group">
                <label>性别:</label>
                <select v-model="teacherInfo.gender">
                  <option value="男">男</option>
                  <option value="女">女</option>
                </select>
              </div>
              <div class="form-group">
                <label>学院ID:</label>
                <input type="number" v-model="teacherInfo.collegeId" />
              </div>
              <div class="form-group">
                <label>手机:</label>
                <input type="text" v-model="teacherInfo.phone" />
              </div>
              <div class="form-group">
                <label>邮箱:</label>
                <input type="text" v-model="teacherInfo.email" />
              </div>
              <div class="form-group">
                <label>密码:</label>
                <input type="text" value="******" disabled style="background:#f5f7fa; color:#909399" />
              </div>
            </div>
            <button class="action-btn" style="margin-top: 15px;" @click="updateProfile">保存基本信息</button>
          </div>

          <div class="card" style="margin-top: 20px;">
            <h3>安全设置 - 修改密码</h3>
            <div class="form-row">
              <input type="password" v-model="passwordForm.oldPassword" placeholder="请输入原密码" />
              <input type="password" v-model="passwordForm.newPassword" placeholder="请输入新密码" />
              <button class="action-btn warning-btn" @click="changePassword">确认修改</button>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'my-courses'">
          <h2>我负责的课程</h2>
          <div v-for="c in courses" :key="c.id" class="card">
            <h3>{{ c.subject }}</h3>
            <button @click="loadStudentsForView(c.subject)">查看选课学生</button>
            <div v-if="viewStudentsMap[c.subject]" class="student-list">
              <table>
                <thead><tr><th>学号</th><th>姓名</th><th>科目</th></tr></thead>
                <tbody>
                <tr v-for="s in viewStudentsMap[c.subject]" :key="s.id">
                  <td>{{ s.studentId }}</td>
                  <td>{{ s.studentName }}</td>
                  <td>{{ s.subject }}</td>
                </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div v-if="currentTab === 'attendance'">
          <h2>考勤信息管理</h2>
          <div class="form-box">
            <h3>新增缺勤</h3>
            <select v-model="attForm.subject" @change="loadStudentsForForm('att')">
              <option disabled value="">请选择科目</option>
              <option v-for="c in courses" :key="c.id" :value="c.subject">{{ c.subject }}</option>
            </select>
            <select v-model="attForm.selectedStudent" :disabled="!attForm.subject">
              <option disabled value="">请选择学生</option>
              <option v-for="s in formStudents" :key="s.studentId" :value="s">
                {{ s.studentName }} ({{ s.studentId }})
              </option>
            </select>
            <input type="datetime-local" v-model="attForm.time" />
            <button @click="submitAttendance">提交</button>
          </div>

          <table>
            <thead><tr><th>科目</th><th>学生</th><th>时间</th><th>操作</th></tr></thead>
            <tbody>
            <tr v-for="item in attendanceList" :key="item.id">
              <td>{{ item.subjectName }}</td>
              <td>{{ item.studentName }}</td>
              <td>{{ formatTime(item.time) }}</td>
              <td><button class="del-btn" @click="delAttendance(item.id)">删除</button></td>
            </tr>
            </tbody>
          </table>
        </div>

        <div v-if="currentTab === 'score'">
          <h2>成绩管理</h2>
          <div class="form-box">
            <h3>录入成绩</h3>
            <select v-model="scoreForm.subject" @change="loadStudentsForForm('score')">
              <option disabled value="">请选择科目</option>
              <option v-for="c in courses" :key="c.id" :value="c.subject">{{ c.subject }}</option>
            </select>
            <select v-model="scoreForm.selectedStudent" :disabled="!scoreForm.subject">
              <option disabled value="">请选择学生</option>
              <option v-for="s in formStudents" :key="s.studentId" :value="s">
                {{ s.studentName }} ({{ s.studentId }})
              </option>
            </select>
            <input type="number" v-model="scoreForm.scores" placeholder="分数" />

            <button @click="submitScore">录入</button>
          </div>

          <table>
            <thead><tr><th>科目</th><th>学生</th><th>分数</th><th>学分</th><th>操作</th></tr></thead>
            <tbody>
            <tr v-for="item in scoreList" :key="item.id">
              <td>{{ item.subject }}</td>
              <td>{{ item.studentName }}</td>
              <td>
                <input v-if="editScoreId === item.id" type="number" v-model="editScoreVal" style="width:60px">
                <span v-else>{{ item.scores }}</span>
              </td>
              <td>{{ item.credit }}</td>
              <td>
                <div v-if="editScoreId === item.id">
                  <button @click="saveScoreEdit(item)">保存</button>
                  <button @click="editScoreId = null">取消</button>
                </div>
                <div v-else>
                  <button @click="startEdit(item)">编辑</button>
                  <button class="del-btn" @click="delScore(item.id)">删除</button>
                </div>
              </td>
            </tr>
            </tbody>
          </table>
        </div>

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

const courses = ref([]);
const viewStudentsMap = ref({});
const formStudents = ref([]);
const attendanceList = ref([]);
const scoreList = ref([]);

const attForm = ref({ subject: '', selectedStudent: '', time: '' });
const scoreForm = ref({ subject: '', selectedStudent: '', scores: '', credit: '' });

const editScoreId = ref(null);
const editScoreVal = ref('');

// === 1. 加载个人信息 ===
const loadProfile = async () => {
  try {
    const res = await api.get('/info');
    teacherInfo.value = res.data;
    // 此时 res.data.password 已经是 "******"，前端直接显示即可，无泄露风险
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
.dashboard-container { display: flex; height: 100vh; }
.sidebar { width: 200px; background: #001529; color: white; }
.logo { height: 60px; line-height: 60px; text-align: center; background: #002140; font-weight: bold; }
.nav-item { padding: 15px 20px; cursor: pointer; border-bottom: 1px solid #002140; }
.nav-item.active, .nav-item:hover { background: #1890ff; }
.main-content { flex: 1; padding: 20px; background: #f0f2f5; overflow-y: auto; }
.header { display: flex; justify-content: space-between; margin-bottom: 20px; background: white; padding: 15px; border-radius: 4px; }
.card, .form-box { background: white; padding: 20px; margin-bottom: 20px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1); }
table { width: 100%; border-collapse: collapse; background: white; margin-top: 10px; }
th, td { border: 1px solid #eee; padding: 10px; text-align: left; }
.logout-btn { background: #ff4d4f; color: white; border: none; padding: 5px 15px; border-radius: 4px; cursor: pointer; }
.del-btn { color: red; margin-left: 10px; }
input, select { padding: 8px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
button { padding: 8px 15px; cursor: pointer; background: #1890ff; color: white; border: none; border-radius: 4px; }
.student-list { margin-top: 10px; padding: 10px; background: #fafafa; border: 1px solid #eee; }

.form-row-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }
.form-group { display: flex; flex-direction: column; }
.form-group label { font-weight: bold; margin-bottom: 5px; }
.form-group input, .form-group select { width: 100%; padding: 8px; }
.action-btn { background: #52c41a; border: none; color: white; padding: 10px 20px; border-radius: 4px; cursor: pointer; }
.warning-btn { background: #faad14; }
.form-row { display: flex; align-items: center; gap: 10px; }
</style>
