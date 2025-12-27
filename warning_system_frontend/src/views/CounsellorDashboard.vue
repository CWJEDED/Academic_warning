<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo">学业预警系统</div>
      <nav>
        <div
          :class="['nav-item', currentTab === 'profile' ? 'active' : '']"
          @click="currentTab = 'profile'">
          个人中心
        </div>
        <div
          :class="['nav-item', currentTab === 'warnings' ? 'active' : '']"
          @click="fetchWarnings(); currentTab = 'warnings'">
          预警信息
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <span>欢迎您，{{ counsellorInfo.counsellorName }} 辅导员</span>
        <button class="logout-btn" @click="handleLogout">退出登录</button>
      </header>

      <div class="content-body">

        <div v-if="currentTab === 'profile'" class="profile-section">
          <h2>个人信息管理</h2>
          <div class="card">
            <div class="form-item">
              <label>工号 (不可修):</label>
              <input type="text" v-model="counsellorInfo.counsellorId" disabled />
            </div>
            <div class="form-item">
              <label>姓名:</label>
              <input type="text" v-model="counsellorInfo.counsellorName" />
            </div>
            <div class="form-item">
              <label>所属学院ID:</label>
              <input type="text" v-model="counsellorInfo.collegeId" disabled />
            </div>
            <div class="form-item">
              <label>手机:</label>
              <input type="text" v-model="counsellorInfo.phone" />
            </div>
            <div class="form-item">
              <label>邮箱:</label>
              <input type="text" v-model="counsellorInfo.email" />
            </div>
            <button class="save-btn" @click="updateProfile">保存修改</button>
          </div>

          <h3>修改密码</h3>
          <div class="card">
            <div class="form-item">
              <label>原密码:</label>
              <input type="password" v-model="passwordForm.oldPassword" />
            </div>
            <div class="form-item">
              <label>新密码:</label>
              <input type="password" v-model="passwordForm.newPassword" />
            </div>
            <button class="warn-btn" @click="changePassword">确认修改密码</button>
          </div>
        </div>

        <div v-if="currentTab === 'warnings'" class="warnings-section">
          <h2>本院学生预警信息</h2>

          <div class="search-bar">
            <input type="text" v-model="searchName" placeholder="输入学生姓名搜索..." />
            <button @click="fetchWarnings">查询</button>
            <button @click="triggerSystemUpdate" class="refresh-btn">刷新系统数据</button>
          </div>

          <table class="data-table">
            <thead>
            <tr>
              <th>预警标题</th>
              <th>学生姓名</th>
              <th>学号</th>
              <th>发送时间</th>
              <th>内容摘要</th>
              <th>操作</th>
            </tr>
            </thead>
            <tbody>
            <tr v-for="item in warningList" :key="item.id">
              <td>{{ item.title }}</td>
              <td>{{ item.studentName }}</td>
              <td>{{ item.studentId }}</td>
              <td>{{ formatDate(item.sendTime) }}</td>
              <td class="text-truncate">{{ item.text }}</td>
              <td>
                <button class="view-btn" @click="showDetail(item)">查看详情</button>
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
        <h3>预警详情</h3>
        <p><strong>标题：</strong>{{ currentDetail.title }}</p>
        <p><strong>学生：</strong>{{ currentDetail.studentName }} ({{ currentDetail.studentId }})</p>
        <p><strong>时间：</strong>{{ formatDate(currentDetail.sendTime) }}</p>
        <hr/>
        <p class="modal-text">{{ currentDetail.text }}</p>
        <button @click="showModal = false">关闭</button>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import { jwtDecode } from "jwt-decode"; // 如果没装这个包，可以用简单的atob解析，或者依赖localStorage里的name

const router = useRouter();
const currentTab = ref('profile'); // 默认显示个人中心
const showModal = ref(false);
const currentDetail = ref({});

// 数据模型
const counsellorInfo = ref({
  counsellorId: '',
  counsellorName: '',
  collegeId: '',
  phone: '',
  email: ''
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: ''
});

const warningList = ref([]);
const searchName = ref('');

// 获取当前登录用户的ID (从 localStorage 或 Token 解析)
const getTokenId = () => {
  const token = localStorage.getItem('token');
  if (!token) return null;
  // 简单起见，我们在登录时存了 name，但 ID 最好在 login 时也存入 localStorage，
  // 或者这里假设 JWT 的 subject 是 ID
  // 这里我们假设你修改了 LoginView.vue，在 localStorage 存了 'userId'
  // 如果没有存，请在 LoginView.vue 的 handleLogin 里加一句: localStorage.setItem('userId', request.username);

  // 临时补救：解析 JWT payload (仅作演示，建议生产环境用库)
  try {
    const base64Url = token.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(window.atob(base64));
    return payload.sub; // sub 也就是 subject，通常存的是 ID
  } catch (e) {
    return null;
  }
};

