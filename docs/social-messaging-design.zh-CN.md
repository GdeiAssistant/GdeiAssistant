# 关注、粉丝、互关好友与私信设计

日期：2026-10-05。状态：本轮实现采用的共享契约。

## 产品依据与范围

用户确定：关注是单向关系，粉丝是反向视角，好友就是互相关注；不设置好友申请，不保存独立好友关系。私信取决于接收方隐私设置。系统当前用于研究和演示，现有数据为演示数据；允许直接升级架构、更新所有实际客户端，不建立没有实际使用者的旧接口兼容层。

参考资料只确认产品模式，不证明两家产品所有版本的设置完全一致：

- 微博客服《公开微博是否可以转为私密微博》（2026-03-27）明确好友圈为互粉好友：https://kefu.weibo.com/faqdetail?id=12171
- 微博客服《一键防护功能》（2026-03-11）列出按互关、关注关系限制私信的选项：https://kefu.weibo.com/faqdetail?id=21580
- 小红书公开使用教程（2023-03-25，非官方，旧版界面）展示“谁可以私信我→互相关注的人”：https://jingyan.baidu.com/article/b0b63dbf2964270b4930705b.html 。当前官方协议页面抓取超时，不能据此断言当前 App 的所有选项。

本轮完成关注、粉丝、互关好友、公开用户主页/搜索、拉黑、四档私信隐私、文字及单张图片私信、历史补拉、未读与已读、四端可访问页面和 mock。包含直接支持这些功能的身份、匿名返回、注销、数据库结构与契约调整。首版不做好友申请、群聊、语音、陌生人消息请求、第三方 IM、生产发布或真实数据操作。不顺带重构充值和其他校园查询。

## 架构与身份

保留 Spring Boot + MyBatis + MySQL 的模块化单体，增加 social/chat 模块。MySQL 是关系与消息的事实来源；Redis 用于已有会话/限流和实时事件辅助。MongoDB 维持已有教务缓存用途，不存聊天正文。

应用账号必须有稳定内部 BIGINT 主键、随机 UUID 公开 ID、ACTIVE/CLOSED 状态与时间字段。推荐 app_user 为应用账号，campus_credential 为独立校园账号/凭证（引用应用账号），不得将校园用户名或学号作为公开社交 ID。实现者可根据实际代码直接改造现有 user 表，但只能有一个权威应用账号模型，不能引入双轨账号、通用 registry 或无实际消费者的兼容层。校园凭证仍是经授权使用的可逆加密数据，不能错误替换为密码哈希。

更新实际登录、账号初始化、注销、隐私、资料和测试调用方。账号注销使社交账号 CLOSED，禁止新关注/发送，撤销会话，停止实时连接；重新注册产生新的应用账号及公开 ID，不继承旧关系或旧聊天。演示建库和升级路径必须达到同一最终结构。不执行已有数据库的 DROP、清空或任何远端迁移。

公开 DTO 只含公开 ID、昵称、头像、授权展示的简介和关系统计。不能含校园 username、密码、电话、邮箱、生日原值或内部主键。公开头像不能通过对象路径泄露校园用户名；可以通过鉴权头像端点代理读取旧私有对象，或直接调整演示对象命名，不在本轮访问真实对象存储。

匿名树洞正文/评论/互动通知不返回可关联真实用户的 username/publicId/头像 URL；不设置匿名作者主页或私信入口，不把树洞加入公开个人主页。实名社区作者可增加 authorId（公开 ID），四端将其用于个人主页入口。卖室友里的联系对象标为“发布者”，不能认为是被介绍的人。

匿名属于公开展示和响应层的处理。数据库仍保留正文作者、评论者、互动通知 actor/receiver 与内部账号的关联，用于本人管理、审核和举报处理；转换独立 VO/DTO 时隐藏身份，不改写 mapper 返回的持久化实体。匿名表白保留用户填写的公开昵称，但不返回校园用户名、隐藏竞猜真名、不提供作者主页或私信入口。演示 mock 同样只隐藏返回副本，内部记录仍保留归属与竞猜数据。

