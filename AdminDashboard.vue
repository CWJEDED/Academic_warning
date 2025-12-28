<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo-area">
        <div class="logo-icon">🛡️</div>
        <div class="logo-text">管理员后台</div>
      </div>
      <nav>
        <div :class="['nav-item', tab === 'student' ? 'active' : '']" @click="loadData('student'); tab = 'student'">
          <span class="icon">👨‍🎓</span> 学生管理
        </div>
        <div :class="['nav-item', tab === 'teacher' ? 'active' : '']" @click="loadData('teacher'); tab = 'teacher'">
          <span class="icon">👨‍🏫</span> 教师管理
        </div>
        <div :class="['nav-item', tab === 'counsellor' ? 'active' : '']" @click="loadData('counsellor'); tab = 'counsellor'">
          <span class="icon">🧑‍💼</span> 辅导员管理
        </div>
        <div :class="['nav-item', tab === 'college' ? 'active' : '']" @click="loadData('college'); tab = 'college'">
          <span class="icon">🏛️</span> 学院管理
        </div>
        <div :class="['nav-item', tab === 'subject' ? 'active' : '']" @click="loadData('subject'); tab = 'subject'">
          <span class="icon">📖</span> 科目管理
        </div>
        <div :class="['nav-item', tab === 'warning' ? 'active' : '']" @click="loadData('warning'); tab = 'warning'">
          <span class="icon">⚠️</span> 预警信息
        </div>
        <div :class="['nav-item', tab === 'rule' ? 'active' : '']" @click="loadData('rule'); tab = 'rule'">
          <span class="icon">⚙️</span> 预警规则
        </div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <div class="breadcrumb">首页 / {{ getTabName(tab) }}</div>
        <div class="header-actions">
          <span class="welcome-text">管理员，您好</span>
          <span class="divider"></span>
          <button class="text-btn" @click="openPwdModal">🔒 修改密码</button>
          <span class="divider"></span>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </header>

      <div class="content-body">

        <transition name="fade" mode="out-in">
          <div :key="tab">
            <div class="toolbar-card">
              <div class="toolbar-title">{{ getTitle() }}</div>
              <div class="actions">
                <div class="search-box" v-if="['student','teacher','warning','rule'].includes(tab)">
                  <input
                    v-model="searchText"
                    placeholder="请输入名称搜索..."
                    class="search-input"
                  />
                  <button class="search-btn" @click="loadData(tab)">🔍</button>
                </div>

                <button v-if="tab !== 'warning'" class="btn primary-btn" @click="openModal('add')">
                  <span>+</span> 新增
                </button>

                <button v-if="tab === 'warning'" class="btn refresh-btn" @click="triggerRefresh">
                  🔄 刷新
                </button>
              </div>
            </div>

            <div class="table-card">
              <table class="modern-table">
                <thead>
                <tr>
                  <th v-for="col in columns" :key="col.prop">{{ col.label }}</th>
                  <th v-if="tab !== 'subject'" :width="tab === 'teacher' ? '240px' : '200px'">操作</th>
                </tr>
                </thead>
                <tbody>
                <tr v-for="item in tableData" :key="item.id || item.studentId || item.teacherId">
                  <td v-for="col in columns" :key="col.prop">
                      <span v-if="col.isCollege" class="tag college-tag">
                        {{ getCollegeName(item[col.prop]) }}
                      </span>
                    <span v-else>
                        {{ item[col.prop] }}
                      </span>
                  </td>
                  <td v-if="tab !== 'subject'">
                    <div class="action-group">
                      <button v-if="tab === 'teacher'"
                              class="icon-btn assign"
                              @click="openAssignModal(item)"
                              title="分配所属学院课程">
                        📚
                      </button>

                      <button class="icon-btn edit" @click="openModal('edit', item)" title="编辑">✏️</button>

                      <button v-if="['student','teacher','counsellor'].includes(tab)"
                              class="icon-btn reset"
                              @click="resetUserPassword(item)"
                              title="重置用户密码">
                        🔑
                      </button>

                      <button class="icon-btn delete" @click="delItem(item)" title="删除">🗑️</button>
                    </div>
                  </td>
                </tr>
                </tbody>
              </table>
              <div v-if="tableData.length === 0" class="empty-state">
                暂无数据 📭
              </div>
            </div>
          </div>
        </transition>

      </div>
    </main>

    <transition name="fade">
      <div v-if="showModal" class="modal-overlay">
        <div class="modal">
          <div class="modal-header">
            <h3>{{ modalType === 'add' ? '✨ 新增数据' : '✏️ 编辑数据' }}</h3>
            <button class="close-icon" @click="showModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-layout">
              <div v-for="field in formFields" :key="field.key" class="form-group">
                <label>{{ field.label }}</label>

                <select v-if="field.type === 'select'"
                        v-model="form[field.key]"
                        class="input-field"
                        :disabled="modalType === 'edit' && field.disabledOnEdit">
                  <option disabled value="">请选择{{ field.label }}</option>
                  <option v-for="c in collegeList" :key="c.collegeId" :value="c.collegeId">
                    {{ c.collegeName }}
                  </option>
                </select>

                <input v-else
                       v-model="form[field.key]"
                       class="input-field"
                       :disabled="modalType === 'edit' && field.disabledOnEdit">
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn secondary-btn" @click="showModal = false">取消</button>
            <button class="btn primary-btn" @click="saveItem">保存</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="showPwdModal" class="modal-overlay">
        <div class="modal sm-modal">
          <div class="modal-header">
            <h3>🔒 修改密码</h3>
            <button class="close-icon" @click="showPwdModal = false">×</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label>原密码</label>
              <div class="pwd-wrapper">
                <input
                  :type="showOldPwd ? 'text' : 'password'"
                  v-model="pwdForm.oldPassword"
                  placeholder="请输入原密码"
                  class="input-field pwd-input"
                  autocomplete="new-password"
                >
                <span class="eye-icon" @click="showOldPwd = !showOldPwd" title="显示/隐藏密码">
                  {{ showOldPwd ? '🙈' : '👁️' }}
                </span>
              </div>
            </div>
            <div class="form-group">
              <label>新密码</label>
              <div class="pwd-wrapper">
                <input
                  :type="showNewPwd ? 'text' : 'password'"
                  v-model="pwdForm.newPassword"
                  placeholder="请输入新密码"
                  class="input-field pwd-input"
                  autocomplete="new-password"
                >
                <span class="eye-icon" @click="showNewPwd = !showNewPwd" title="显示/隐藏密码">
                  {{ showNewPwd ? '🙈' : '👁️' }}
                </span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn secondary-btn" @click="showPwdModal = false">取消</button>
            <button class="btn warning-btn" @click="submitChangePassword">确认修改</button>
          </div>
        </div>
      </div>
    </transition>

    <transition name="fade">
      <div v-if="showAssignModal" class="modal-overlay">
        <div class="modal sm-modal">
          <div class="modal-header">
            <h3>📚 分配课程</h3>
            <button class="close-icon" @click="showAssignModal = false">×</button>
          </div>
          <div class="modal-body">
            <p style="margin-bottom: 15px; color: #64748b;">
              正在为教师 <strong>{{ assignForm.teacherName }}</strong> 分配负责课程。
            </p>
            <div class="form-group">
              <label>选择科目 (仅限 {{ assignForm.collegeName }})</label>
              <select v-model="assignForm.subject" class="input-field">
                <option disabled value="">请选择要分配的科目</option>
                <option v-for="s in availableSubjects" :key="s.id" :value="s.name">
                  {{ s.name }} ({{ s.credit }}学分)
                </option>
              </select>
              <p v-if="availableSubjects.length === 0" style="color: red; font-size: 12px; margin-top: 5px;">
                该学院暂无可选科目，请先在“科目管理”中添加。
              </p>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn secondary-btn" @click="showAssignModal = false">取消</button>
            <button class="btn primary-btn" @click="submitAssignCourse">确认分配</button>
          </div>
        </div>
      </div>
    </transition>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const token = localStorage.getItem('token');
