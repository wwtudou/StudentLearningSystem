/** 路由可见角色（对照需求 2.4 权限矩阵） */
export const ROUTE_ROLES = {
  '/': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER', 'STUDENT'],
  '/students': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER', 'STUDENT'],
  '/org': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER'],
  '/dicts': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER'],
  '/teachers': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER'],
  '/courses': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER'],
  '/rewards': ['SYS_ADMIN', 'DEPT_ADMIN', 'STUDENT'],
  '/enrollments': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER', 'STUDENT'],
  '/grades': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER', 'STUDENT'],
  '/reports': ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER', 'STUDENT'],
  '/db-tech': ['SYS_ADMIN'],
  '/users': ['SYS_ADMIN']
}

/** 模块写操作权限 */
export const WRITE_ROLES = {
  org: ['SYS_ADMIN', 'DEPT_ADMIN'],
  dict: ['SYS_ADMIN'],
  student: ['SYS_ADMIN', 'DEPT_ADMIN'],
  teacher: ['SYS_ADMIN', 'DEPT_ADMIN'],
  course: ['SYS_ADMIN', 'DEPT_ADMIN'],
  offering: ['SYS_ADMIN', 'DEPT_ADMIN', 'TEACHER'],
  reward: ['SYS_ADMIN', 'DEPT_ADMIN'],
  enrollment: ['STUDENT'],
  grade: ['SYS_ADMIN', 'TEACHER']
}

export function hasAnyRole(roles, allowed) {
  if (!roles?.length || !allowed?.length) return false
  return roles.some(r => allowed.includes(r))
}

export function canAccessRoute(path, roles) {
  const allowed = ROUTE_ROLES[path]
  if (!allowed) return true
  return hasAnyRole(roles, allowed)
}

export function canWrite(module, roles) {
  return hasAnyRole(roles, WRITE_ROLES[module] || [])
}

export function roleLabel(roles) {
  const map = {
    SYS_ADMIN: '系统管理员',
    DEPT_ADMIN: '院系管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return (roles || []).map(r => map[r] || r).join('、')
}
