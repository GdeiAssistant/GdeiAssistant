# 社交与私信实现说明（GdeiAssistant）

日期：2026-10-06（图片私信追加及实际验证）。对应共享契约：`docs/SOCIAL_MESSAGING_DESIGN.zh-CN.md`（未改字段/端点；实现遵循最新「图片私信追加契约」）。

## 身份模型

- 权威应用账号：`app_user`（`id` BIGINT、`public_id` UUID、`status` ACTIVE/CLOSED）。
- 校园凭证独立：`campus_credential`（`user_id` → `app_user.id`，`campus_username` + AES 密码）。
- `init.sql` 演示校园密码为 `NULL`；校园认证仍走现有真实入口，不假冒 demo 成功。
- 升级脚本对旧 `user` 表用动态 SQL（表不存在则跳过），临时映射生成随机 UUID 并绑定 app_user/campus_credential，重复升级幂等；社交表补 FK/CHECK。
- 注销：有序锁 `app_user` 行、删除 follow/block、CLOSED、撤销 Redis、断开 WS。

## 关系与私信并发

- create/send/follow/block/privacy：`READ_COMMITTED` + 固定升序 `app_user … FOR UPDATE` + 锁后重查权限/状态。
- 已提交相同 `clientMessageId` 先返回既有消息，不因隐私收紧误报未发送。
- 成功事件经 `SocialRealtimeHub.pushToUserAfterCommit`；rollback 零成功事件。
- 已读：会话锁后取实际 `last_read_seq`，afterCommit 推送给对端与本账号其他设备。
- 关闭 peer：会话列表/详情可读历史，peer 显示「已注销」且 `canSend=false`。
- 简介隐私缺失不默认公开；头像与公开主页同访问规则。

## 图片私信

- `POST /api/social/conversations/{id}/messages/image`：multipart `clientMessageId` + `image`；与消息同事务确认。
- `GET .../messages/{messageId}/image`：成员 + 消息属会话即可读历史图；不按当前 canSend/拉黑/隐私；`Cache-Control: private, no-store`、`X-Content-Type-Options: nosniff`。
- `chat_message` 同表可空元数据：`type`（旧 TEXT）、`image_key`/`image_content_type`/`image_width`/`image_height`/`image_size`/`image_sha256`；不引入 pending 表或后台任务。
- 编解码：`ChatImageCodec` 签名识别仅 JPEG/PNG，`ImageReader` 先宽高（≤4096 / ≤16M 像素）再 decode，按 JPEG EXIF Orientation 1–8 旋正像素后重编码去元数据，SHA-256 幂等；原图与输出均 ≤5 MiB。
- 存储：`SocialChatImageService` + `R2StorageService.uploadBytesStrict` / `deleteObjectStrict` / `downloadBytesBounded`；独立 `r2.chatBucketName`，缺配置或与默认 bucket 同名则 `imageMessagingEnabled=false`。
- 上传前完成权限检查；事务内上传随机 key 并插消息；`afterCompletion` rollback 仅删本次对象；上传失败清理该 key；存储异常映射 `SOCIAL_IMAGE_UNAVAILABLE`，不泄漏 URL/底层信息。
- **启用前须人工核实 chat bucket 未启用公开域名或公开读取**；本地无法证明真实 private ACL。崩溃后孤儿对象为首版已知限制。

## 分页与契约

- 搜索/关系列表：SQL 先滤 inactive 与任一方向 block 再 `LIMIT`；黑名单单独允许自己的 block 列表。
- 无登录 401：`success/code/message/errorCode`；社交 400 JSON/参数失败同契约包装；非成员会话 404。
- Conversation DTO 含 `imageMessagingEnabled`；ChatMessage 含 `type`/`image`（代理 URL，不含 key/bucket）。

## like 唯一约束

- `topic_like` / `express_like` / `photograph_like` / `secret_like`：唯一键 + `insert ignore`，仅插入成功时发通知。
- 升级若发现重复：输出 `REVIEW_REQUIRED` 警告，不加唯一键、不删除数据。

## REST / WebSocket

- `/api/social/realtime` 白名单握手；首帧 auth；30s 有界复核 JWT 过期、Redis/账号；`@PreDestroy` 销毁 scheduler。
- OpenAPI 索引：`docs/openapi.yaml` 已补社交与图片路径。

## 公共作者