const tab = ref('student');

const pwdForm = ref({ oldPassword: '', newPassword: '' });
// === 新增：密码显示状态 ===
const showOldPwd = ref(false);
const showNewPwd = ref(false);

const tableData = ref([]);
const collegeList = ref([]);
const searchText = ref('');

// 通用数据弹窗
const showModal = ref(false);
const modalType = ref('add');
const form = ref({});

// 修改密码弹窗
const showPwdModal = ref(false);

// === 新增：分配课程相关状态 ===
const showAssignModal = ref(false);
const assignForm = ref({ teacherId: '', teacherName: '', collegeName: '', subject: '' });
const availableSubjects = ref([]);

const api = axios.create({
  baseURL: 'http://localhost:8080/api/admin',
  headers: { 'Authorization': `Bearer ${token}` }
});

watch(searchText, (newVal) => {
  if (newVal === '') loadData(tab.value);
});

// 辅助函数
const getTabName = (key) => {
  const map = {
    'student': '学生管理', 'teacher': '教师管理', 'counsellor': '辅导员管理',
    'college': '学院管理', 'subject': '科目管理', 'warning': '预警信息', 'rule': '预警规则'
  };
  return map[key] || '管理后台';
};

const getCollegeName = (idOrName) => {
  if (!idOrName) return '未分配';
  if (typeof idOrName === 'number' || !isNaN(Number(idOrName))) {
    const found = collegeList.value.find(c => c.collegeId === Number(idOrName));
    return found ? found.collegeName : idOrName;
  }
  return idOrName;
};

