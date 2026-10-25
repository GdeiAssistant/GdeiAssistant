# AGENTS.md

## 项目是什么

广东第二师范学院校园助手系统的服务端与网页端，配套 Android、iOS 和微信小程序客户端。

## 技术栈

Java（Gradle，`build.gradle`）+ Node.js 前端（`frontend/package.json`，Vite + Vitest + Playwright）。

## 目录

- `src/` — Java 服务端。
- `frontend/` — 网页端。`npm run test:unit` 用 Vitest，`test:e2e` 用 Playwright。
- `db-init/` — 数据库初始化脚本。
- `design/` — 设计令牌来源，`npm run check:design-tokens` 校验产物是否同步。
- `docs/` — `architecture` 类文档和 `operations/` 运维说明。

## 本地验证

按改动选择相关离线检查，命令以仓库当前 CI 为准。先区分代码问题与环境条件，不为通过检查降低保护。

**后端检查**

```bash
./gradlew check architectureCoverage jacocoTestReport
```

**前端依赖**

```bash
npm ci --prefix frontend
```

**前端构建**

```bash
npm run build --prefix frontend
```

**前端单元测试**

```bash
npm run test:unit --prefix frontend
```

**设计令牌一致性**

```bash
npm run check:design-tokens --prefix frontend
```

**地点名称一致性**

```bash
npm run check:location-names --prefix frontend
```

## 命名与文档

- 新增文档文件名小写、`-` 连接，中文版后缀 `.zh-CN.md`；`README.md`、`AGENTS.md` 等工具约定文件保留固定名称。源码沿用现有语言命名。
- 不提交审计报告、复盘、交接单、进度快照、升级计划。要追溯看 commit 历史。
- 改文件名时同一次提交改完所有引用。
- 提交前跑 `python scripts/check_hygiene.py`。

## 不要做

- 不要手改 `design/` 生成出来的令牌产物。改源文件后重新生成，否则 `check:design-tokens` 会红。
- 不要绕过 `check:location-names`。地点名称有统一来源。
- 不要在四个客户端之间单方面改接口。服务端、Android、iOS、微信小程序共享同一套契约，改一处要确认另外三处。

## 提交与 PR

Conventional Commits，标题中文，不超过 50 字。类型只用 `feat` `fix` `docs` `refactor` `test` `chore` `perf` `ci`。

分支名 `<type>/<主题>`，例如 `docs/normalize-filenames`。不直接推默认分支，不 force push 已 review 的分支。

提交、推送、合并及生产动作按用户已明确授权的范围执行；同范围已有授权不重复确认。

PR 描述写改了什么、为什么改、怎么验证。
