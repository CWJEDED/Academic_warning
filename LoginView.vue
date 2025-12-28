<template>
  <div class="login-container">
    <div class="circle circle-1"></div>
    <div class="circle circle-2"></div>

    <div class="login-box">
      <div class="login-header">
        <h2 class="title">学业预警系统</h2>
        <p class="subtitle">Academic Warning System</p>
      </div>

      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>账号 / ID</label>
          <div class="input-wrapper">
            <input
              type="number"
              v-model="form.username"
              required
              placeholder="请输入工号或学号"
              class="custom-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label>密码 / Password</label>
          <div class="input-wrapper">
            <input
              type="password"
              v-model="form.password"
              required
              placeholder="请输入登录密码"
              class="custom-input"
            />
          </div>
        </div>

        <div class="form-group">
          <label class="role-label">请选择您的身份</label>
          <div class="role-grid">
            <label :class="['role-card', form.role === 'student' ? 'active' : '']">
              <input type="radio" value="student" v-model="form.role" class="hidden-radio" />
              <span class="role-name">学生</span>
            </label>

            <label :class="['role-card', form.role === 'teacher' ? 'active' : '']">
              <input type="radio" value="teacher" v-model="form.role" class="hidden-radio" />
              <span class="role-name">教师</span>
            </label>

            <label :class="['role-card', form.role === 'counsellor' ? 'active' : '']">
              <input type="radio" value="counsellor" v-model="form.role" class="hidden-radio" />
              <span class="role-name">辅导员</span>
            </label>

            <label :class="['role-card', form.role === 'administrator' ? 'active' : '']">
              <input type="radio" value="administrator" v-model="form.role" class="hidden-radio" />
              <span class="role-name">管理员</span>
            </label>
          </div>
        </div>

        <transition name="fade">
          <p v-if="errorMessage" class="error-msg">
            <span class="icon">⚠️</span> {{ errorMessage }}
          </p>
        </transition>

        <button type="submit" class="submit-btn">
          立即登录
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const form = ref({
  username: '',
  password: '',
  role: 'student' // 默认选中学生
});
const errorMessage = ref('');

const handleLogin = async () => {
  try {
    // 发送请求到后端
    const res = await axios.post('http://localhost:8080/api/login', form.value);

    // 1. 获取 Token 并存储
    const token = res.data.token;
    localStorage.setItem('token', token);
    localStorage.setItem('role', res.data.role);
    localStorage.setItem('name', res.data.name);

    // 2. 根据角色跳转到对应页面
    switch (form.value.role) {
      case 'administrator': router.push('/admin-dashboard'); break;
      case 'student': router.push('/student-dashboard'); break;
      case 'teacher': router.push('/teacher-dashboard'); break;
      case 'counsellor': router.push('/counsellor-dashboard'); break;
    }
  } catch (error) {
    // 登录失败处理
    if (error.response && error.response.data) {
      errorMessage.value = error.response.data; // 显示后端返回的错误信息
    } else {
      errorMessage.value = "登录请求失败，请检查网络";
    }
  }
};
</script>

<style scoped>
/* 容器与背景 */
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
  font-family: 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
}

/* 背景装饰圆 */
.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  z-index: 1;
}
.circle-1 {
  width: 300px;
  height: 300px;
  top: -50px;
  left: -50px;
}
.circle-2 {
  width: 200px;
  height: 200px;
  bottom: 50px;
  right: -50px;
}

/* 登录框主体 */
.login-box {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 40px 50px;
  border-radius: 16px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  width: 420px;
  z-index: 2;
  transition: transform 0.3s ease;
}

/* 标题区域 */
.login-header {
  text-align: center;
  margin-bottom: 30px;
}
.title {
  margin: 0;
  color: #333;
  font-size: 28px;
  font-weight: 700;
  letter-spacing: 1px;
}
.subtitle {
  margin: 5px 0 0;
  color: #888;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 2px;
}

/* 表单通用 */
.form-group {
  margin-bottom: 20px;
}
.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #555;
  font-size: 14px;
  font-weight: 600;
}

/* 输入框美化 */
.input-wrapper {
  position: relative;
}
.custom-input {
  width: 100%;
  padding: 12px 15px;
  border: 2px solid #e1e1e1;
  border-radius: 8px;
  background: #f9f9f9;
  font-size: 15px;
  transition: all 0.3s ease;
  box-sizing: border-box; /* 确保 padding 不撑大宽度 */
}
.custom-input:focus {
  border-color: #667eea;
  background: #fff;
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

/* 角色选择网格 */
.role-label {
  margin-bottom: 10px;
}
.role-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}
.role-card {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 10px;
  border: 1px solid #e1e1e1;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: #fff;
  color: #666;
  font-size: 14px;
}
.hidden-radio {
  display: none;
}
/* 选中状态 */
.role-card.active {
  background: #667eea;
  color: white;
  border-color: #667eea;
  font-weight: bold;
  box-shadow: 0 4px 6px rgba(102, 126, 234, 0.3);
  transform: translateY(-2px);
}
.role-card:hover:not(.active) {
  background: #f0f2f5;
}

/* 登录按钮 */
.submit-btn {
  width: 100%;
  padding: 14px;
  margin-top: 10px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 16px;
  font-weight: bold;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px rgba(118, 75, 162, 0.3);
}
.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(118, 75, 162, 0.4);
}
.submit-btn:active {
  transform: translateY(0);
}

/* 错误提示 */
.error-msg {
  background: #fef0f0;
  color: #f56c6c;
  padding: 10px;
  border-radius: 6px;
  font-size: 13px;
  margin-bottom: 15px;
  border-left: 4px solid #f56c6c;
  display: flex;
  align-items: center;
}
.error-msg .icon {
  margin-right: 8px;
}

/* 动画 */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.3s;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