## 现有数据库评审结论

本轮需要调整的是账号身份和社交数据约束，而不是更换整套数据库：

- 原账号把校园 username 作为身份，增加跨模块社交后容易泄漏学号、混用校园凭证和应用身份。已改为 `app_user` + `campus_credential`，社交关系和消息只引用应用账号主键。
- 原点赞缺少内容/用户唯一键，重复请求可能增加重复记录和通知。已补四种点赞唯一键与幂等写入，并修正话题统计查询。
- 新关系表使用复合主键和正反向查询索引；会话使用唯一用户对；消息使用会话序号与发送方重试键双唯一约束，并增加外键及禁止自关注/自会话的检查。
- MySQL 承担有事务要求的关系与聊天；Redis 保留会话/临时状态，MongoDB 保留既有教务缓存。这种分工足够研究版本使用，当前没有引入微服务、图数据库或独立 IM 服务的依据。

仍需单独安排的已有结构整理：`profile`、`privacy`、部分旧社区表内部仍关联校园 username；若下一阶段支持多校园账号或完全独立的应用注册，应把这些引用统一迁到 `app_user.id`。已有商品/跑腿金额中的 FLOAT 也宜改为 DECIMAL，并同步 DTO 和价格计算。本轮不把这两项独立重构混进私信实现，四端公共身份入口已经使用 UUID。

研究环境推荐直接使用新 `init.sql` 建库；保留演示数据时才使用升级脚本。已在独立、可删除的 MySQL 8.0 演示实例验证初始化、按顺序升级、重复升级和结构一致性；未操作现有数据库。图片追加轮另验证了旧 TEXT 内容保留及 IMAGE 元数据读写。

## 关系和权限

user_follow(follower_id, followee_id, created_at)，唯一键(follower_id,followee_id)，双方外键关联应用账号；禁止自己关注自己。粉丝反向查询同表，好友查双向记录。返回关注变更后的实际状态，不用 toggle 接口。重复 PUT/DELETE 幂等，不重复增加计数或通知。

user_block(blocker_id,blocked_id,created_at) 保存拉黑。任一方向拉黑均禁止新关注和私信。拉黑事务内移除双方关注；解除拉黑不恢复之前关注。历史会话仍只向既有成员开放，历史读取不授予新发送权限。对外不披露对方拉黑细节，仅返回 CONTACT_UNAVAILABLE。自我拉黑不合法。

私信设置 dmPolicy：ALL（所有登录用户）、FOLLOWING（接收者关注了发送者）、MUTUAL（双方互关）、NONE（不接收）。默认 MUTUAL；未知或缺失值不得变成 ALL。A 给 B 发消息，FOLLOWING 检查 B→A，不能写反。B 的 NONE 不阻止 B 主动发出，但 A 的回复仍按 B 当前策略判断。双方账户状态和拉黑优先于设置。每次新建会话/发送都查当前权限，旧会话、旧 canMessage 和缓存不得绕过最新关系或设置；关系/设置写入与发送要有明确并发排序。

公开关系列表仅对登录用户可见；本轮不新增公开互联网索引。互关好友和所有其他关系列表通过同一授权查询。当前用户列表和公开主页排除已注销、不可联系对象。昵称搜索不以校园账号查找用户。

## 会话与消息

conversation(id,user_low_id,user_high_id,last_seq,last_message_at,created_at)，同一对用户唯一，low<high。
conversation_member(conversation_id,user_id,last_read_seq,created_at)，复合主键，普通私信恰好两个参与者。
chat_message(id,conversation_id,seq,sender_id,client_message_id,content,created_at)，唯一(conversation_id,seq)，唯一(conversation_id,sender_id,client_message_id)。正文纯文本，trim 后1–1000个 Unicode 字符；不解释 HTML。发送者来自服务端登录态，不能接受客户端指定 senderId。服务端时间 ISO8601，明确时区；BIGINT ID/seq API 序列化为十进制字符串。