// 页面配置
const config = {
  student: {
    title: '学生管理',
    cols: [
      { label: '学号', prop: 'studentId' },
      { label: '姓名', prop: 'studentName' },
      { label: '所属学院', prop: 'collegeId', isCollege: true },
      { label: '班级', prop: 'className' },
      { label: '手机', prop: 'phone' }
    ],
    fields: [
      { key: 'studentName', label: '姓名' },
      { key: 'gender', label: '性别' },
      { key: 'collegeId', label: '所属学院', type: 'select' },
      { key: 'className', label: '班级' },
      { key: 'grade', label: '年级' },
      { key: 'phone', label: '手机' },
      { key: 'email', label: '邮箱' }
    ],
    idKey: 'studentId'
  },
  teacher: {
    title: '教师管理',
    cols: [
      { label: '工号', prop: 'teacherId' },
      { label: '姓名', prop: 'teacherName' },
      { label: '所属学院', prop: 'collegeId', isCollege: true },
      { label: '手机', prop: 'phone' }
    ],
    fields: [
      { key: 'teacherName', label: '姓名' },
      { key: 'gender', label: '性别' },
      { key: 'collegeId', label: '所属学院', type: 'select' },
      { key: 'phone', label: '手机' },
      { key: 'email', label: '邮箱' }
    ],
    idKey: 'teacherId'
  },
  counsellor: {
    title: '辅导员管理',
    cols: [
      { label: '工号', prop: 'counsellorId' },
      { label: '姓名', prop: 'counsellorName' },
      { label: '所属学院', prop: 'collegeId', isCollege: true },
      { label: '手机', prop: 'phone' }
    ],
    fields: [
      { key: 'counsellorName', label: '姓名' },
      { key: 'collegeId', label: '所属学院', type: 'select' },
      { key: 'phone', label: '手机' },
      { key: 'email', label: '邮箱' }
    ],
    idKey: 'counsellorId'
  },
  college: {
    title: '学院信息管理',
    cols: [
      { label: '编号', prop: 'collegeId' },
      { label: '学院名称', prop: 'collegeName' }
    ],
    fields: [
      { key: 'collegeName', label: '学院名称' }
    ],
    idKey: 'collegeId'
  },
  subject: {
    title: '科目信息管理',
    cols: [
      { label: 'ID', prop: 'id' },
      { label: '科目名称', prop: 'name' },
      { label: '所属学院', prop: 'collegeName', isCollege: true },
      { label: '学分', prop: 'credit' }
    ],
    fields: [
      { key: 'name', label: '科目名称' },
      { key: 'credit', label: '学分' },
      { key: 'collegeName', label: '所属学院', type: 'select' }
    ],
    idKey: 'id'
  },
  warning: {
    title: '预警信息管理',
    cols: [
      { label: 'ID', prop: 'id' },
      { label: '标题', prop: 'title' },
      { label: '学生', prop: 'studentName' },
      { label: '内容', prop: 'text' }
    ],
    fields: [
      { key: 'title', label: '标题' },
      { key: 'text', label: '内容' }
    ],
    idKey: 'id'
  },
  rule: {
    title: '预警规则设置',
    cols: [
      { label: 'ID', prop: 'id' },
      { label: '学院名称', prop: 'collegeName' },
      { label: '缺勤阈值', prop: 'attendence' },
      { label: '挂科阈值', prop: 'failure' }
    ],
    fields: [
      { key: 'collegeName', label: '学院名称' },
      { key: 'attendence', label: '缺勤阈值' },
      { key: 'failure', label: '挂科阈值' }
    ],
    idKey: 'id'
  }
};

