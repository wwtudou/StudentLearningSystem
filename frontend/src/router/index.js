// 前端路由配置
import { createRouter, createWebHistory } from 'vue-router'
import api from '../api'
import { canAccessRoute } from '../permissions'
import { useAuth } from '../composables/useAuth'
import HomeView from '../views/HomeView.vue'
import LoginView from '../views/LoginView.vue'
import StudentView from '../views/StudentView.vue'
import OrgView from '../views/OrgView.vue'
import DictView from '../views/DictView.vue'
import TeacherView from '../views/TeacherView.vue'
import CourseView from '../views/CourseView.vue'
import RewardView from '../views/RewardView.vue'
import EnrollmentView from '../views/EnrollmentView.vue'
import GradeView from '../views/GradeView.vue'
import ReportView from '../views/ReportView.vue'
import UserView from '../views/UserView.vue'
import DbTechView from '../views/DbTechView.vue'

const routes = [
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/', name: 'home', component: HomeView },
  { path: '/students', name: 'students', component: StudentView },
  { path: '/org', name: 'org', component: OrgView },
  { path: '/dicts', name: 'dicts', component: DictView },
  { path: '/teachers', name: 'teachers', component: TeacherView },
  { path: '/courses', name: 'courses', component: CourseView },
  { path: '/rewards', name: 'rewards', component: RewardView },
  { path: '/enrollments', name: 'enrollments', component: EnrollmentView },
  { path: '/grades', name: 'grades', component: GradeView },
  { path: '/reports', name: 'reports', component: ReportView },
  { path: '/db-tech', name: 'db-tech', component: DbTechView },
  { path: '/users', name: 'users', component: UserView }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.public) return true
  const { setUser, clearUser } = useAuth()
  try {
    const { data } = await api.get('/auth/me')
    if (data.code !== 0) {
      clearUser()
      return '/login'
    }
    setUser(data.data)
    const roles = data.data?.roles ?? []
    if (!canAccessRoute(to.path, roles)) {
      return roles.length ? '/' : '/login'
    }
    return true
  } catch (_) {
    clearUser()
    return '/login'
  }
})

export default router
