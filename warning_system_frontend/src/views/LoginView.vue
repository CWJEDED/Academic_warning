<template>
  <div class="login-container">
    <div class="login-box">
      <h2>学业预警系统登录</h2>
      <form @submit.prevent="handleLogin">
        <div class="form-group">
          <label>账号 (ID):</label>
          <input type="number" v-model="form.username" required placeholder="请输入工号/学号" />
        </div>

        <div class="form-group">
          <label>密码:</label>
          <input type="password" v-model="form.password" required placeholder="请输入密码" />
        </div>

        <div class="form-group role-group">
          <label>身份选择:</label>
          <div class="radio-options">
            <label><input type="radio" value="administrator" v-model="form.role" /> 管理员</label>
            <label><input type="radio" value="teacher" v-model="form.role" /> 教师</label>
            <label><input type="radio" value="student" v-model="form.role" /> 学生</label>
            <label><input type="radio" value="counsellor" v-model="form.role" /> 辅导员</label>
          </div>
        </div>

        <p v-if="errorMessage" class="error">{{ errorMessage }}</p>

        <button type="submit">登录</button>
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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5;
}
.login-box {
  background: white;
  padding: 40px;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
  width: 400px;
}
.form-group {
  margin-bottom: 20px;
}
input[type="number"], input[type="password"] {
  width: 100%;
  padding: 10px;
  margin-top: 5px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}
.role-group {
  text-align: left;
}
.radio-options label {
  margin-right: 15px;
  cursor: pointer;
}
button {
  width: 100%;
  padding: 12px;
  background-color: #409eff;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 16px;
}
button:hover {
  background-color: #66b1ff;
}
.error {
  color: red;
  margin-bottom: 10px;
}
</style>
