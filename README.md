# GdeiAssistant | 广东第二师范学院校园助手系统

![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg)
![JDK](https://img.shields.io/badge/JDK-17-blue.svg)
![Vue](https://img.shields.io/badge/Vue.js-3.x-4FC08D.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-orange.svg)

广东第二师范学院校园助手系统采用了网络爬虫模拟登录技术和 Spring Boot 4 以及 Vue 3 框架开发构建，实现了前后端分离和无状态架构。

项目适配了 Docker 容器化与 GraalVM Native Image 技术，支持以原生二进制文件运行，具有毫秒级启动与极低内存占用的优势。

---

## 📂 目录结构概览

```text
GdeiAssistant/                 # 仓库根目录
├── src/main/java/             # 后端 Java 源码
├── frontend/                  # 前端 Vue.js 源码
│   ├── src/
│   ├── package.json
│   └── vite.config.js
├── db-init/                   # 数据库初始化脚本
│   ├── mysql/                 # MySQL 结构与 Mock 数据
│   └── mongodb/               # MongoDB 初始化
├── docker-compose.yml         # 基础设施全栈编排
└── .env.template              # 环境变量配置模板

```

---

## 快速入门

**1. 环境**  
安装 Docker + Docker Compose。若需要本地前端开发，建议使用 Node.js `24.14.1 LTS`（仓库已提供 `.nvmrc`）。

**2. 配置**  
复制并编辑环境变量。**根目录 `.env` 默认只服务本地 `development`**，供 `./gradlew bootRun`、`docker compose up -d` 和本地前端联调用：

```bash
cp .env.template .env
```

填写本地开发所需的数据库密码、Redis、JWT 等（见模板注释）。环境语义统一为：

- `development`：本地开发，保持仓库内全栈 Docker 方案
- `staging`：演示 / 测试环境，推荐前后端与数据库拆开部署
- `production`：正式环境，推荐前后端与数据库拆开部署

> **当前约定：**
> - 根目录 `.env` 保持 `development` 默认值，优先指向本地 `localhost`
> - `staging` / `production` 不要直接复用本地 `.env`
> - 演示 / 生产请改用各自独立的环境变量配置，例如 Azure App Service App Settings、CI Secret、部署平台 Secret 文件

> **演示 / 生产外部数据库常用变量：**
> - MySQL：`DB_USERNAME`、`DB_JDBC_PARAMS`、`DB_JDBC_URL_APP`、`DB_JDBC_URL_LOG`、`DB_JDBC_URL_DATA`
> - Redis：`REDIS_USERNAME`、`REDIS_SSL_ENABLED`
> - MongoDB：`MONGO_URI`
>
> 当前 MySQL 仍默认使用三套 schema：`DB_NAME`、`DB_NAME_LOG`、`DB_NAME_DATA`。

> **生产环境必填变量：**
> - `JWT_SECRET` — JWT 签名密钥（至少 32 位随机串）
> - `CRON_SECRET` — 定时任务触发密钥（任意随机串）
> - `ENCRYPT_ENABLE=true` — 启用敏感字段加密
> - `ENCRYPT_PRIVATE_KEY` — AES 加密密钥
>
> 缺少以上变量时，生产环境会拒绝启动。

**3. 运行**

- **开发全栈（Docker）**：`docker compose up -d`（前后端 + MySQL + Redis + MongoDB）。
- **测试全栈编排**：`docker compose -f docker-compose-staging.yml up -d`。
- **生产全栈编排**：`docker compose -f docker-compose-prod.yml up -d`。
- **仅后端**：`./gradlew bootRun`（自动加载根目录 `.env`，按 `development` 语义启动）。
- **演示 / 生产推荐形态**：后端单独部署，前端单独构建，数据库改为外部服务；例如当前演示环境前端可使用 `https://gdeiassistant.pages.dev`，后端 API 使用 `https://gdeiassistant.azurewebsites.net/api`。
- **仅前端**：进入前端目录安装依赖并启动开发服务器：



```bash
cd frontend
npm install
npm run dev
```

前端开发服务器默认运行在 `http://localhost:5173`，需配置接口代理指向本地后端（如 `http://localhost:8080`），参见 `frontend/vite.config.*` 或 `frontend/.env*`。

> 如果你要在本机临时直连演示环境，请不要改仓库根目录 `.env` 的默认语义；更稳妥的做法是单独导出一次环境变量，或在 IDE / 部署平台里覆盖 `SPRING_PROFILES_ACTIVE`、数据库连接串和 `CORS_ALLOWED_ORIGIN_PATTERNS`。

更完整的环境矩阵见：`docs/environment-matrix.md`。

---

## 技术栈

| 端 | 技术 |
|----|------|
| 后端 | JDK 17、Spring Boot 4.0、MySQL 8.0、MyBatis-Plus、MongoDB、Redis、Gradle 8.14、GraalVM Native Image（可选） |
| 前端 | Vue 3、Vue Router、Vite、Axios、WeUI、Node.js 24.14.1 LTS |

---

## 预览

<p>
  <img src="assets/screenshots/function.png" width="250">
  <img src="assets/screenshots/ershou.png" width="250">
  <img src="assets/screenshots/secret.png" width="250">
</p>

---

## 项目结构

| 路径 | 说明 |
|------|------|
| `src/main/java` | Spring Boot 后端 |
| `frontend/` | Vue 前端 |
| `db-init/mysql/` | MySQL 初始化（含 Mock 数据） |
| `db-init/mongodb/` | MongoDB 初始化（含 Mock 数据） |
| `docker-compose.yml` | 全栈编排 |

---

## 测试账号

`gdeiassistant` / `gdeiassistant`（仅用于本地初始化或开发演示，见 `db-init/mysql/init.sql`，不要用于公网生产环境）。

---

## 客户端与官网

[官网](https://gdeiassistant.cn) · [Android](https://github.com/GdeiAssistant/GdeiAssistant-Android) · [iOS](https://github.com/GdeiAssistant/GdeiAssistant-iOS) · [微信小程序](https://github.com/GdeiAssistant/GdeiAssistant-WechatApp)

### 功能演示

- [前端网站](https://gdeiassistant.pages.dev)
- [后端接口](https://gdeiassistant.azurewebsites.net/actuator/health)

---

## 法律、隐私与安全提示

- [用户协议](frontend/src/views/about/UserAgreement.vue)
- [隐私政策](frontend/src/views/about/PrivacyPolicy.vue)
- [Cookie 与本地存储说明](frontend/src/views/about/CookiePolicy.vue)
- [社区准则](frontend/src/views/about/SocialPolicy.vue)
- [第三方服务清单](frontend/src/views/about/ThirdPartyServices.vue)
- [安全说明](frontend/src/views/about/Security.vue)
- [协议与政策版本记录](docs/policy-changelog.md)
- [SECURITY.md](SECURITY.md)
- [CONTRIBUTING.md](CONTRIBUTING.md)
- [TRADEMARK.md](TRADEMARK.md)
- [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md)

部署者或二次开发者需要注意：

- 本项目在实际部署中可能涉及校园账号、校园数据、短信、邮件、对象存储、AI OCR，以及云服务器、数据库、缓存、日志和备份等能力；是否启用、启用哪些服务以及是否发生境外处理，取决于实际部署、服务商区域和配置。部署者应根据实际情况完成必要的告知、授权、加密、日志脱敏、密钥管理和第三方服务清单维护，并同步更新隐私告知。
- 二次部署者不得使用原项目名称、Logo、域名或页面文案暗示自己是学校官方服务、原项目官方服务，或已获得学校官方授权、学校官方合作或原项目背书。
- 生产环境不得使用默认密钥，必须启用必要的敏感字段加密和 Secret 管理；包括但不限于 `JWT_SECRET`、`ENCRYPT_PRIVATE_KEY`、短信、邮件、AI、对象存储等相关密钥。

### 二次部署前检查清单

- 是否已替换默认密钥、`JWT_SECRET`、加密密钥、数据库密码和其他生产凭证。
- 是否已确认短信、邮件、对象存储、AI OCR、数据库、缓存、日志与备份等能力的实际启用情况。
- 是否已按实际启用的服务更新第三方服务清单、隐私告知和部署说明。
- 是否已确认校园账号凭证是否会被保存、如何加密、如何删除以及如何撤回授权。
- 是否已关闭调试日志、请求体输出和其他可能暴露密码、验证码、Token、手机号、邮箱或取件码的调试能力。
- 是否已配置 HTTPS、CORS、必要的 CSRF/XSS 防护、访问控制和备份策略。
- 是否已避免使用原项目名称、Logo、域名或页面文案暗示学校官方或原项目官方背书。
- 是否已准备用户投诉、隐私请求、侵权投诉和安全漏洞反馈的处理渠道。

---

## 许可证

[Apache License 2.0](LICENSE) · Copyright (c) 2016-2026 GdeiAssistant
