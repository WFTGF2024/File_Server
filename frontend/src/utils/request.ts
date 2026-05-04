import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAccessToken, clearTokens } from '@/utils/auth'
import router from '@/router'

/** File Server Backend axios 实例 (port 9004) */
const fileServerRequest: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/** User Backend axios 实例 (port 9000, 通过 /api/auth 和 /api/users 代理) */
const userBackendRequest: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/** JWT Token 拦截器 */
function addTokenInterceptor(instance: AxiosInstance): void {
  instance.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
      const token = getAccessToken()
      if (token && config.headers) {
        config.headers['Authorization'] = `Bearer ${token}`
      }
      return config
    },
    (error) => Promise.reject(error)
  )
}

/** File Server 响应拦截器 */
function addFileServerResponseInterceptor(instance: AxiosInstance): void {
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      const res = response.data
      if (res.code !== 200) {
        ElMessage.error(res.message || '请求失败')
        // 401 未认证
        if (res.code === 401) {
          clearTokens()
          router.push('/login')
        }
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      return response
    },
    (error) => {
      if (error.response) {
        const status = error.response.status
        if (status === 401) {
          clearTokens()
          ElMessageBox.confirm('登录已过期，请重新登录', '提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            router.push('/login')
          })
        } else if (status === 403) {
          ElMessage.error('无权限访问')
        } else if (status === 404) {
          ElMessage.error('请求的资源不存在')
        } else {
          ElMessage.error(error.response.data?.message || '服务器错误')
        }
      } else if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时')
      } else {
        ElMessage.error('网络连接异常')
      }
      return Promise.reject(error)
    }
  )
}

/** User Backend 响应拦截器 */
function addUserBackendResponseInterceptor(instance: AxiosInstance): void {
  instance.interceptors.response.use(
    (response: AxiosResponse) => {
      const res = response.data
      if (!res.success) {
        ElMessage.error(res.message || '请求失败')
        return Promise.reject(new Error(res.message || '请求失败'))
      }
      return response
    },
    (error) => {
      if (error.response) {
        const status = error.response.status
        if (status === 401) {
          clearTokens()
          router.push('/login')
        } else {
          ElMessage.error(error.response.data?.message || '服务器错误')
        }
      } else {
        ElMessage.error('网络连接异常')
      }
      return Promise.reject(error)
    }
  )
}

// 应用拦截器
addTokenInterceptor(fileServerRequest)
addTokenInterceptor(userBackendRequest)
addFileServerResponseInterceptor(fileServerRequest)
addUserBackendResponseInterceptor(userBackendRequest)

export { fileServerRequest, userBackendRequest }

/** File Server GET 请求 */
export function get<T>(url: string, params?: Record<string, unknown>, config?: AxiosRequestConfig): Promise<T> {
  return fileServerRequest.get(url, { params, ...config }).then((res) => res.data.data as T)
}

/** File Server POST 请求 */
export function post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return fileServerRequest.post(url, data, config).then((res) => res.data.data as T)
}

/** File Server PUT 请求 */
export function put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
  return fileServerRequest.put(url, data, config).then((res) => res.data.data as T)
}

/** File Server DELETE 请求 */
export function del<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
  return fileServerRequest.delete(url, config).then((res) => res.data.data as T)
}

/** File Server 文件上传 (multipart/form-data) */
export function upload<T>(url: string, formData: FormData, onProgress?: (percent: number) => void): Promise<T> {
  return fileServerRequest.post(url, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (event) => {
      if (onProgress && event.total) {
        onProgress(Math.round((event.loaded / event.total) * 100))
      }
    }
  }).then((res) => res.data.data as T)
}

/** User Backend POST 请求 (登录/注册) */
export function userPost<T>(url: string, data?: unknown): Promise<T> {
  return userBackendRequest.post(url, data).then((res) => res.data.data as T)
}