所有消息查询、已读、详情、实时事件只对会话成员开放。锁定会话后事务内递增 seq、插入消息、更新摘要，提交后确认成功、触发实时事件。同一个 clientMessageId 重试相同正文返回已提交的同一消息；不同正文返回409 CLIENT_MESSAGE_CONFLICT。已提交消息的重试仍需登录/成员资格，不重复发送。最新隐私变化不能把已成功提交的原消息错误描述为未发送。

lastReadSeq 仅前进，不能超过已提交的 last_seq。未读统计 lastReadSeq 之后由对方发送的消息，不直接用序号相减。普通发送、已读更新、关注/拉黑/隐私并发均用数据库事务/既有锁能力处理，不新建通用锁框架。

消息页按会话 seq 游标；beforeSeq 和 afterSeq 互斥。无游标返回最近一页，items始终按seq升序。beforeSeq返回更早一页（供prepend），afterSeq返回更晚一页（供append）；按方向返回nextCursor和hasMore。limit默认20、最大50。关系列表/会话列表使用稳定游标，包含并列时的唯一键，不使用仅秒级时间或不断变化的offset。

## REST 契约（四端必须一致）

前缀 /api/social。均需现有 Bearer JWT；复用现有 JsonResult/DataJsonResult 包装：成功 code=200,success=true,message="success",data；失败 success=false，HTTP状态与业务code一致，另含errorCode供客户端判断。不要只HTTP200携带失败。分页data统一为 {items,nextCursor,hasMore}。

SocialUser：{id:string,nickname:string,avatarUrl:string|null,introduction:string|null,followingCount:int,followerCount:int,friendCount:int,relationship:"SELF"|"NONE"|"FOLLOWING"|"FOLLOWED_BY"|"MUTUAL",blockedByMe:boolean,canMessage:boolean,messagePermissionReason:string|null}。SELF没有发送/关注按钮。唯一公开身份字段为id（随机UUID）。不要从nickname或展示标签猜身份。

ChatMessage：{id:string,conversationId:string,seq:string,senderId:string,clientMessageId:string,type:"TEXT"|"IMAGE",content:string,createdAt:string,image:{url:string,width:int,height:int,size:int,contentType:"image/jpeg"|"image/png"}|null}。已有文字消息没有 type 时客户端按 TEXT 读取；IMAGE 的 content 为 ""，会话摘要显示本地化的图片提示。
Conversation：{id:string,peer:SocialUser,lastMessage:ChatMessage|null,updatedAt:string,unreadCount:int,lastReadSeq:string,canSend:boolean,sendPermissionReason:string|null,imageMessagingEnabled:boolean}。imageMessagingEnabled 表示服务器图片存储可用，与 canSend 的关系/隐私判断分开；未提供时默认 false，不假造真实服务能力。

| 方法 | 路径（含 /api/social 前缀） | 请求和data |
|---|---|---|
| GET | /me | SocialUser |
| GET | /users?query=&cursor=&limit= | Page<SocialUser>；query昵称或公开ID，不搜校园账号 |
| GET | /users/{id} | SocialUser |
| GET | /users/{id}/avatar | 鉴权后image响应，不暴露校园对象路径 |
| GET | /users/{id}/relationships?kind=following\|followers\|friends&cursor=&limit= | Page<SocialUser> |
| PUT/DELETE | /users/{id}/follow | SocialUser（操作后状态） |
| PUT/DELETE | /users/{id}/block | {blocked:boolean} |
| GET | /blocks?cursor=&limit= | Page<SocialUser>（自己的黑名单） |
| GET/PUT | /privacy | GET或PUT body {dmPolicy:"ALL"\|"FOLLOWING"\|"MUTUAL"\|"NONE"}；data相同 |
| GET | /unread | {total:int}（仅私信未读） |
| POST | /conversations | body {peerId:string}；data Conversation，既有会话返回同一ID |
| GET | /conversations?cursor=&limit= | Page<Conversation> |
| GET | /conversations/{id} | Conversation |
| GET | /conversations/{id}/messages?beforeSeq=&afterSeq=&limit= | Page<ChatMessage> |
| POST | /conversations/{id}/messages | body {clientMessageId:UUID,content:string}；data ChatMessage |
| PUT | /conversations/{id}/read | body {lastReadSeq:string}；data {lastReadSeq:string,unreadCount:int} |

