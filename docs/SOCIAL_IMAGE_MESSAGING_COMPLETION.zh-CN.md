# 图片私信完成与验证记录

日期：2026-10-06。四端实现与共享契约见 [设计说明](SOCIAL_MESSAGING_DESIGN.zh-CN.md)，实际验证与限制见 [实现说明](SOCIAL_MESSAGING_IMPLEMENTATION.zh-CN.md#验证图片追加轮)。

- 图片 POST 使用 multipart `image` + UUID `clientMessageId`，GET 为成员鉴权的私有图片代理；历史图不受后来的拉黑/接收隐私限制。
- 单张 JPEG/PNG 至多 5 MiB，解码前检查 4096 单边/1600 万像素，按 EXIF 旋正后去元数据；同 ID/规范图片 digest 幂等，类型或内容不同返回 409。
- `r2.chatBucketName: ${R2_CHAT_BUCKET_NAME:}` 显式环境映射；独立私有桶，缺配置时关闭新图片发送，已提交图片的同 ID 确认仍有效，文字可用。
- 同步上传与消息事务确认，rollback 只清理本次随机 key；上传后崩溃的不可访问孤儿对象为首版限制。未接入真实 R2，启用前须确认无公开读取。
- 新建/升级/重复执行的 MySQL 列、索引一致；旧消息 TEXT 内容保留。图片迁移文件为 `upgrade-2026-10-06-chat-image-messaging.sql`。

后端 467 测试、MySQL 8.0.46 新建/升级/重复迁移、Web 98 单测和浏览器图片操作已通过。

移动端已在 GitHub Actions 构建和执行：Android 228 单测、lint、Debug APK 及 API 35 模拟器 4 项基础 UI 测试通过；iOS 在 Xcode 26.3 / iOS 26.2 模拟器完成构建，157 单测和 4 项基础 UI 测试通过；小程序 Node 24.14.1 下 187 项测试及 lint、format、34 页配置检查通过。以上基础 UI 测试不代表系统选图流程已验证。

本轮继续补充 Android/iOS 系统选图、预览、图片重试和离开聊天清理测试，以及小程序实际页面函数与 wx 传输/文件系统桩的组合回归。真实 R2 私有桶、Azure 后端数据库迁移与部署、微信开发者工具和移动端签名上传尚待对应环境与凭据。提交、PR 合并及部署结果分别以 GitHub 和目标服务的实际结果为准。
