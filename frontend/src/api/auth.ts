import { userPost } from '@/utils/request'
import type { LoginParams, RegisterParams, LoginResult } from '@/types/user'

/** 用户登录 */
export function login(data: LoginParams): Promise<LoginResult> {
  return userPost<LoginResult>('/auth/login', data)
}

/** 用户注册 */
export function register(data: RegisterParams): Promise<LoginResult> {
  return userPost<LoginResult>('/auth/register', data)
}

/** 验证 Token */
export function validateToken(): Promise<boolean> {
  return userPost<boolean>('/auth/validate')
}

/** 获取当前用户信息 */
export function getUserInfo(): Promise<{ userId: number; account: string; nickname: string; userType: string }> {
  return userPost<{ userId: number; account: string; nickname: string; userType: string }>('/auth/user-info')
}

/** 刷新令牌 */
export function refreshToken(refreshToken: string): Promise<LoginResult> {
  return userPost<LoginResult>('/auth/refresh', { refreshToken })
}

/** 登出 */
export function logout(): Promise<void> {
  return userPost<void>('/auth/logout')
}