稳定errorCode：AUTH_REQUIRED(401)、USER_NOT_FOUND(404)、CONVERSATION_NOT_FOUND(404，对非成员不暴露存在性)、CONTACT_UNAVAILABLE(403)、PRIVACY_RESTRICTED(403)、INVALID_REQUEST(400)、CLIENT_MESSAGE_CONFLICT(409)、RATE_LIMITED(429)。前端不显示敏感底层异常。

## 实时与客户端状态

使用简单WebSocket JSON，不引入STOMP/SockJS/外部IM。端点 /api/social/realtime；WSS连接第一帧 {type:"auth",token:当前JWT}，服务端认证后返回 {type:"ready"}；认证前不推送任何业务事件，5秒无认证关闭。验证签名、过期、现有会话撤销和应用账号状态；不能只解码JWT、不能信任客户端userId。token/正文不进日志。客户端只在认证后接受事件，logout/换账号时关闭并清空。

客户端允许发 {type:"ping"}，收到 {type:"pong"}。服务端事件：
- {type:"message.created",conversationId:string,messageId:string,seq:string}：重新取详情/afterSeq消息和未读。
- {type:"conversation.read",conversationId:string,readerId:string,lastReadSeq:string}：同步已读。
- {type:"social.changed"}：重新取自己摘要/当前主页/权限；关注、拉黑和隐私修改通知有关账号所有连接。

服务端推送仅为加速；客户端ready/回到前台/断线恢复时通过REST同步。单实例先用本机连接管理；多实例可复用Redis pub/sub传递无正文的事件并按用户筛选，MySQL仍可补拉。禁止把Pub/Sub到达当消息持久化成功。

每个客户端一个统一实时连接管理器，不能每个聊天页建立永久连接。前台活动聊天可每10秒补拉，消息列表每30秒更新；离开/后台停止轮询。重连退避并清理旧连接；token过期按既有登录失效路径处理。发送气泡 pending/sent/failed，重试沿用clientMessageId。返回与推送按消息id去重。读到消息后才更新已读，多设备游标只前进。隐私拒绝时保留草稿、刷新权限；“已发送”只代表已入库，不冒充对方已读。初版不承诺离线发送。

## 页面与接入

四端均新增：可搜索的用户入口、公开主页、关注/粉丝/好友列表、私信会话列表和文字聊天、私信接收范围、黑名单管理。现有个人中心显示统计及关系列表入口；消息入口区分私信、互动、系统。公开非匿名社区作者可进入主页；匿名树洞不允许此入口。可用的mock/remote一致，不让Release落到mock。六种已有语言新增文案，不使用硬编码英文失败或把未知数据造为真实时间/状态。

各端可沿用现有网络/仓储/ViewModel或Vue composable结构。本轮不为新功能迁移全部UI组件，也不新增全局状态框架。

## 验证与协作

本轮只有本地源码/演示fixture和安全测试授权；不commit、push、PR、部署、真实登录、充值、数据清空、升级IDE或扩大凭据权限。四个仓库分别一个writer，GdeiAssistant包含后端和Web，禁止另开同仓并行writer。共享本文件在派发前已确定；实现如发现必须改契约先报告主助手，不自行让各端采用不同接口。任务内禁止嵌套代理。

