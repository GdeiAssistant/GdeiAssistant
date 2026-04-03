# GdeiAssistant Frontend

GdeiAssistant Web 前端，基于 Vue 3 + Vite + Vue Router 构建，负责校园助手 Web 端界面、路由、Mock/Remote 数据源切换与多语言展示。

## 环境要求

- Node.js `24.14.1`
- npm `11+`

仓库内已提供 `.nvmrc`，建议直接使用：

```bash
nvm use
```

## 安装与启动

```bash
npm ci
npm run dev
```

默认开发地址：`http://localhost:5173`

Vite 会将 `/api` 请求代理到本地后端 `http://localhost:8080`，见 `vite.config.js`。

## 常用命令

```bash
npm run dev         # 本地开发
npm run build       # 生产构建
npm run test:unit   # Vitest 单元测试
npm run test:e2e    # Playwright 端到端测试
```

## 环境变量

- `VITE_APP_BASE_API`：后端 API 基地址，默认 `/api`
- `VITE_PE_EXTERNAL_URL`：体测等外链页面地址，可留空

生产构建时通常通过 `frontend/Dockerfile` 或根目录编排文件注入这些变量。

## 数据源模式

前端支持两种数据源：

- `remote`：请求真实后端接口
- `mock`：走本地 Mock 数据，便于 UI 和交互验证

当前模式会保存在浏览器 `localStorage` 的 `gdei_data_source_mode` 中。

## 目录约定

- `src/api`：接口封装
- `src/services`：业务服务与数据源适配
- `src/views`：页面视图
- `src/components`：通用组件
- `src/router`：路由配置
- `test`：Vitest 单元测试
- `e2e`：Playwright 用例

## CI 基线

`frontend-ci.yml` 默认执行以下检查：

1. `npm ci`
2. `npm run build`
3. `npm run test:unit`

如需扩展 Playwright smoke，请在现有 `e2e/` 用例基础上继续补充。
