# 社交关系与私信架构契约

本文定义 GdeiAssistant 服务端与 Web、Android、iOS、微信小程序四端共享的社交关系、即时私信及图片私信接口契约。

## 关系模型与边界

- **单向关注与互关好友**：关注为单向关系，粉丝为反向查询视角，互相关注即为好友；系统不设独立好友申请表或额外好友关系表。
- **拉黑优先级**：任意方向拉黑均禁止新关注与新私信发送，拉黑操作在同一事务内清除双方关注；解除拉黑不自动恢复历史关注。历史会话对既有成员保持只读可见，不授予新的发送权限。
- **私信接收策略（`dmPolicy`）**：支持 `ALL`（所有登录用户）、`FOLLOWING`（接收者已关注发送者）、`MUTUAL`（双方互关）与 `NONE`（拒收新发起消息）。系统默认策略为 `MUTUAL`，未知或缺失策略按 `MUTUAL` 处理，绝不回退为 `ALL`。
- **主动发出规则**：`NONE` 策略不阻止用户主动向外发起或发送消息，但对端回复时仍严格按接收方当前策略校验；双方账户状态与拉黑状态优先于 `dmPolicy`。

## 身份模型与匿名边界

- **身份分离**：权威应用账号为 `app_user`（内部 `BIGINT` 主键 `id`、随机 UUID 公开标识 `public_id`、`ACTIVE`/`CLOSED` 状态及时间字段）；校园账号与可逆加密凭证独立存储在 `campus_credential` 中，关联 `app_user.id`。
- **零标识泄露**：公开社交 DTO 只包含 `public_id`、昵称、头像、授权展示简介与关系统计，不得暴露校园 `username`、密码、手机号、邮箱、生日原值或数据库自增主键。
- **头像代理**：`GET /api/social/users/{id}/avatar` 通过鉴权代理读取对象，不直接向客户端暴露包含校园账号特征的存储对象路径。
- **匿名社区隔离**：匿名树洞与匿名表白墙在响应 DTO 与互动通知中剥离 `username`、`publicId` 及头像 URL，不提供作者公开主页或私信入口，也不计入公开个人主页；持久化层仍保留关联以支持本人管理、审核与举报处置。
- **卖室友展示规范**：公开条目携带发布者 `authorId` 时仅标识信息发布者，不得混淆为被介绍人身份。
- **账号注销**：注销事务按序锁定 `app_user` 行、删除关注与拉黑关系、将账号置为 `CLOSED`、撤销 Redis 会话并断开 WebSocket。已关闭账号不可新关注或发消息，原会话保留只读历史且对端显示为已注销状态。

## 并发控制与存储约束

- **事务隔离与加锁**：关系建立、关注、拉黑、隐私变更及发送消息采用 `READ_COMMITTED` 隔离级别，按固定升序锁定双方 `app_user ... FOR UPDATE`，并在加锁后重新校验账户状态、拉黑与 `dmPolicy`。
- **幂等发送**：`chat_message` 通过 `(conversation_id, seq)` 与 `(conversation_id, sender_id, client_message_id)` 双唯一键保证顺序与幂等。同一 `clientMessageId` 重试相同内容时优先返回已提交消息；相同 ID 携带不同内容或不同消息类型时返回 `409 CLIENT_MESSAGE_CONFLICT`。
- **已读游标**：`conversation_member.last_read_seq` 仅允许单调递增且不超过已提交 `last_seq`，未读数统计 `last_read_seq` 之后由对端发送的消息数。
- **点赞唯一性**：`topic_like`、`express_like`、`photograph_like`、`secret_like` 均建有复合唯一约束，防止重复请求产生重复点赞或重复通知。
- **数据库职责分工**：MySQL 承担关系与消息事实源及事务一致性；Redis 承担会话、限流与多实例事件分发辅助；MongoDB 维持教务结果缓存，不存储聊天正文。

## REST 契约（`/api/social`）

所有接口均需有效 `Authorization: Bearer <jwt>`，统一复用 `JsonResult` / `DataJsonResult` 响应信封，分页 `data` 结构为 `{ items, nextCursor, hasMore }`。所有 `BIGINT` 主键与 `seq` 在 JSON 中以十进制字符串传输。

### 核心数据结构

- **SocialUser**：`{ id, nickname, avatarUrl, introduction, followingCount, followerCount, friendCount, relationship ("SELF"|"NONE"|"FOLLOWING"|"FOLLOWED_BY"|"MUTUAL"), blockedByMe, canMessage, messagePermissionReason }`
- **ChatMessage**：`{ id, conversationId, seq, senderId, clientMessageId, type ("TEXT"|"IMAGE"), content, createdAt, image: { url, width, height, size, contentType ("image/jpeg"|"image/png") } | null }`
- **Conversation**：`{ id, peer: SocialUser, lastMessage: ChatMessage | null, updatedAt, unreadCount, lastReadSeq, canSend, sendPermissionReason, imageMessagingEnabled }`

### 端点列表

