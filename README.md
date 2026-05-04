# File_Server

基于 Spring Boot 3 + Vue 3 + TypeScript 的前后端分离文件存储和管理系统，提供用户认证、文件管理、文件夹组织、回收站、存储配额、策略管理及文件在线预览等功能。

## 项目结构

```
File_Server/
├── backend/                        # Spring Boot 后端
│   ├── src/main/java/com/example/fileserver/
│   │   ├── FileServerApplication.java   # 应用入口
│   │   ├── common/                      # 通用配置（Security、CORS、异常等）
│   │   ├── modules/                     # 业务模块
│   │   │   ├── auth/                    # 认证模块（JWT）
│   │   │   ├── file/                    # 文件管理模块
│   │   │   ├── folder/                  # 文件夹模块
│   │   │   ├── user/                    # 用户模块
│   │   │   └── policy/                  # 策略与配额模块
│   │   └── storage/                     # 文件存储服务
│   ├── src/main/resources/
│   │   └── application.yml             # 应用配置
│   └── pom.xml                         # Maven 依赖
│
├── frontend/                       # Vue 3 前端
│   ├── src/
│   │   ├── api/                    # API 接口层
│   │   ├── components/             # 组件（文件列表、预览、上传、文件夹树等）
│   │   ├── layout/                 # 布局组件
│   │   ├── router/                 # 路由配置
│   │   ├── stores/                 # Pinia 状态管理
│   │   ├── types/                  # TypeScript 类型定义
│   │   ├── utils/                  # 工具函数（请求封装、认证等）
│   │   ├── views/                  # 页面组件
│   │   ├── App.vue                 # 根组件
│   │   └── main.ts                 # 入口文件
│   ├── package.json
│   └── vite.config.ts              # Vite 配置（含代理）
│
└── README.md                       # 本文件
```

## 功能特性

### 用户系统
- JWT 认证（Access Token + Refresh Token）
- 用户注册和登录
- 角色管理（SUPER_ADMIN / ADMIN / NORMAL）

### 文件管理
- 文件上传（支持进度显示、拖拽上传）
- 文件下载
- 文件在线预览（图片、PDF、文本、Markdown、Word）
- 文件重命名、移动、删除
- 文件夹树形组织
- 回收站（软删除 + 恢复）
- 权限管理（公开/私有）

### 策略与配额
- 用户分类策略（存储容量、单文件大小、文件数量限制）
- 存储配额使用统计

### 文件预览
- **图片**：blob URL 内联显示
- **PDF**：fetch 带 Token 下载 → blob URL → iframe 内嵌
- **文本**（txt/log/json/xml/csv/yml 等）：fetch + 代码高亮显示
- **Markdown**：fetch + 简易渲染器（标题、代码块、链接、图片等）
- **Word**（docx）：mammoth.js 转 HTML 渲染

## 技术栈

### 后端
- **Java 17+**
- **Spring Boot 3.2** - Web 框架
- **Spring Security** - 认证与授权
- **MyBatis-Plus** - ORM 框架
- **PostgreSQL** - 主数据库
- **JWT (jjwt)** - Token 生成与验证

### 前端
- **Vue 3** - 渐进式 JavaScript 框架
- **TypeScript** - 类型安全
- **Vue Router** - 路由管理
- **Pinia** - 状态管理
- **Element Plus** - UI 组件库
- **Axios** - HTTP 客户端
- **mammoth.js** - Word 文件预览（docx → HTML）
- **Vite** - 构建工具

### 数据库
- **PostgreSQL** - 主数据库

## 快速开始

### 前提条件

- Java 17+
- Maven 3.8+
- Node.js 16+
- PostgreSQL 14+

### 1. 数据库设置

创建 PostgreSQL 数据库：

```sql
CREATE DATABASE authentication_db;
```

### 2. 后端设置

```bash
cd backend

# 修改 src/main/resources/application.yml 中的数据库连接配置
# 默认: localhost:5432/authentication_db, 用户: postgres

# 启动后端
mvn spring-boot:run
```

后端服务将运行在 `http://localhost:9004`

### 3. 前端设置

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将运行在 `http://localhost:8004`，API 请求通过 Vite 代理转发到后端。

### 4. 访问应用

打开浏览器访问 `http://localhost:8004` 即可使用应用。

## 部署

### 开发环境

后端和前端分别运行，使用 Vite 代理转发 API 请求。

### 生产环境

1. **构建后端**：
   ```bash
   cd backend
   mvn clean package -DskipTests
   java -jar target/fileserver-1.0.0.jar
   ```

2. **构建前端**：
   ```bash
   cd frontend
   npm run build
   ```

3. **部署前端**：将 `frontend/dist` 目录部署到 Nginx

4. **Nginx 配置示例**：
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;

       root /path/to/frontend/dist;
       index index.html;

       location / {
           try_files $uri $uri/ /index.html;
       }

       location /api/ {
           proxy_pass http://localhost:9004/;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
       }
   }
   ```

## 许可证

本项目仅供学习和研究使用。
