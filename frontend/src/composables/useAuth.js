import { ref, computed } from 'vue'
import { canAccessRoute, canWrite, roleLabel } from '../permissions'

const user = ref(null)

export function useAuth() {
  const roles = computed(() => user.value?.roles ?? [])

  function setUser(u) {
    user.value = u
  }

  function clearUser() {
    user.value = null
  }

  function hasRole(role) {
    return roles.value.includes(role)
  }

  function routeAllowed(path) {
    return canAccessRoute(path, roles.value)
  }

  function writeAllowed(module) {
    return canWrite(module, roles.value)
  }

  const displayRoles = computed(() => roleLabel(roles.value))

  return {
    user,
    roles,
    displayRoles,
    setUser,
    clearUser,
    hasRole,
    routeAllowed,
    writeAllowed
  }
}
