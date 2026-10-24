# Changelog

本文件记录 GdeiAssistant 服务端的架构变更和重要行为调整。格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

客户端的变更记录在各自仓库。

## [Unreleased]

### 变更

- 演示环境架构升级。

  - 请求字段、公共身份和金额契约统一。跑腿与二手市场的公共数据不再返回校园账号。
  - 后端分层调整为 Controller / Service / Repository；客户端继续沿用现有 MVVM、Repository 与 Mapper，不引入新的架构框架。
  - 职责过大的账号模块和社交模块按功能拆分。
  - 外部教务系统经核实只支持 HTTP，保留上游协议；自有站点和 API 使用 HTTPS，上游入口可通过 `campus.upstream.*` 配置。
  - 校园凭据只在服务端消费。客户端的借阅和注销凭据放在 JSON 请求体里，不拼进 URL。
  - 注销在应用事务内关闭账号并登记持久清理任务；Redis、Mongo、对象存储和日志清理在事务提交后重试。应用账号状态会立即阻断旧会话。

### 迁移说明

- 已有数据库需设置 `ARCHITECTURE_MIGRATION_ENABLED=true`，然后执行保留记录的重复安全升级。初始化文件只用于新库，不能覆盖已有库。
- 升级会先拒绝重复键、孤立关系和越界金额。
- Mongo 的成绩与课表缓存会同步迁移类名元数据，并建立 `username` 唯一索引。
- 接口文档更新：`./gradlew generateOpenApi`。
- 校验命令：`./gradlew test checkOpenApi architectureCoverage jacocoTestReport`。

### 已知缺口

- CI 对本次升级涉及的关键模块执行 40% 行覆盖率门槛。
- 全库覆盖率约 29%，原 `./gradlew check` 的全库 40% 目标保留但尚未达到。
- 定向验证通过不等于全库覆盖率达标，发布说明中不应混为一谈。