const getTitle = () => config[tab.value]?.title;
const columns = computed(() => config[tab.value]?.cols || []);
const formFields = computed(() => config[tab.value]?.fields || []);

// === API 操作 ===

const loadColleges = async () => {
  try {
    const res = await api.get('/colleges');
    collegeList.value = res.data;
  } catch (e) { console.error("加载学院失败", e); }
};

const loadData = async (type) => {
  let url = `/${type}s`;
  let params = {};
  if (searchText.value) params.name = searchText.value;
  try {
    const res = await api.get(url, { params });
    tableData.value = res.data;
  } catch (e) { console.error(e); }
};

const resetUserPassword = async (item) => {
  if (!confirm(`确定要重置该用户的密码为 123456 吗？`)) return;
  const c = config[tab.value];
  const id = item[c.idKey];
  try {
    await api.post(`/${tab.value}/${id}/reset-password`);
    alert("密码重置成功");
  } catch (e) {
    alert(e.response?.data || "重置失败");
  }
};

// === 分配课程逻辑 ===
const openAssignModal = async (teacher) => {
  if (!teacher.collegeId) return alert("该教师未分配学院，无法分配课程");

  assignForm.value = {
    teacherId: teacher.teacherId,
    teacherName: teacher.teacherName,
    collegeName: getCollegeName(teacher.collegeId),
    subject: ''
  };

  try {
    const res = await api.get('/subjects');
    const targetCollegeName = getCollegeName(teacher.collegeId);
    availableSubjects.value = res.data.filter(s => s.collegeName === targetCollegeName);

    showAssignModal.value = true;
  } catch (e) {
    alert("获取科目列表失败");
  }
};

const submitAssignCourse = async () => {
  if (!assignForm.value.subject) return alert("请选择科目");

  try {
    await api.post('/assign-course', {
      teaId: assignForm.value.teacherId,
      teaName: assignForm.value.teacherName,
      subject: assignForm.value.subject
    });
    alert("课程分配成功！");
    showAssignModal.value = false;
  } catch (e) {
    alert(e.response?.data || "分配失败，请稍后重试");
  }
};

// === 常规增删改逻辑 ===
const openModal = (type, item) => {
  modalType.value = type;
  form.value = type === 'edit' ? { ...item } : {};
  if(type === 'add' && config[tab.value].fields.some(f => f.type === 'select')) {
    config[tab.value].fields.forEach(f => {
      if(f.type === 'select') form.value[f.key] = '';
    });
  }
  if (type === 'edit' && tab.value === 'subject') {
    const cName = item.collegeName;
    const found = collegeList.value.find(c => c.collegeName === cName);
    if (found) form.value.collegeName = found.collegeId;
  }
  showModal.value = true;
};

