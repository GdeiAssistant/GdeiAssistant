# Environment Matrix

## Canonical profiles

GdeiAssistant 统一使用以下三套环境语义：

| Profile | 用途 | 典型入口 |
| --- | --- | --- |
| `development` | 本地开发、联调、快速调试 | `./gradlew bootRun`、`docker compose up -d` |
| `staging` | 测试环境、上线前验证、演示环境 | Azure App Service / 单独后端容器，数据库外置；也可继续使用 `docker compose -f docker-compose-staging.yml up -d` |
| `production` | 正式环境 | 推荐前后端与数据库拆开部署；也可继续使用 `docker compose -f docker-compose-prod.yml up -d` |

## Default URLs

| 环境 | 后端 API | 前端入口 |
| --- | --- | --- |
| `development` | `http://localhost:8080/api` | `http://localhost:5173` |
| `staging` | `https://gdeiassistant.azurewebsites.net/api` 或你的演示域名 | `https://gdeiassistant.pages.dev` 或你的演示前端域名；`VITE_APP_BASE_API` 指向演示后端 |
| `production` | `https://gdeiassistant.cn/api` 或正式域名 | 推荐独立部署，前端通过 `/api` 或绝对地址访问后端 |

## Security expectations

- `development` 默认允许 `ENCRYPT_ENABLE=false`
- `staging` / `production` 必须显式提供：
  - `DB_PASSWORD`
  - `JWT_SECRET`
  - `CRON_SECRET`
  - `ENCRYPT_ENABLE=true`
  - `ENCRYPT_PRIVATE_KEY`

## Notes

- `staging` 用来验证上线前配置和 fail-fast 行为，因此 compose 配置与 `production` 保持一致的密钥要求。
- 当前仅 `production` 加载定时任务与生产专用 cron Bean；`staging` 默认不启动这些任务，避免测试环境误触真实后台任务。

## Deployment shape

- `development`：保持现状，直接使用仓库内 `docker-compose.yml`，前后端和 MySQL / Redis / MongoDB 一起起。
- `staging` / `production`：推荐拆开部署，前端单独构建，后端单独运行，数据库改为外部服务。
- 当前后端已支持以下外部连接配置：
  - MySQL：`DB_USERNAME`、`DB_JDBC_PARAMS`、`DB_JDBC_URL_APP`、`DB_JDBC_URL_LOG`、`DB_JDBC_URL_DATA`
  - Redis：`REDIS_USERNAME`、`REDIS_SSL_ENABLED`
  - MongoDB：`MONGO_URI`
- 若继续使用仓库内 staging / production compose，它们仍会按全栈模式启动本地 MySQL / Redis / MongoDB。

## Deployment notes

- Azure App Service 适合作为演示环境后端入口，保留 `*.azurewebsites.net` 域名；前端可放在 Cloudflare Pages、Azure Static Web Apps Free 或其他静态托管；数据库建议改为外部实例。
- 本地根目录 `.env` 继续服务 `development`，建议保持 `SPRING_PROFILES_ACTIVE=development` 且默认指向 `localhost`。
- 演示 / 生产建议改用各自独立的环境变量配置，不要和本地开发共用同一份真实凭据，也不要直接把本地 `.env` 上传到部署平台。
- 当前 MySQL 仍按三套 schema（`DB_NAME` / `DB_NAME_LOG` / `DB_NAME_DATA`）组织；外部数据库也需要准备对应 schema，或者自行调整初始化策略。
