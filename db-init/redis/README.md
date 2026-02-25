# Redis 在本项目中的用途说明

本项目 **不需要** 在初始化时预置任何 Key，所有 Redis 数据均由应用在运行时写入。以下为用途说明与可选运维示例。

## 用途概览

| 用途 | 说明 | 写入时机 |
|------|------|----------|
| **登录/会话** | 用户登录凭证、Session、Cookie 等（如 `UserCertificateDaoImpl` 的 LOGIN_PREFIX、SESSION_PREFIX、COOKIE_PREFIX） | 用户登录后 |
| **CookieStore / LoginToken** | 与登录态相关的 Token 存储 | 登录、刷新 |
| **VerificationCode** | 验证码（如短信/邮箱验证码） | 发送验证码时 |
| **Request 限流** | 接口限流、防刷（按 IP 或用户） | 请求到达时 |
| **ExportData** | 导出任务结果缓存 | 用户触发导出时 |
| **AccessToken** | 第三方或内部 API 的 AccessToken 缓存 | 授权/刷新时 |
| **其他业务缓存** | 如课表、成绩等可被缓存的数据（部分逻辑在 MongoDB） | 按业务配置 |

## 是否需要初始化脚本

- **不需要**。Redis 仅作为运行时缓存与会话存储，登录、限流、验证码等 Key 均由应用按需创建并设置 TTL。
- 若希望清空所有与本项目相关的 Key 做“干净启动”，可在确认无其他服务共用同一 Redis 实例的前提下执行（**慎用**）：

```bash
# 仅当 Redis 专用于本应用且确认无其他服务时使用
redis-cli FLUSHDB
```

- 若项目使用了 key 前缀（如 `gdeiassistant:`），可按前缀删除（需与代码中的前缀一致）：

```bash
# 示例：删除以 gdeiassistant: 开头的 key（前缀以实际配置为准）
redis-cli --scan --pattern 'gdeiassistant:*' | xargs -r redis-cli DEL
```

## 连接与配置

应用通过 `application-*.yml` 中的 `spring.redis.*` 配置连接 Redis。确保 Redis 已启动且地址/端口/密码与配置一致即可，无需预先执行任何脚本。
