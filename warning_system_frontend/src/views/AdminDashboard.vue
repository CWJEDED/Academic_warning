<template>
  <div class="dashboard-container">
    <aside class="sidebar">
      <div class="logo">管理员后台</div>
      <nav>
        <div :class="['nav-item', tab === 'student' ? 'active' : '']" @click="loadData('student'); tab = 'student'">学生管理</div>
        <div :class="['nav-item', tab === 'teacher' ? 'active' : '']" @click="loadData('teacher'); tab = 'teacher'">教师管理</div>
        <div :class="['nav-item', tab === 'counsellor' ? 'active' : '']" @click="loadData('counsellor'); tab = 'counsellor'">辅导员管理</div>
        <div :class="['nav-item', tab === 'college' ? 'active' : '']" @click="loadData('college'); tab = 'college'">学院信息管理</div>
        <div :class="['nav-item', tab === 'subject' ? 'active' : '']" @click="loadData('subject'); tab = 'subject'">科目信息管理</div>
        <div :class="['nav-item', tab === 'warning' ? 'active' : '']" @click="loadData('warning'); tab = 'warning'">预警信息管理</div>
        <div :class="['nav-item', tab === 'rule' ? 'active' : '']" @click="loadData('rule'); tab = 'rule'">预警规则设置</div>
      </nav>
    </aside>

    <main class="main-content">
      <header class="header">
        <span class="welcome-text">管理员，您好</span>
        <div class="header-actions">
          <button class="text-btn" @click="openPwdModal">修改密码</button>
          <span class="divider">|</span>
          <button class="logout-btn" @click="logout">退出登录</button>
        </div>
      </header>

      <div class="content-body">

        <div class="toolbar">
          <h2>{{ getTitle() }}</h2>
          <div class="actions">
            <input v-if="['student','teacher','warning','rule'].includes(tab)"
                   v-model="searchText"
                   placeholder="搜索..."
                   class="search-input"/>

            <button v-if="['student','teacher','warning','rule'].includes(tab)" @click="loadData(tab)">查询</button>

            <button v-if="tab !== 'subject' && tab !== 'warning'" class="add-btn" @click="openModal('add')">新增</button>

            <button v-if="tab === 'warning'" class="refresh-btn" @click="triggerRefresh">刷新预警</button>
          </div>
        </div>

        <table class="data-table">
          <thead>
          <tr>
            <th v-for="col in columns" :key="col.prop">{{ col.label }}</th>
            <th v-if="tab !== 'subject'">操作</th>
          </tr>
          </thead>
          <tbody>
          <tr v-for="item in tableData" :key="item.id || item.studentId || item.teacherId">
            <td v-for="col in columns" :key="col.prop">
                <span v-if="col.isCollege">
                  {{ getCollegeName(item[col.prop]) }}
                </span>
              <span v-else>
                  {{ item[col.prop] }}
                </span>
            </td>
            <td v-if="tab !== 'subject'">
              <button class="edit-btn" @click="openModal('edit', item)">编辑</button>

              <button v-if="['student','teacher','counsellor'].includes(tab)"
                      class="reset-btn"
                      @click="resetUserPassword(item)"
                      title="重置为123456">
                重置密码
              </button>

              <button class="del-btn" @click="delItem(item)">删除</button>
            </td>
          </tr>
          </tbody>
        </table>
        <p v-if="tableData.length === 0" class="no-data">暂无数据</p>

      </div>
    </main>

    <div v-if="showModal" class="modal-overlay">
      <div class="modal">
        <h3>{{ modalType === 'add' ? '新增' : '编辑' }}</h3>
        <div class="modal-form">
          <div v-for="field in formFields" :key="field.key" class="form-group">
            <label>{{ field.label }}</label>

            <select v-if="field.type === 'select'"
                    v-model="form[field.key]"
                    :disabled="modalType === 'edit' && field.disabledOnEdit">
              <option disabled value="">请选择{{ field.label }}</option>
              <option v-for="c in collegeList" :key="c.collegeId" :value="c.collegeId">
                {{ c.collegeName }}
              </option>
            </select>

            <input v-else
                   v-model="form[field.key]"
                   :disabled="modalType === 'edit' && field.disabledOnEdit">
          </div>
        </div>
        <div class="modal-footer">
          <button class="save-btn" @click="saveItem">保存</button>
          <button class="cancel-btn" @click="showModal = false">取消</button>
        </div>
      </div>
    </div>

    <div v-if="showPwdModal" class="modal-overlay">
      <div class="modal" style="width: 400px;">
        <h3>修改管理员密码</h3>
        <div class="modal-form">
          <div class="form-group">
            <label>原密码</label>
            <input type="password" v-model="pwdForm.oldPassword" placeholder="请输入原密码">
          </div>
          <div class="form-group">
            <label>新密码</label>
            <input type="password" v-model="pwdForm.newPassword" placeholder="请输入新密码">
          </div>
        </div>
        <div class="modal-footer">
          <button class="save-btn" @click="submitChangePassword">确认修改</button>
          <button class="cancel-btn" @click="showPwdModal = false">取消</button>
        </div>
      </div>
    </div>

  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';