const saveItem = async () => {
  const c = config[tab.value];
  const url = `/${tab.value}s`;
  try {
    if (form.value.collegeName && typeof form.value.collegeName === 'number') {
      form.value.collegeName = String(form.value.collegeName);
    }
    if (modalType.value === 'add') {
      await api.post(url, form.value);
    } else {
      await api.put(url, form.value);
    }
    alert("保存成功");
    showModal.value = false;
    loadData(tab.value);
    if(tab.value === 'college') loadColleges();
  } catch (e) {
    alert("操作失败: " + (e.response?.data?.message || e.message));
  }
};

const delItem = async (item) => {
  if (!confirm("确认删除？")) return;
  const c = config[tab.value];
  const id = item[c.idKey];
  try {
    await api.delete(`/${tab.value}s/${id}`);
    alert("删除成功");
    loadData(tab.value);
    if(tab.value === 'college') loadColleges();
  } catch (e) { alert("删除失败"); }
};

const openPwdModal = () => {
  pwdForm.value = { oldPassword: '', newPassword: '' };
  showOldPwd.value = false;
  showNewPwd.value = false;
  showPwdModal.value = true;
};

const submitChangePassword = async () => {
  if(!pwdForm.value.oldPassword || !pwdForm.value.newPassword) return alert("请输入密码");
  try {
    await api.post('/change-password', {
      id: 1,
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword
    });
    alert("修改成功");
    showPwdModal.value = false;
  } catch (e) { alert("修改失败：" + (e.response?.data || "未知错误")); }
};

const triggerRefresh = () => {
  alert("系统预警信息已刷新");
  loadData('warning');
};

const logout = () => { localStorage.clear(); router.push('/'); };

onMounted(() => {
  if (!token) { router.push('/'); return; }
  loadColleges();
  loadData('student');
});
</script>

<style scoped>
/* 全局变量 */
:root {
  --primary: #3b82f6;
  --secondary: #64748b;
  --success: #10b981;
  --warning: #f59e0b;
  --danger: #ef4444;
  --bg-dark: #0f172a;
  --bg-light: #f1f5f9;
}

.dashboard-container { display: flex; height: 100vh; font-family: 'Segoe UI', 'Roboto', Helvetica, Arial, sans-serif; background-color: #f1f5f9; color: #334155; }

/* 侧边栏 */
.sidebar { width: 240px; background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%); color: white; display: flex; flex-direction: column; box-shadow: 4px 0 10px rgba(0,0,0,0.1); z-index: 10; }
.logo-area { height: 70px; display: flex; align-items: center; justify-content: center; border-bottom: 1px solid rgba(255,255,255,0.1); gap: 10px; }
.logo-icon { font-size: 24px; }
.logo-text { font-size: 18px; font-weight: 700; letter-spacing: 1px; }

