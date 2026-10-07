# 第二轮工程审计整改

本轮针对已确认的原子性、上传归属、外部副作用、读取竞态、接口契约和模块耦合问题。保留 Spring MVC、模块化单体与各客户端 Repository/ViewModel；不改页面样式，不做客户端正式渠道上传。

## 实现

- R01/R02/R13/R14/R18：严格对象删除；临时上传绑定会话所有者、有限 ticket、实际长度/MIME/内容校验及 ETag 条件复制。新增 `stored_asset` 持久化 PENDING/READY/DELETING/MISSING 状态，失败发布回滚 SQL，独立登记的清理任务继续重试。已确认的资源本地签名，历史资源首次探测并缓存；私有签名保留 S3 域名。
- R03–R05：Mongo 自定义课表按用户确定性 ID 和版本 CAS 更新，精确课程 ID 删除，五门限制，重复历史用户记录拒绝修改；Redis 短期数据一次设置值/TTL，验证码匹配后原子消费，SecureRandom 生成。
- R06–R08：Web/微信/Android/iOS 成绩课表只允许最新请求更新；分页刷新丢弃旧结果，失败保留数据；资料成功后才更新，串行处理同字段编辑。iOS 明确 yearIndex 与课程实际学年。
- R09–R11/R19：外部 HTTP 有限连接/读取超时，Provider 链有总预算与有界 executor，无效 OCR 输出继续降级；状态区分配置、可尝试与最近结果。翻译提交失败清去重标记，取消终止重试；成绩课表 Cron 按有界批次提交。校园 Apache 响应在成功、重定向和异常分支关闭。
- R12/R13：作者及聊天成员/最后消息/未读/隐私按页批量查询；保持每个查看者的权限。个人物品列表服务端 25 项分页，Web 增量读取，其余客户端适配现有完整摘要契约；编辑按 ID 读取详情；账户注销查询完整的最小 ID/状态投影。
- R15–R17/R20：view 的传输 URL 集中到模块 API；滚动加载消费明确 `{list,hasMore}`，错误形态和 suppress 行为一致；环境桶名及校园地址按配置，移除无消费者的 OCR/SMS selector。新闻改 NewsItem/NewsRepository，保留原集合与记录；PublicAuthorResolver 放业务层。MyBatis 使用现有完整类型名，删除无消费者的旧 alias 扫描，避免两种 UserLoginDTO 的短名冲突。Entity marker 仍被 ReflectionUtils 使用，保留。
- R21：增加真实隔离 MySQL/Mongo/Redis、HTTP 合成 fixture、失败/并发与前端请求生命周期测试；提高全库行门槛 40%→45%，四个关键存储/Provider 类分别检查 75% 行、50% 分支。Web 输出完整源码覆盖报告，对两个请求生命周期 composable 分别检查 75% 行、60% 分支。

## 部署及迁移

新版本需要先创建 `stored_asset` 表；init.sql 和 ArchitectureDatabaseUpgrade 使用相同结构。首次部署启用既有 `ARCHITECTURE_MIGRATION_ENABLED`，确认重复安全迁移及服务启动后关闭。不要跳过表创建，也不要删改现有业务/历史对象。

Mongo 不需要新增 customSchedule 的 createIndex 权限；grade/schedule 既有权限与索引保持。旧新闻 `_class` 可按明确目标类型读取，测试确认不改写原文档。遇到重复 customSchedule 用户记录停止其修改，先核对数据，不能自动合并/删除。

临时对象最长 PUT 15 分钟、ticket 20 分钟，清理只处理超过 20 分钟的 PENDING/DELETING，每批最多 50 项；加锁后再次核对状态/时间，避免删除被新上传续期的对象。历史 MISSING 缓存 1 小时后重查。图片限制同时包括现有字节上限和 1600 万像素；JPEG/PNG/GIF/BMP 解码验证，WebP 与音频验证文件/容器特征，未加入完整媒体转码器。

## 外部配置及边界

SMTP 当前没有账户、已验证发件人或指定收件人，只交付 [免费方案配置说明](FREE_SMTP.zh-CN.md)，模块不宣称已接通。发送账户与 EMAIL_FROM 分开，587 强制 STARTTLS，465 隐式 TLS，校验证书，连接/读取/写入默认 5 秒。

Gemini 默认改为当前可用的 gemini-3.8-flash，认证 header 携带 key，过滤 thought 部分后验证 OCR 输出。现有演示环境显式 model 需要同步；项目是否免费及真实合成调用须另行核实，不能因 key 存在声称模型可用。未配置的短信/DeepL 不自动开通；不订阅付费套餐。学校 HTTP 和 Redis 免费 Essentials 不支持 TLS 是已确认的外部限制。

全库 60%/75% 为后续分阶段目标，本轮不声称达成；覆盖率不是业务正确性的证明。真实校园登录、充值、挂失、注销、短信/邮件、用户图片及付费模型不用于日常测试。已授权 Git 发布/部署与源码、CI、运行状态分别读回。

## 本地验证（2026-10-07）

完整后端 `check architectureCoverage jacocoTestReport bootJar` 通过 677 项测试、0 失败/0 跳过；行 8,091/16,541 = 48.91%，分支 2,683/7,068 = 37.96%。随后追加历史自定义课表身份保持测试，完整 CI 按最新源码再验证。

Web 163 项单测、25 项浏览器 E2E、语法检查和构建通过；完整源码行覆盖 24.49%，两个生命周期 composable 行覆盖均 100%，分支分别 75%/98.14%。这是完整源码报告与关键模块门槛的区分，不称前端全库高覆盖。所有已修改 Vue 的 style block 与基线一致。

Android 本地单测/debug 构建及 PR 模拟器 CI 通过；微信 PR 的 226 项测试/lint/格式/34 页 smoke 通过。iOS 本机无完整 Xcode，style/localization/语法及 PR build smoke 通过，完整单元/UI 测试以实际 CI 为准。正式客户端上传暂缓。

所有数据库测试只使用本轮隔离 Docker 内的合成记录，校园网络 fixture 只连接 loopback。未调用真实校园帐号或执行真实充值、挂失、注销、短信/邮件。

PR 后续验证：删除新对象服务未使用的 bucket 参数（统一配置桶），复用 SecureRandom，并以 Math.addExact 防止个人列表游标溢出，新增边界断言。CodeQL 的 GET 写入告警经调用链核对为 lazy stored_asset 元数据缓存的误报：不会发布/删除业务对象或把待上传资源变为 READY；认证使用明确 Bearer header。只按具体告警记录 false positive，未关闭规则、认证、生命周期保护或分支保护。
修后完整本地检查 679 测试、0 失败/0 跳过；行覆盖 48.93%，分支 37.99%。Android/微信/iOS 的最终 PR CI 均通过并合并；正式渠道上传仍暂缓。
