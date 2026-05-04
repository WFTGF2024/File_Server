/** 统一响应结构 - File Server Backend */
export interface Result<T> {
  code: number
  message: string
  data: T
}

/** 统一响应结构 - User Backend */
export interface UserBackendResult<T> {
  success: boolean
  message: string
  data: T
  timestamp: number
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  size: number
}

/** 分页请求参数 */
export interface PageParams {
  pageNum?: number
  pageSize?: number
}
