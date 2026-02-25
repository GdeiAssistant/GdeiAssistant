# GdeiAssistant 数据库初始化说明

## 阶段 1：项目数据结构分析

### 使用的数据库

| 数据库 | 用途 | 说明 |
|--------|------|------|
| **MySQL** | 主业务 + 数据库 + 日志库 | 三库：`gdeiassistant`（用户/树洞/失物/表白/二手/跑腿等）、`gdeiassistant_data`（公告/电费/黄页/阅读）、`gdeiassistant_log`（充值日志/注销日志/IP 记录） |
| **MongoDB** | 试用与缓存数据 | 存成绩缓存(grade)、课表(schedule)、馆藏/借阅(collection、book)、试用数据(trial) 等，供测试账号与爬虫结果缓存 |
| **Redis** | 会话与运行时缓存 | 见 `redis/README.md` |

### 登录鉴权与密码存储

- 登录校验：`UserLoginService.UserLogin` 从 MySQL `user` 表通过 `UserMapper.selectUser(username)` 取出用户，再使用 **明文比较** `password.equals(queryUser.getPassword())`。
- 密码写入：若启用 **encrypt 模块**（AES），MyBatis 的 `MybatisEncryptionTypeHandler` 会通过 `StringEncryptUtils.encryptString` 将密码加密后写入；若未启用，则写入为明文。
- **结论**：默认 dev 未启用加密时，可直接在库中写入明文密码 `gdeiassistant`；若生产启用 AES，需通过应用“修改密码”或登录一次由应用写入密文。

### 核心模块与表对应

- **用户/登录**：`user`（username, password）、`profile`、`privacy`、`introduction`
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

1. **MySQL**：执行 `mysql/init.sql`（见该文件内注释）。
2. **MongoDB**：若使用 Mongo，执行 `mongodb/init.js`（见该目录说明）。
3. **Redis**：无需预置 Key，见 `redis/README.md`。
