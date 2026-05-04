/** 用户信息 */
export interface UserInfo {
  userId: number
  account: string
  nickname: string
  userType: string
}

/** 登录请求 */
export interface LoginParams {
  account: string
  password: string
}

/** 注册请求 */
export interface RegisterParams {
  account: string
  password: string
  nickname: string
}

/** 登录响应 (user_backend) */
export interface LoginResult {
  accessToken: string
  refreshToken: string
  accessExpiresIn: number
  refreshExpiresIn: number
  userId: number
  account: string
  nickname: string
  userType: string
}

/** 用户类型枚举 */
export enum UserTypeEnum {
  NORMAL = 'NORMAL',
  ADVANCED = 'ADVANCED',
  VIP = 'VIP',
  ENTERPRISE = 'ENTERPRISE',
  ADMIN = 'ADMIN',
  SUPER_ADMIN = 'SUPER_ADMIN'
}

/** 判断是否为管理员 */
export function isAdmin(userType: string): boolean {
  return userType === UserTypeEnum.ADMIN || userType === UserTypeEnum.SUPER_ADMIN
}