后端验证包括权限四档及方向、重复关注、取消互关、双方拉黑、同对会话唯一、并发发送seq、重试不重复、不同正文冲突、非成员读取/已读/推送拒绝、已读单调/上限、注销重注册、匿名DTO/通知不含身份、分页并列与深翻页、消息纯文本和Unicode长度、会话摘要和未读的提交一致性。使用离线合成数据。能运行MySQL8时跑数据库约束/并发集成；H2只能作为其支持范围的辅助，不能冒充MySQL验证。

Web做build、相关Vitest、mock Playwright路径；小程序lint、tests、smoke。Android运行现有JVM测试/lint/build（需SDK）；iOS运行真实可用的Xcode构建/测试（需完整Xcode）。当前主机缺Android SDK与完整Xcode时，保留明确阻塞，可做合法的源码/语法检查，不称设备验证完成。

数据库及前端通用测试仅在本轮改动相关时扩展。交付区分代码实现、实际执行的测试、设备/数据库未验证及未发布状态。


## 图片私信追加契约（2026-10-05）

- POST `/api/social/conversations/{id}/messages/image`，multipart 字段 `clientMessageId`（UUID）、`image`（单张文件），data 返回统一 ChatMessage。图片和消息一起确认，没有独立暂存上传接口。沿用成员校验、最新隐私/拉黑、账号升序锁、消息 seq 和提交后无正文通知。
- GET `/api/social/conversations/{id}/messages/{messageId}/image`，Bearer 登录、当前账号 ACTIVE、会话成员及消息属于该会话；只读历史不受后来的拉黑/接收隐私限制。返回图片字节，`Cache-Control: private, no-store`、`X-Content-Type-Options: nosniff`，不返回对象 key、bucket、公开域名、预签名 URL 或 token query。
- 原图至多 5 MiB，仅 JPEG/PNG。服务器实际解码识别格式，先检查宽高（单边至多 4096、至多 1600 万像素）再解码，按 JPEG EXIF Orientation 1–8 旋正像素后重新编码去除 EXIF/附加元数据；规范化结果也限制 5 MiB。拒绝 SVG、GIF、WebP、HEIC 和伪造 MIME。iOS/Android 可沿用现有图片处理转换为 JPEG；选图必须由用户触发，不请求整个相册读取权限。
- 图片以随机 key 保存在独立 `r2.chatBucketName` 私有 bucket，不能与通用默认 bucket 重名。不自动创建云资源、配置凭据或修改公开存储。缺配置时 imageMessagingEnabled=false，文字仍可用。启用前实际确认 bucket 无公开域名/公开读取；本次只实现和离线验证，不操作真实存储。
- chat_message 增加 type（旧数据默认 TEXT）和可空图片元数据：image_key、image_content_type、image_width、image_height、image_size、image_sha256。不会把二进制图片存入聊天正文。保留原两个唯一键；初始化与可重复升级得到一致结构。
- 同一个 sender/clientMessageId 的图片重试比较规范化图片 SHA-256；类型变化或不同图片返回 409 CLIENT_MESSAGE_CONFLICT，相同图片返回原消息。已经提交的原图在隐私收紧后仍可确认，不再次上传。客户端保留失败图片的本地数据和原 ID，先用服务端结果合并，已确认 sent 不因超时改 failed。
- 上传与消息事务失败时只清理本次生成的随机对象，不扫描或删除其他对象；删除失败需留下准确诊断，不能假称已清理。进程在上传后崩溃可能留下不可访问孤儿对象，此首版限制在启用前文档中说明；不新增后台清理框架。
- 四端补选图、发送前预览/取消、图片气泡、点击查看、上传/失败状态与同 ID 重试；用现有网络/鉴权图片能力，仅精确匹配本域上述私信图片路径才加 Bearer，外部地址不得携带登录凭据，私信图片不得作为裸公开 img URL 使用。退出登录/换账号清理图片缓存、blob 和未发送文件；页退场旧异步结果失效。
- demo 图片属于合成/现有公开演示素材，不访问真实相册、存储或后端；mock 保持相同类型、隐私和重试语义。手机键盘、安全区、图片预览需设备环境才能最终确认。
