# GdeiAssistant 数据库初始化说明

## 阶段 1：项目数据结构分析

### 使用的数据库

| 数据库 | 用途 | 说明 |
|--------|------|------|
| **MySQL** | 主业务 + 数据库 + 日志库 | 三库：`gdeiassistant`（用户/树洞/失物/表白/二手/跑腿等）、`gdeiassistant_data`（公告/电费/黄页/阅读）、`gdeiassistant_log`（充值日志/注销日志/IP 记录） |
| **MongoDB** | 试用与缓存数据 | 存成绩缓存(grade)、课表(schedule)、馆藏/借阅(collection、book)、试用数据(trial) 等，供测试账号与爬虫结果缓存 |
| **Redis** | 会话与运行时缓存 | 见 `redis/README.md` |

### 登录鉴权与密码存储

- 应用身份使用 `app_user`；校园账号和可选保存的加密凭证使用 `campus_credential`。社交 API 只返回公开 UUID。
- 校园登录走现有远程认证服务；演示库的校园密码为 `NULL`，不能把明文 demo 密码写入 AES 字段或当作已通过校园认证。
- 四端本地 mock 可演示社交功能，不依赖真实校园登录。

### 核心模块与表对应

- **用户/登录**：`app_user`、`campus_credential`、`profile`、`privacy`、`introduction`
- **失物招领**：`lostandfound`（图片存 R2，表无 picture 字段）
- **树洞**：`secret_content`、`secret_comment`、`secret_like`
- **表白墙**：`express`、`express_comment`、`express_like`、`express_guess`
- **二手交易**：`ershou`（图片存 R2）
- **跑腿/快递**：`delivery_order`、`delivery_trade`
- **卖室友**：`dating_profile`、`dating_pick`、`dating_message`
- **话题/校园拍拍**：`topic`、`topic_like`；`photograph`、`photograph_comment`、`photograph_like`
- **其他**：`feedback`、`email`、`phone`、`authentication`、`cet` 等

---

## 使用方式

1. **新的研究/演示环境**：使用 `mysql/init.sql` 创建完整结构和演示数据。该文件包含 `DROP TABLE`，只用于可重建的演示库；本轮只在新建的临时 MySQL 实例执行。
   - **保留旧演示数据**：在 MySQL 8 中执行 `mysql/upgrade-2026-10-05-social-messaging.sql`，迁移身份并新建社交表；旧校园密码置空。升级脚本可重复执行，不删除已有数据。
   - **图片私信列**：再执行 `mysql/upgrade-2026-10-06-chat-image-messaging.sql`（可重复），为已有 `chat_message` 增加 `type` 与可空图片元数据；旧行默认 `TEXT`。全新 `init.sql` / 社交升级建表已含相同列。
   - 若点赞存在重复记录，升级会报告 `REVIEW_REQUIRED` 并跳过对应唯一键；清理前先核实重复数据，不能将此状态当作完整升级成功。
   - **TiDB Cloud**：创建迁移前数据分支，并先在隔离分支验证。执行初始化或升级前设置 `SET GLOBAL tidb_enable_check_constraint=ON`，确认该值与 `@@session.foreign_key_checks` 均为 `1`；TiDB 默认关闭 CHECK 功能，不能只根据建表成功认定约束生效。重复升级通过 `information_schema.CHECK_CONSTRAINTS` 检查已存在的 CHECK，兼容 MySQL 8 与 TiDB 8.5。
   - 已在临时 MySQL 8.0.46 验证初始化、整目录有序升级、图片列重复升级、旧 TEXT 保留及最终列/索引一致；另在真实 TiDB Cloud 8.5 隔离分支验证两份社交迁移连续重复执行，以及 CHECK、外键和消息唯一键实际拒绝非法写入。
2. **MongoDB**：若使用 Mongo，执行 `mongodb/init.js`（见该目录说明）。
3. **Redis**：无需预置 Key，见 `redis/README.md`。

### 私信图片存储（启用前核对）

- 配置独立桶名 `r2.chatBucketName`（环境变量可用 `R2_CHAT_BUCKET_NAME`），必须与默认 `r2.bucketName` 不同；否则 `imageMessagingEnabled=false`，文字私信仍可用。
- 启用前须人工确认该 bucket **未启用公开域名或公开读取**；本仓库离线测试无法证明真实 private ACL。
- 不自动创建云资源；进程在上传后崩溃可能留下不可访问孤儿对象，首版不设后台清理。