.nav-item { padding: 16px 24px; cursor: pointer; transition: all 0.3s; display: flex; align-items: center; gap: 12px; font-size: 14px; color: #94a3b8; border-left: 4px solid transparent; }
.nav-item:hover { background: rgba(255,255,255,0.05); color: #fff; }
.nav-item.active { background: rgba(59, 130, 246, 0.1); color: #fff; border-left-color: #3b82f6; font-weight: 600; }
.icon { width: 24px; text-align: center; font-size: 16px; }

/* 主内容区 */
.main-content { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.header { height: 64px; background: white; padding: 0 32px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 3px rgba(0,0,0,0.05); z-index: 5; }
.breadcrumb { font-size: 14px; color: #64748b; }
.header-actions { display: flex; align-items: center; gap: 12px; }
.welcome-text { font-size: 14px; font-weight: 500; color: #334155; }
.divider { width: 1px; height: 16px; background: #cbd5e1; }

.text-btn { background: none; border: none; color: #3b82f6; cursor: pointer; font-size: 14px; font-weight: 500; padding: 0; transition: color 0.2s; }
.text-btn:hover { color: #2563eb; text-decoration: underline; }

.logout-btn { padding: 6px 16px; border: 1px solid #fee2e2; background: #fff; color: #ef4444; border-radius: 6px; cursor: pointer; font-size: 13px; transition: all 0.2s; }
.logout-btn:hover { background: #fef2f2; }

.content-body { padding: 24px; overflow-y: auto; }

/* 工具栏卡片 */
.toolbar-card { background: white; padding: 16px 24px; border-radius: 12px; box-shadow: 0 1px 3px rgba(0,0,0,0.05); display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.toolbar-title { font-size: 18px; font-weight: 600; color: #1e293b; }
.actions { display: flex; gap: 12px; }

.search-box { display: flex; background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 4px; }
.search-input { border: none; background: transparent; padding: 6px 12px; outline: none; width: 200px; font-size: 14px; }
.search-btn { background: white; border: 1px solid #e2e8f0; border-radius: 6px; cursor: pointer; padding: 0 10px; }

/* 按钮通用 */
.btn { padding: 8px 16px; border-radius: 8px; border: none; cursor: pointer; font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 6px; transition: transform 0.1s, box-shadow 0.2s; }
.btn:active { transform: scale(0.98); }
.primary-btn { background: #3b82f6; color: white; box-shadow: 0 2px 4px rgba(59, 130, 246, 0.3); }
.primary-btn:hover { background: #2563eb; }
.secondary-btn { background: #f1f5f9; color: #475569; }
.secondary-btn:hover { background: #e2e8f0; }
.warning-btn { background: #f59e0b; color: white; }
.refresh-btn { background: #8b5cf6; color: white; }

/* 表格卡片 */
.table-card { background: white; border-radius: 12px; padding: 0; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.05); overflow: hidden; }
.modern-table { width: 100%; border-collapse: collapse; }
.modern-table th { background: #f8fafc; color: #475569; font-weight: 600; text-align: left; padding: 16px 24px; border-bottom: 1px solid #e2e8f0; font-size: 14px; }
.modern-table td { padding: 16px 24px; border-bottom: 1px solid #f1f5f9; color: #334155; font-size: 14px; }
.modern-table tr:hover { background: #f8fafc; }

/* 标签与操作按钮 */
.tag { padding: 4px 10px; border-radius: 20px; font-size: 12px; font-weight: 500; }
.college-tag { background: #e0f2fe; color: #0284c7; }

.action-group { display: flex; gap: 8px; }
.icon-btn { width: 32px; height: 32px; border-radius: 6px; border: 1px solid transparent; background: transparent; cursor: pointer; transition: all 0.2s; display: flex; align-items: center; justify-content: center; font-size: 16px; }
.icon-btn:hover { background: #f1f5f9; border-color: #e2e8f0; }
.icon-btn.assign { color: #10b981; } /* 分配课程按钮颜色 */
.icon-btn.edit { color: #3b82f6; }
.icon-btn.reset { color: #f59e0b; }
.icon-btn.delete { color: #ef4444; }

.empty-state { padding: 40px; text-align: center; color: #94a3b8; }

/* 弹窗 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.4); backdrop-filter: blur(4px); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: white; width: 500px; border-radius: 16px; box-shadow: 0 20px 25px -5px rgba(0,0,0,0.1); overflow: hidden; animation: slideIn 0.3s ease; }
.sm-modal { width: 400px; }
.modal-header { padding: 20px 24px; border-bottom: 1px solid #f1f5f9; display: flex; justify-content: space-between; align-items: center; }
.modal-header h3 { margin: 0; font-size: 18px; color: #1e293b; }
.close-icon { background: none; border: none; font-size: 24px; color: #94a3b8; cursor: pointer; }
.modal-body { padding: 24px; }
.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 8px; font-size: 14px; font-weight: 500; color: #475569; }
.input-field { width: 100%; padding: 10px 12px; border: 1px solid #cbd5e1; border-radius: 8px; outline: none; font-size: 14px; transition: border-color 0.2s; box-sizing: border-box; }
.input-field:focus { border-color: #3b82f6; box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1); }
.modal-footer { padding: 16px 24px; background: #f8fafc; display: flex; justify-content: flex-end; gap: 12px; }

/* 动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.2s; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
@keyframes slideIn { from { transform: translateY(20px); opacity: 0; } to { transform: translateY(0); opacity: 1; } }

/* === 修改：修复眼睛图标位置并隐藏默认图标 === */
.pwd-wrapper {
  position: relative;
  width: 100%;
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