const router = useRouter();
const token = localStorage.getItem('token');
const tab = ref('student'); // 默认进学生管理，不再是 profile

const tableData = ref([]);
const collegeList = ref([]);
const searchText = ref('');

// 业务数据弹窗
const showModal = ref(false);
const modalType = ref('add');
const form = ref({});

// 修改密码弹窗
const showPwdModal = ref(false);
const pwdForm = ref({ oldPassword: '', newPassword: '' });

const api = axios.create({
  baseURL: 'http://localhost:8080/api/admin',
  headers: { 'Authorization': `Bearer ${token}` }
});

// 自动刷新逻辑
watch(searchText, (newVal) => {
  if (newVal === '') {
    loadData(tab.value);
  }
});

const getCollegeName = (idOrName) => {
  if (!idOrName) return '未分配';
  if (typeof idOrName === 'number' || !isNaN(Number(idOrName))) {
    const found = collegeList.value.find(c => c.collegeId === Number(idOrName));
    return found ? found.collegeName : idOrName;
  }
  return idOrName;
};

// === 页面配置定义 (移除了 password 字段) ===
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
  if (searchText.value) {
    params.name = searchText.value;
  }
  try {
    const res = await api.get(url, { params });
    tableData.value = res.data;
  } catch (e) { console.error(e); }
};

// 重置用户密码
const resetUserPassword = async (item) => {
  if (!confirm(`确定要重置该用户的密码为 123456 吗？`)) return;
  const c = config[tab.value];
  const id = item[c.idKey];
  try {
    // 调用后端: /admin/{type}/{id}/reset-password
    await api.post(`/${tab.value}/${id}/reset-password`);
    alert("密码重置成功");
  } catch (e) {
    alert(e.response?.data || "重置失败");
  }
};

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
    console.error(e);
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

// === 管理员修改自己密码 ===
const openPwdModal = () => {
  pwdForm.value = { oldPassword: '', newPassword: '' };
  showPwdModal.value = true;
};

const submitChangePassword = async () => {
  if(!pwdForm.value.oldPassword || !pwdForm.value.newPassword) return alert("请输入密码");
  try {
    await api.post('/change-password', {
      id: 1, // 管理员ID
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
  loadData('student'); // 默认加载学生数据
});
</script>

<style scoped>
.dashboard-container { display: flex; height: 100vh; font-family: sans-serif; }
.sidebar { width: 220px; background: #001529; color: white; display: flex; flex-direction: column; }
.logo { height: 60px; line-height: 60px; text-align: center; background: #002140; font-weight: bold; }
.nav-item { padding: 15px 20px; cursor: pointer; border-bottom: 1px solid #002140; transition: background 0.3s; }
.nav-item:hover, .nav-item.active { background: #1890ff; }
.main-content { flex: 1; display: flex; flex-direction: column; background: #f0f2f5; overflow: hidden; }

/* 顶部栏样式优化 */
.header { height: 60px; background: white; padding: 0 20px; display: flex; justify-content: space-between; align-items: center; box-shadow: 0 1px 4px rgba(0,0,0,0.1); }
.header-actions { display: flex; align-items: center; }
.divider { margin: 0 15px; color: #ddd; }
.text-btn { background: none; border: none; color: #1890ff; cursor: pointer; font-size: 14px; padding: 0; }
.text-btn:hover { text-decoration: underline; }
.logout-btn { background: #ff4d4f; color: white; border: none; padding: 5px 15px; border-radius: 4px; cursor: pointer; }

.content-body { flex: 1; padding: 20px; overflow-y: auto; }
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.actions { display: flex; gap: 10px; }
.search-input { padding: 6px; border: 1px solid #ddd; border-radius: 4px; width: 200px;}

.data-table { width: 100%; border-collapse: collapse; background: white; border-radius: 4px; overflow: hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.05); }
.data-table th, .data-table td { padding: 12px; border-bottom: 1px solid #f0f0f0; text-align: left; }
.no-data { text-align: center; color: #999; margin-top: 20px; }

button { padding: 6px 12px; cursor: pointer; border: none; border-radius: 4px; color: white; font-size: 13px; }
.add-btn { background: #52c41a; }
.edit-btn { background: #1890ff; margin-right: 5px; }
.reset-btn { background: #faad14; margin-right: 5px; } /* 重置密码黄色按钮 */
.del-btn { background: #ff4d4f; }
.refresh-btn { background: #722ed1; }
.save-btn { background: #1890ff; margin-right: 10px; }
.cancel-btn { background: #999; }

/* 弹窗 */
.modal-overlay { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; justify-content: center; align-items: center; z-index: 1000; }
.modal { background: white; padding: 25px; border-radius: 8px; width: 450px; }
.modal-form { display: flex; flex-direction: column; gap: 15px; margin: 20px 0; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group label { font-weight: bold; font-size: 14px; }
.form-group input, .form-group select { padding: 8px; border: 1px solid #ddd; border-radius: 4px; }
.modal-footer { display: flex; justify-content: flex-end; }
</style>
