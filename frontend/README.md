# GdeiAssistant Frontend

GdeiAssistant Web 前端基于 Vue 3.5 + Vite 8 + Vue Router 5 + Vue I18n 11 + Tailwind CSS 4 + Reka UI 2 构建，采用《岭南书院人文风》设计规范，负责 Web 端界面、路由、Mock/Remote 数据源切换、主题与 6 种语言展示。

## 环境要求

- Node.js `24.14.1`（见 `.nvmrc`）
- npm `11+`

## 安装与启动

```bash
npm ci
npm run dev
```

默认开发地址为 `http://localhost:5173`，Vite 通过 `vite.config.js` 将 `/api`（含 WebSocket）代理到 `DEV_BACKEND_PROXY_TARGET`（默认 `http://localhost:8080`）。

## 常用命令

```bash
npm run dev                  # 启动 Vite 开发服务器
npm run build                # 构建生产产物
npm run preview              # 本地预览生产构建产物
npm run test:unit            # 运行 Vitest 单元测试
npm run test:coverage        # 运行单元测试并校验覆盖率阈值
npm run test:e2e             # 运行 Playwright 端到端冒烟测试
npm run check:design-tokens  # 校验 design/tokens.json 生成产物同步
npm run check:location-names # 校验 6 种语言地点名称一致性
npm run check:syntax         # 检查 JS 语法
```

本地运行 Playwright 时如需复用本机 Chrome，可设置 `PLAYWRIGHT_CHROME_EXECUTABLE_PATH` 环境变量指向 Chrome 可执行文件。

## 主题与国际化

- **双模式画布**：支持浅色「岭南晨纸」（`#F7F6F2`）、深色「珠江夜读」（`#0D1311`）及跟随系统。
- **4 套校园主题色**：`banyan`（榕荫翠）、`kapok`（木棉丹）、`haizhu`（海珠蓝）、`bauhinia`（紫荆紫），由 `src/theme.js` 与 `src/style.css` 管理并在 `index.html` 内联脚本中防闪烁恢复。
- **字号与多语言**：支持 4 档字体缩放与 6 种语言实时切换（`zh-CN`、`zh-HK`、`zh-TW`、`en`、`ja`、`ko`），词条位于 `src/locales/`。

## 环境变量与数据源

- `VITE_APP_BASE_API`：后端 API 基地址，默认 `/api`
- `VITE_PE_EXTERNAL_URL`：体测等外链页面地址，可留空
- `localStorage.gdei_data_source_mode`：切换 `remote`（真实后端）与 `mock`（本地演示数据）

## 目录结构

- `src/api`：后端接口封装
- `src/services`：业务服务与数据源适配
- `src/views`：功能页面视图
- `src/components`：通用 UI 与社区组件
- `src/layout`：应用壳层、侧边导航、底栏与按需加载的命令面板
- `src/locales`：6 种语言国际化词条
- `src/styles`：包含自动生成的 `design-tokens.generated.css`
- `tests` / `test`：Vitest 单元测试与测试夹具
- `e2e`：Playwright 端到端测试用例

## CI 基线

`frontend-ci.yml` 默认依次执行：`npm ci`、`npm run build`、`npm run check:design-tokens`、`npm run check:location-names`、`npm run check:syntax`、`npm run test:coverage` 以及 `npm run test:e2e`。
