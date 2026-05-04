import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, register as registerApi, getUserInfo as getUserInfoApi } from '@/api/auth'
import { setAccessToken, setRefreshToken, clearTokens, getAccessToken } from '@/utils/auth'
import type { UserInfo, LoginParams, RegisterParams } from '@/types/user'
import { isAdmin } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref<UserInfo | null>(null)
  const accessToken = ref<string | null>(getAccessToken())

  const isLoggedIn = computed(() => !!accessToken.value)
  const userType = computed(() => userInfo.value?.userType || '')
  const isAdminUser = computed(() => isAdmin(userType.value))
  const nickname = computed(() => userInfo.value?.nickname || '')
  const account = computed(() => userInfo.value?.account || '')
  const userId = computed(() => userInfo.value?.userId || 0)

  /** 登录 */
  async function login(params: LoginParams): Promise<void> {
    const result = await loginApi(params)
    accessToken.value = result.accessToken
    setAccessToken(result.accessToken)
    setRefreshToken(result.refreshToken)
    userInfo.value = {
      userId: result.userId,
      account: result.account,
      nickname: result.nickname,
      userType: result.userType
    }
  }

  /** 注册 */
  async function register(params: RegisterParams): Promise<void> {
    const result = await registerApi(params)
    accessToken.value = result.accessToken
    setAccessToken(result.accessToken)
    setRefreshToken(result.refreshToken)
    userInfo.value = {
      userId: result.userId,
      account: result.account,
      nickname: result.nickname,
      userType: result.userType
    }
  }

  /** 获取用户信息 */
  async function fetchUserInfo(): Promise<void> {
    if (!accessToken.value) return
    const info = await getUserInfoApi()
    userInfo.value = info
  }

  /** 登出 */
  function logout(): void {
    accessToken.value = null
    userInfo.value = null
    clearTokens()
  }

  return {
    userInfo,
    accessToken,
    isLoggedIn,
    userType,
    isAdminUser,
    nickname,
    account,
    userId,
    login,
    register,
    fetchUserInfo,
    logout
  }
})
