# GdeiAssistant 数据库初始化说明

## 数据存储结构

### 使用的数据库

| 数据库 | 用途 | 说明 |
| --- | --- | --- |
| **MySQL** | 主业务 + 数据 + 日志库 | 三套 schema：`gdeiassistant`（用户/社交/树洞/失物/表白/二手/跑腿等）、`gdeiassistant_data`（公告/电费/黄页/阅读）、`gdeiassistant_log`（充值日志/注销日志/IP 记录） |
| **MongoDB** | 试用与缓存数据 | 存储成绩缓存（`grade`）、课表（`schedule`）、馆藏/借阅（`collection`、`book`）、试用数据（`trial`）等 |
| **Redis** | 会话与运行时缓存 | 见 [`redis/README.md`](redis/README.md) |

### 登录鉴权与密码存储

- 应用身份使用 `app_user`；校园账号与可逆加密凭证使用 `campus_credential`，社交 API 仅返回公开 UUID。
- 校园认证走远程教务认证服务；`init.sql` 演示校园密码为 `NULL`，不得将明文测试密码写入 AES 密文字段或当作已通过校园认证。
- 四端本地 mock 可独立演示社交与业务流程，不依赖真实校园登录。

### 核心模块与表对应

- **用户/登录/社交**：`app_user`、`campus_credential`、`profile`、`privacy`、`introduction`、`user_follow`、`user_block`、`conversation`、`conversation_member`、`chat_message`
- **失物招领**：`lostandfound`（图片存对象存储，表无 `picture` 字段）
- **树洞**：`secret_content`、`secret_comment`、`secret_like`
- **表白墙**：`express`、`express_comment`、`express_like`、`express_guess`
- **二手交易**：`ershou`（图片存对象存储）
- **跑腿/快递**：`delivery_order`、`delivery_trade`
- **卖室友**：`dating_profile`、`dating_pick`、`dating_message`
- **话题/校园拍拍**：`topic`、`topic_like`；`photograph`、`photograph_comment`、`photograph_like`
- **其他**：`feedback`、`email`、`phone`、`authentication`、`cet`、`announcement_read`、`charge_order`、`campus_credential_consent` 等

---

## 使用方式

1. **全新初始化（演示 / 本地开发环境）**：执行 `mysql/init.sql` 创建完整结构与示例数据（含 `DROP TABLE`，仅用于可重建环境）。
2. **存量库顺序升级（幂等脚本）**：保留既有数据时，按日期文件名依次执行 `mysql/` 下的增量脚本：
   1. `upgrade-2026-04-28-charge-order-status.sql`（`gdeiassistant_log`：充值订单状态表）
   2. `upgrade-2026-05-11-campus-credential-controls.sql`（`gdeiassistant`：校园凭证授权与快捷认证控制）
   3. `upgrade-2026-06-28-idempotency-unique-constraint.sql`（`gdeiassistant_log`：幂等键唯一约束，依赖 `04-28` 脚本）
   4. `upgrade-2026-10-05-social-messaging.sql`（`gdeiassistant`：迁移 `app_user`/`campus_credential` 并创建社交与私信表）
   5. `upgrade-2026-10-06-chat-image-messaging.sql`（`gdeiassistant`：为 `chat_message` 增加图片私信列）
   6. `upgrade-2026-10-08-announcement-read.sql`（`gdeiassistant_data`：公告已读记录表）
3. **注意事项**：
   - 若点赞表存在历史重复记录，社交升级脚本会输出 `REVIEW_REQUIRED` 并跳过对应唯一键，需先核查清理重复数据再补加唯一约束。
   - **TiDB Cloud**：迁移前先创建隔离数据分支，确认开启 `SET GLOBAL tidb_enable_check_constraint=ON` 且与 `@@session.foreign_key_checks` 均为 `1`（TiDB 默认关闭 CHECK 约束）。
4. **MongoDB**：执行 `mongodb/init.js` 初始化缓存集合与示例文档。
5. **Redis**：无需预置 Key，说明见 [`redis/README.md`](redis/README.md)。

### 私信图片存储（启用前核对）

- 配置独立桶名 `r2.chatBucketName`（环境变量 `R2_CHAT_BUCKET_NAME`），必须与默认 `r2.bucketName` 不同，否则 `imageMessagingEnabled=false`。
- 启用前须人工核实该私有桶未绑定公开域名且禁用公开读取；进程上传后崩溃可能留下孤儿对象，建议配置对象存储生命周期策略。