- 非匿名话题/拍照/二手/失物/卖室友：公开 `authorId`（发布者 UUID）+ Web 入口；树洞/表白无身份入口。
- 响应不泄漏校园 username；卖室友链指发布者。

## 验证（图片追加轮）

实际运行：

- `./gradlew test`：467 项测试通过；含 JPEG EXIF 八种方向、尺寸在解码前限制、图片/文字类型冲突、成员历史读取、rollback 删除与元数据 mapper 测试。
- MySQL 8.0.46：新初始化、整目录按文件名升级、图片升级重复执行、旧 TEXT 内容保留、最终列/索引一致、图片元数据读写通过。脚本 `upgrade-2026-10-06-chat-image-messaging.sql` 明确选择应用库，并排在社交建表之后。
- Web：98 项 Vitest 通过，构建通过。浏览器整套 22 项中 21 项通过、图片 viewer 测试发现提交到达会关闭已打开图片，修复后该完整图片流程定向通过；包含预览取消、发送、查看、刷新、摘要和拉黑后历史读取。真实浏览器 EXIF 照片方向测试通过。
- UI：实际浏览器检查 375px 与 1366px 的聊天、会话、主页、搜索和隐私页面，亮/暗色截图；无水平溢出或页面异常，输入/发送控件 ≥44px。320px 聊天操作也通过浏览器测试。
- 小程序：180 项 Node 测试通过（含 13 项图片专项），lint、format:check 和 34 页面 smoke 通过。实际 Node 26.3.0；package 的 24.14.1 为声明版本。Mock 用原文件 bytes 的 SHA1 比较内容，服务端用规范化图片 SHA256，两者摘要算法不混用；Mock 对仅元数据变化的文件可能更严格。
- Android：52 项纯 JVM 检查通过；使用离线缓存依赖和实际生产 helper/DTO/mock/Retrofit 契约，不能代替声明依赖版本的 Android Gradle 构建或设备验证。
- iOS：351 个 Swift 文件 parse、工程与六语言 plist/strings 检查通过；实际生产 helper 按 Swift 5 + 默认 MainActor 编译零诊断，31 项图片断言及既有消息/滚动断言通过。XCTest 仅语法检查。

使用 Build Web Apps 的构建/前端验证 skill、Build iOS Apps 的 SwiftUI UI patterns 和 simulator/debugger 能力（本机无 simctl）；Context7 已连接，用官方 AndroidX 与微信文档核对单张选图和 multipart 上传 API。Cursor Auto 四个实现会话已结束；其中后端/iOS/Android 的 Shell 被拒，主助手与限定验证任务补跑了上述可运行层。

未验证：真实 R2 private ACL/读写、校园或生产服务、完整 iOS/Android App 构建、模拟器/真机与微信开发者工具。没有发布、迁移现有数据或新增云资源。临时 MySQL 容器与独立 Colima profile 已清理，默认 profile 保持停止。


## 个人资料与六语言核查（2026-10-06）

- 关注、粉丝、好友数量移入头像与昵称资料头部；私信接收权限和黑名单统一从既有隐私设置进入。保留详情深链和返回隐私设置的入口，关系及接收方权限契约不变。
- 六语言为简体中文、香港繁体粤语、台湾繁体国语、英语、日语、韩语。Web 每种语言现有 1314 个键；补齐日、韩、港、台原各 785 个缺失键，进一步修复页面和无障碍标签的硬编码。地区码统一处理大小写、下划线、Accept-Language 首项和港澳繁体变体。
- 日期、加载提示、校内账号说明及动态弹窗按所选语言显示；页面 lang 与标题跟随切换。用户输入、姓名、昵称、标题和正文保留原文，语言切换不改写用户内容。
- 社交错误及手动校验提示按六语言本地化，保留 HTTP 状态和业务错误代码；非法参数不回显底层异常文本。台湾资源没有混入粤语助词。
- 本轮实际检查：后端完整 518 项测试零失败；Web 117 项单测及生产构建通过，完整 25 项浏览器测试通过，包含手机/桌面六语言切换、隐私保存、刷新、真实图片文件预览发送及历史读取。浏览器使用本地 mock 服务，不代表真实校园认证或云端私图端到端验收。
- Azure Free F1 仍显示免费配额超限；后端运行及真实登录联调尚未通过。没有升级收费方案。具体合并、部署与移动端结果以对应 PR/CI 和本轮发布验证记录为准。
