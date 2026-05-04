const ACCESS_TOKEN_KEY = 'fs_access_token'
const REFRESH_TOKEN_KEY = 'fs_refresh_token'

/** 获取 Access Token */
export function getAccessToken(): string | null {
  return localStorage.getItem(ACCESS_TOKEN_KEY)
}

/** 设置 Access Token */
export function setAccessToken(token: string): void {
  localStorage.setItem(ACCESS_TOKEN_KEY, token)
}

/** 获取 Refresh Token */
export function getRefreshToken(): string | null {
  return localStorage.getItem(REFRESH_TOKEN_KEY)
}

/** 设置 Refresh Token */
export function setRefreshToken(token: string): void {
  localStorage.setItem(REFRESH_TOKEN_KEY, token)
}

/** 清除所有 Token */
export function clearTokens(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
}

/** 检查是否已登录 */
export function isAuthenticated(): boolean {
  return !!getAccessToken()
}
