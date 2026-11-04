# API 约定

本文是跨端接口的维护约定。逐接口定义以 [`openapi.yaml`](openapi.yaml) 为唯一事实源；该文件由 Controller 请求/响应类型生成，CI 通过 `checkOpenApi` 校验它是否落后于代码。

## 单一事实源

- 不要手工编辑 `docs/openapi.yaml`。
- 修改 Controller 或 DTO 后运行 `./gradlew generateOpenApi`，提交生成结果。
- 提交前运行 `./gradlew checkOpenApi`。
- 请求和响应使用有类型的 DTO，不要用 `Map<String, Object>`；否则 OpenAPI 只能生成 `additionalProperties`，四端无法据此生成模型或校验字段。

## 鉴权

- 默认所有 `/api/**` 需要 `Authorization: Bearer <jwt>`。
- 兼容请求头 `token: <jwt>`，新客户端不要使用。
- 放行路径以 `SettingConstantUtils.LOGIN_INTERCEPTOR_EXCEPTION_LIST` 为准；其中 `/api/**` 下为 `/api/auth/login`、`/api/auth/logout`、`/api/module`、`/api/social/realtime`，其余条目是旧版页面路径。
- `/cron/*` 使用 `X-Cron-Secret`，属于内部任务接口，不进入 OpenAPI。
- Web 客户端附带 `X-Client-Type: WEB` 和 `Accept-Language`；服务端回写 `X-Request-ID` 用于追踪。
- 充值等幂等写接口使用 `Idempotency-Key`（兼容 `X-Idempotency-Key`）。

## 返回信封

- 纯状态：`JsonResult`，字段为 `code`、`success`、`message`，需要时带 `errorCode`。
- 带数据：`DataJsonResult<T>`，在上述字段之外增加 `data`。
- 成功：`success=true`，`code=200`。
- 失败：`success=false`，`message` 是可展示文本，`errorCode` 是稳定的机器码。
- 头像、图片等二进制响应直接返回字节流，不使用信封。

## 错误语义

接口目前存在两套错误语义，客户端必须同时检查 HTTP 状态和 `success`：

- 新接口（包括 `/api/social/**`）：HTTP 状态码与业务错误一致，常见为 400、401、403、404、409、429、5xx，并返回 `errorCode`。
- 历史业务接口：可能返回 HTTP 200 + `success=false`，由 `GlobalRestExceptionHandler` 统一处理。
- 不要仅凭 HTTP 200 判断成功。
- 稳定错误码示例：`AUTH_REQUIRED`、`PRIVACY_RESTRICTED`、`CLIENT_MESSAGE_CONFLICT`。新增错误码写入 `ErrorConstantUtils` 或对应模块异常，并在 PR 中说明客户端处理方式。

## 分页

- `start` 是从 0 开始的偏移量，`size` 是每页数量。
- 现有路径形式 `/start/{start}/size/{size}` 保留；部分列表接口固定页大小（例如 marketplace 10、delivery 25），以 OpenAPI 描述为准。
- 新接口使用查询参数 `start`、`size`，不要新增路径形式的分页参数；同一接口只使用一种风格。
- 支持继续翻页的列表返回 `nextStart`（或游标）和 `hasMore`，不要让客户端猜测是否还有下一页。

## 命名

- 路径段使用小写 kebab-case，例如 `/api/campus-credential`、`/api/graduate-exam`。
- 资源使用复数名词；单个资源使用 `/{id}`，不要新增 `/资源/id/{id}` 形式。
- 读操作使用 GET，创建使用 POST，全量替换使用 PUT，局部更新使用 PATCH，删除使用 DELETE。
- 非 CRUD 动作可以使用子资源或动作段，例如 `/conversations/{id}/read`、`/card/lost`，但同一模块内保持一致。
- JSON 字段使用 lowerCamelCase。
- 路径变量使用 `{id}` 或 lowerCamelCase，例如 `{orderId}`；同一资源保持一致。

## 版本与兼容

- 当前不使用 `/v1` 路径版本；OpenAPI 的 `info.version` 表示契约修订日期，不是应用版本。
- 只做向后兼容的增量修改。删除或重命名路径、字段属于破坏性变更。
- 服务端、Android、iOS、微信小程序和 Web 共享同一契约。破坏性变更必须四端确认，并保留至少一个发布周期的旧路径或字段。
- 旧路径迁移按模块进行，不要一次性全量重命名。

## 国际化

- 客户端通过 `Accept-Language` 传递语言：`zh-CN`、`zh-HK`、`zh-TW`、`en`、`ja`、`ko`。
- 服务端错误 `message` 会本地化；`errorCode` 不本地化，客户端应基于它分支。
- 动态内容翻译由服务端白名单控制；法律文本始终显示中文原版。

## WebSocket

- `/api/social/realtime` 是 WebSocket 握手路径，不在 OpenAPI 中。
- 握手后首帧发送认证信息，5 秒内未认证会被关闭；详细行为见 [`social-messaging-implementation.zh-CN.md`](social-messaging-implementation.zh-CN.md)。