| 方法 | 路径（`/api/social` 前缀） | 请求体 / 查询参数与返回 `data` |
| --- | --- | --- |
| `GET` | `/me` | 返回当前用户的 `SocialUser` |
| `GET` | `/users?query=&cursor=&limit=` | 按昵称或公开 UUID 搜索，`Page<SocialUser>` |
| `GET` | `/users/{id}` | 返回目标 `SocialUser` |
| `GET` | `/users/{id}/avatar` | 鉴权返回头像字节流 |
| `GET` | `/users/{id}/relationships?kind=following\|followers\|friends&cursor=&limit=` | 返回关系用户列表 `Page<SocialUser>` |
| `PUT` / `DELETE` | `/users/{id}/follow` | 幂等关注 / 取关，返回操作后 `SocialUser` |
| `PUT` / `DELETE` | `/users/{id}/block` | 幂等拉黑 / 取消拉黑，返回 `{ blocked }` |
| `GET` | `/blocks?cursor=&limit=` | 返回当前用户的黑名单 `Page<SocialUser>` |
| `GET` / `PUT` | `/privacy` | 读取或更新 `{ dmPolicy }` |
| `GET` | `/unread` | 返回私信总未读数 `{ total }` |
| `POST` | `/conversations` | 请求体 `{ peerId }`，创建或复用并返回 `Conversation` |
| `GET` | `/conversations?cursor=&limit=` | 会话列表 `Page<Conversation>` |
| `GET` | `/conversations/{id}` | 会话详情 `Conversation` |
| `GET` | `/conversations/{id}/messages?beforeSeq=&afterSeq=&limit=` | 按 `seq` 游标正/反向分页返回升序 `Page<ChatMessage>`（`limit` 默认 20，最大 50） |
| `POST` | `/conversations/{id}/messages` | 请求体 `{ clientMessageId, content }`，文本 1–1000 Unicode 字符，返回 `ChatMessage` |
| `POST` | `/conversations/{id}/messages/image` | multipart `{ clientMessageId, image }`，同事务上传私有图并落库，返回 `ChatMessage` |
| `GET` | `/conversations/{id}/messages/{messageId}/image` | 鉴权读取私信原图字节流 |
| `PUT` | `/conversations/{id}/read` | 请求体 `{ lastReadSeq }`，返回 `{ lastReadSeq, unreadCount }` |

### 错误码

| HTTP 状态 | `errorCode` | 含义 |
| --- | --- | --- |
| `400` | `INVALID_REQUEST` | 参数校验失败、互斥游标冲突或消息长度/图片格式不合法 |
| `401` | `AUTH_REQUIRED` | 缺失或失效的 Bearer JWT |
| `403` | `CONTACT_UNAVAILABLE` | 任一方拉黑、目标账号已关闭或不可联系 |
| `403` | `PRIVACY_RESTRICTED` | 目标用户当前 `dmPolicy` 不允许发起或回复消息 |
| `404` | `USER_NOT_FOUND` | 目标公开 UUID 不存在 |
| `404` | `CONVERSATION_NOT_FOUND` | 会话不存在或当前用户非会话成员（不暴露存在性） |
| `409` | `CLIENT_MESSAGE_CONFLICT` | 同一 `clientMessageId` 提交了不同内容或不同类型的消息 |
| `429` | `RATE_LIMITED` | 触发限流阈值 |

## 实时通道与客户端状态

- **WebSocket 握手与认证**：端点 `/api/social/realtime` 使用原生 JSON 消息（不引入 STOMP/SockJS）。连接建立后首帧须发送 `{ type: "auth", token }`，服务端校验签名、过期、Redis 会话撤销与账号 `ACTIVE` 状态后回发 `{ type: "ready" }`；5 秒内未认证则关闭连接。
- **心跳与事件类型**：客户端支持 `{ type: "ping" }` / `{ type: "pong" }`；服务端事件包括 `message.created`、`conversation.read` 与 `social.changed`（仅含事件元数据与 ID/seq，不含消息正文或敏感内容）。
- **一致性策略**：服务端仅在事务 `afterCommit` 阶段广播事件；客户端每个应用实例维护单一连接管理器，以服务端推送加速刷新，并在 `ready`、前台恢复及断线重连后通过 REST `afterSeq` 补拉对账。
- **客户端状态合并**：发送中与失败消息保存在本地待重发队列中，沿用原 `clientMessageId` 重试；收到 WS/REST 确认后按 `(serverId, clientMessageId)` 双索引合并并去重，已确认状态不被晚到超时降级。历史翻页 `prepend` 时保持原有首条消息锚点。

## 图片私信与存储隔离

- **上传限制与规范化**：仅支持真实魔数匹配的 JPEG 与 PNG 图片，原始大小及规范化结果均不得超过 `5 MiB`；服务端在解码前先校验宽高（单边 `<= 4096`，总像素 `<= 1600万`），依据 JPEG EXIF Orientation（1–8）旋正像素并重新编码，彻底剥离 EXIF 与附加元数据。
- **私有对象存储**：私信图片使用随机对象键存入独立私有桶 `r2.chatBucketName`（通过 `R2_CHAT_BUCKET_NAME` 配置，禁止与默认桶 `r2.bucketName` 同名）。桶未配置或配置冲突时 `imageMessagingEnabled` 为 `false`，文字私信保持可用。启用前运维须确认该桶未开启公开域名或公开读取策略。
- **鉴权代理下载**：`GET .../messages/{messageId}/image` 仅对登录且 `ACTIVE` 的会话成员开放（历史消息不受后续拉黑或隐私收紧影响），返回 `Cache-Control: private, no-store` 与 `X-Content-Type-Options: nosniff`，不返回对象键、桶名、公开 URL 或预签名链接。
- **异常回滚与客户端安全**：数据库事务回滚时清理本次上传的随机对象（进程崩溃导致上传后未落库可能产生孤儿对象，可通过私有桶生命周期规则定期清理）；存储不可用时映射为 `SOCIAL_IMAGE_UNAVAILABLE`。客户端仅允许对同源精确私信图片路径携带 Bearer 请求头，并在退出登录、切换账号或离开会话页面时清理临时文件与内存图片缓存。
