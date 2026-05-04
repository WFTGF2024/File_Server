# File Server Backend API 文档

## 项目简介

File Server 是个人站点体系中的**文件服务**，负责文件存储管理、文件夹树、配额管理、策略管理、回收站、审计日志等功能。本服务复用 user_backend 的 JWT 认证体系，不独立实现登录注册。

- **框架**: Spring Boot 3.2.0 + MyBatis-Plus
- **语言**: Java 17
- **数据库**: PostgreSQL (authentication_db)
- **缓存**: Redis
- **认证**: 复用 user_backend JWT (port 9000)
- **端口**: 9004
- **Base URL**: `http://localhost:9004/api`

## 认证机制

所有接口（除分享公开接口外）需携带 JWT Token：

```
Authorization: Bearer <access_token>
```

Token 验证流程：File Server → user_backend POST /api/auth/validate → GET /api/auth/user-info

## 通用响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

---

## 用户端接口

### FileController (`/api/files`)

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 1 | GET | /api/files/home | 文件首页概览（文件夹树+策略+配额） | 是 |
| 2 | GET | /api/files/list | 文件列表（分页） | 是 |
| 3 | POST | /api/files/upload | 文件上传 | 是 |
| 4 | GET | /api/files/download/{id} | 文件下载（流式） | 是 |
| 5 | DELETE | /api/files/{id} | 删除文件（进回收站） | 是 |
| 6 | PUT | /api/files/rename | 重命名文件 | 是 |
| 7 | PUT | /api/files/move | 移动文件 | 是 |
| 8 | GET | /api/files/search | 搜索文件 | 是 |
| 9 | GET | /api/files/quota | 查询配额 | 是 |
| 10 | GET | /api/files/policy | 查询当前策略 | 是 |

#### GET /api/files/home

**响应**: Result\<FileHomeVO\>
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "folderTree": [...],
    "policy": {...},
    "quota": {
      "userId": 1,
      "totalStorageLimitBytes": 2147483648,
      "usedStorageBytes": 0,
      "maxSingleFileSizeBytes": 104857600,
      "maxBatchUploadCount": 5,
      "policySource": "category",
      "usagePercent": 0.0
    }
  }
}
```

#### GET /api/files/list

**参数**: parentFolderId (默认0), pageNum (默认1), pageSize (默认20)

**响应**: Result\<IPage\<FileVO\>\>
```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "fileName": "example.pdf",
        "fileExt": "pdf",
        "mimeType": "application/pdf",
        "fileSize": 1024000,
        "fileHash": "sha256...",
        "parentFolderId": 0,
        "description": "",
        "previewStatus": 0,
        "createdAt": "2025-01-01 00:00:00",
        "updatedAt": "2025-01-01 00:00:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 20
  }
}
```

#### POST /api/files/upload

**请求**: multipart/form-data
- file: MultipartFile (必填)
- parentFolderId: Long (默认0)
- description: String (可选)

**响应**: Result\<FileVO\>

#### GET /api/files/download/{id}

**响应**: 文件流 (application/octet-stream)

#### DELETE /api/files/{id}

**响应**: Result\<Void\>

#### PUT /api/files/rename

**请求体**:
```json
{
  "fileId": 1,
  "newName": "new_filename.pdf"
}
```

**响应**: Result\<FileVO\>

#### PUT /api/files/move

**请求体**:
```json
{
  "fileId": 1,
  "targetFolderId": 5
}
```

**响应**: Result\<FileVO\>

#### GET /api/files/search

**参数**: keyword (必填), pageNum (默认1), pageSize (默认20)

**响应**: Result\<IPage\<FileVO\>\>

#### GET /api/files/quota

**响应**: Result\<QuotaVO\>

#### GET /api/files/policy

**响应**: Result\<FsUserCategoryPolicy\>

---

### FolderController (`/api/folders`)

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 1 | POST | /api/folders/create | 创建文件夹 | 是 |
| 2 | GET | /api/folders/tree | 文件夹树 | 是 |
| 3 | GET | /api/folders/list | 文件夹列表 | 是 |
| 4 | DELETE | /api/folders/{id} | 删除文件夹 | 是 |
| 5 | PUT | /api/folders/rename | 重命名文件夹 | 是 |

#### POST /api/folders/create

**请求体**:
```json
{
  "folderName": "新文件夹",
  "parentId": 0
}
```

**响应**: Result\<FsFolder\>

#### GET /api/folders/tree

**响应**: Result\<List\<FolderTreeVO\>\>
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "folderName": "文档",
      "parentId": 0,
      "depth": 0,
      "folderPath": "/文档",
      "createdAt": "2025-01-01 00:00:00",
      "children": [
        {
          "id": 2,
          "folderName": "工作",
          "parentId": 1,
          "depth": 1,
          "folderPath": "/文档/工作",
          "createdAt": "2025-01-01 00:00:00",
          "children": []
        }
      ]
    }
  ]
}
```

---

### RecycleController (`/api/recycle`)

| # | 方法 | 路径 | 说明 | 认证 |
|---|------|------|------|------|
| 1 | GET | /api/recycle/list | 回收站列表 | 是 |
| 2 | POST | /api/recycle/restore/{id} | 恢复文件 | 是 |
| 3 | DELETE | /api/recycle/remove/{id} | 永久删除 | 是 |

#### GET /api/recycle/list

**参数**: pageNum (默认1), pageSize (默认20)

**响应**: Result\<IPage\<FsRecycleRecord\>\>

---

## 管理端接口

### AdminPolicyController (`/api/admin/policies`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 1 | GET | /api/admin/policies/list | 策略列表 | ADMIN |
| 2 | PUT | /api/admin/policies/update | 更新策略 | ADMIN |

### AdminQuotaController (`/api/admin/quotas`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 1 | GET | /api/admin/quotas/list | 配额列表 | ADMIN |
| 2 | PUT | /api/admin/quotas/update | 调整配额 | ADMIN |

### AdminFileController (`/api/admin/files`)

| # | 方法 | 路径 | 说明 | 权限 |
|---|------|------|------|------|
| 1 | GET | /api/admin/files/users | 获取用户文件列表 | ADMIN |
| 2 | GET | /api/admin/files/user/{userId} | 查看指定用户文件 | ADMIN |
| 3 | DELETE | /api/admin/files/force-delete/{id} | 强制删除文件 | ADMIN |
| 4 | GET | /api/admin/files/audit | 审计日志 | ADMIN |
| 5 | GET | /api/admin/files/system-storage | 系统存储概览 | ADMIN |

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或Token无效 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 文件不存在 |
| 1002 | 文件已存在 |
| 1003 | 文件上传失败 |
| 1004 | 文件下载失败 |
| 1006 | 文件大小超出限制 |
| 1007 | 文件类型不允许 |
| 1008 | 存储空间不足 |
| 1009 | 批量上传数量超出限制 |
| 1010 | 文件名不合法 |
| 2001 | 文件夹不存在 |
| 2002 | 同名文件夹已存在 |
| 2003 | 文件夹深度超出限制 |
| 3001 | 策略不存在 |
| 4002 | 配额不足 |
| 5001 | 回收站记录不存在 |
| 6001 | Token无效 |
| 6004 | 认证服务不可用 |
| 7001 | 管理员权限不足 |