const currentUserId = getTokenId();

// === API 请求 ===

// 1. 加载个人信息
const loadProfile = async () => {
  if (!currentUserId) return;
  try {
    const res = await axios.get(`http://localhost:8080/api/counsellor/info/${currentUserId}`);
    counsellorInfo.value = res.data;
  } catch (err) {
    alert("获取用户信息失败");
  }
};

// 2. 更新个人信息
const updateProfile = async () => {
  try {
    await axios.put('http://localhost:8080/api/counsellor/update', counsellorInfo.value);
    alert("保存成功");
  } catch (err) {
    alert("保存失败");
  }
};

// 3. 修改密码
const changePassword = async () => {
  if (!passwordForm.value.oldPassword || !passwordForm.value.newPassword) {
    alert("请输入密码");
    return;
  }
  try {
    await axios.post('http://localhost:8080/api/counsellor/change-password', {
      id: currentUserId,
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });
    alert("密码修改成功，请重新登录");
    handleLogout();
  } catch (err) {
    alert(err.response?.data || "修改失败");
  }
};

// 4. 获取预警列表
const fetchWarnings = async () => {
  try {
    const res = await axios.get('http://localhost:8080/api/counsellor/warnings', {
      params: {
        counsellorId: currentUserId,
        studentName: searchName.value
      }
    });
    warningList.value = res.data;
  } catch (err) {
    console.error(err);
  }
};

// 5. 触发系统刷新 (模拟功能)
const triggerSystemUpdate = () => {
  alert("系统预警信息已刷新！(模拟)");
  fetchWarnings();
};

// 辅助功能
const handleLogout = () => {
  localStorage.clear();
  router.push('/');
};

const showDetail = (item) => {
  currentDetail.value = item;
  showModal.value = true;
};

const formatDate = (str) => {
  if(!str) return '';
  return new Date(str).toLocaleString();
}

// 初始化
onMounted(() => {
  if (!localStorage.getItem('token')) {
    router.push('/');
  } else {
    loadProfile();
  }
});
</script>

<style scoped>
/* 简单布局样式 */
.dashboard-container {
  display: flex;
  height: 100vh;
}
.sidebar {
  width: 200px;
  background-color: #001529;
  color: white;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  background-color: #002140;
}
.nav-item {
  padding: 15px 20px;
  cursor: pointer;
  transition: background 0.3s;
}
.nav-item:hover, .nav-item.active {
  background-color: #1890ff;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  background-color: #f0f2f5;
  overflow: hidden;
}
.header {
  height: 60px;
  background: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.1);
}
.logout-btn {
  margin-left: 20px;
  background: #ff4d4f;
  color: white;
  border: none;
  padding: 5px 15px;
  border-radius: 4px;
  cursor: pointer;
}
.content-body {
  padding: 20px;
  overflow-y: auto;
  flex: 1;
}

/* 卡片样式 */
.card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.form-item {
  margin-bottom: 15px;
  display: flex;
  align-items: center;
}
.form-item label {
  width: 100px;
  font-weight: bold;
}
.form-item input {
  flex: 1;
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
  max-width: 400px;
}
.save-btn {
  background: #52c41a;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}
.warn-btn {
  background: #faad14;
  color: white;
  border: none;
  padding: 10px 20px;
  border-radius: 4px;
  cursor: pointer;
}

/* 表格样式 */
.data-table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  margin-top: 10px;
}
.data-table th, .data-table td {
  padding: 12px;
  border-bottom: 1px solid #f0f0f0;
  text-align: left;
}
.text-truncate {
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.view-btn {
  background: #1890ff;
  color: white;
  border: none;
  padding: 4px 10px;
  border-radius: 2px;
  cursor: pointer;
}
.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}
.search-bar input {
  padding: 8px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
.refresh-btn {
  margin-left: auto;
  background: #722ed1;
  color: white;
  border: none;
  padding: 8px 15px;
  border-radius: 4px;
  cursor: pointer;
}

/* 弹窗样式 */
.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal {
  background: white;
  padding: 30px;
  width: 500px;
  border-radius: 8px;
}
.modal-text {
  background: #f9f9f9;
  padding: 15px;
  border-radius: 4px;
  margin: 15px 0;
  min-height: 100px;
}
</style>
