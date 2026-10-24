# 通用 AI 服务

业务代码调用 `AiService.generate(route, request)`。`AiRequest` 支持文本、图片、多轮消息、system 指令和输出 token 上限；OCR 使用同一服务，并在 `OcrService` 保留识别结果校验和本地 OCR 备用方案。现阶段不包含流式输出、工具调用、语音和 embedding，需要时按各自协议能力扩展。

## 结构与设计取舍

```mermaid
flowchart LR
  Business[问答 / 摘要 / OCR 等业务] --> Service[AiService 按业务选择队列]
  Service --> Policy[服务端区域策略]
  Policy --> Chain[同区域模型降级链与熔断]
  Chain --> Adapter[Responses / Chat / Messages / Gemini 适配器]
  Adapter --> Platform[直连厂商或聚合平台]
  Catalog[定时更新模型目录] --> Lifecycle[移除弃用与下线型号]
  Lifecycle --> Service
  Catalog --> Redis[现有 Redis 保存目录与下线记录]
```

协议适配器负责 HTTP 请求和响应，平台配置负责地址、凭据、实际处理区域及模型队列；直连与聚合平台使用相同接口。`ProviderChain` 继续负责失败降级、总时间预算、指标和熔断。没有引入新的 SDK、网关服务或生产依赖。

`ocr`、`chat` 等是业务路由名称，与厂商品牌和协议无关。同一厂商可以配置多个模型，同一聚合平台可以配置多个协议端点。不同业务的断路器相互隔离。OCR 的格式不合格时会继续尝试下一个模型，最后尝试本地识别；通用文本业务默认拒绝空结果，也可传入自己的结果校验函数。

## 区域与监管边界

- `AI_SERVICE_REGION=MAINLAND` 是默认值，只允许配置为中国境内处理的端点；`OVERSEAS` 用于独立的境外部署。两个区域之间不自动降级。
- 请求和模型目录刷新使用相同区域策略。区域由服务端配置决定，前端参数、用户 IP、模型名称和供应商品牌均不能改变它。
- 聚合平台按平台及其下游的实际数据处理位置分类。境外平台托管的国产模型仍按境外服务处理。
- HTTPS、禁止重定向、凭据只放请求头、日志不记录 prompt/图片/响应正文。目录分页只使用游标，忽略外部返回的任意下一页 URL。
- 区域字段是运维声明，不是网络定位或监管认证。配置前须核实实际部署、下游路由、数据保留条款、模型授权、服务地区及适用的备案和个人信息处理义务。仅加上地区字段不能证明完整合规。可在部署网络层增加地域对应的出站地址限制。

监管参考：[生成式人工智能服务管理暂行办法](https://www.cac.gov.cn/2023-07/13/c_1690898327029107.htm)。是否适用、数据出境条件和具体义务需按实际业务确认。

## 已提供的服务配置

| 默认处理区域 | 厂商或平台 | 接口方式 |
| --- | --- | --- |
| MAINLAND | 豆包、DeepSeek、通义、智谱 GLM、Moonshot/Kimi、MiniMax、百度千帆、腾讯混元 | OpenAI Chat 兼容接口 |
| MAINLAND | SiliconFlow 中国站 | OpenAI Chat，多模型 |
| OVERSEAS | OpenAI | Responses |
| OVERSEAS | Claude | Anthropic Messages |
| OVERSEAS | Gemini | 原生 generateContent |
| OVERSEAS | xAI/Grok、Mistral | OpenAI Chat 兼容接口 |
| OVERSEAS | OpenRouter | OpenAI Chat，多模型 |
| OVERSEAS | OpenCode | Responses、Messages、Chat、Gemini 分开配置，统一平台密钥 |

这些是可配置的适配入口，不表示所有账号和模型均已取得访问权限或通过真实推理验收。各平台支持的模型、视觉能力及目录接口会变化；需要使用对应账号当前可用的 API 型号。只开通订阅不一定具有通用 API 权限。

## 配置与使用

从 `.env.template` 选择服务端凭据、地址和队列。不同业务队列分别配置，例如：

```dotenv
AI_SERVICE_REGION=MAINLAND
QWEN_CHAT_MODELS=qwen-plus
QWEN_OCR_MODEL=qwen-vl-plus
AI_MODEL_CATALOG_ENABLED=true
AI_MODEL_CATALOG_REFRESH_MS=21600000
```

示例型号用于说明配置方式，使用前核对服务商当前目录。一个变量可以配置逗号分隔的多个型号，按列表顺序尝试。空队列不启用该业务，空 API key 不启用该端点。平台之间按 `priority` 从小到大降级。

```java
String answer = aiService.generate("chat", AiRequest.text("请总结这段公开资料……"));
String extracted = aiService.generate("ocr", AiRequest.image("提取文字", "image/png", base64));
```

增加 `summary` 等业务路由时，在对应端点的 `routes` 下添加模型队列，不需要新增厂商类：

```yaml
routes:
  chat: [approved-text-model]
  summary: [approved-text-model]
  ocr: [approved-vision-model, approved-backup-vision-model]
```

应用自己的 Spring 配置覆盖端点列表时，列表整体替换，不会按 ID 自动合并；需要保留所有希望启用的端点。凭据从部署平台的 Secret 或环境变量注入，不放在配置正文中。

同一个模型可以出现在多个业务队列。配置 OCR 时必须选支持图片输入的模型，不能仅根据名字或厂商判断。未来如果增加不支持图片的协议，须明确拒绝相应请求，不能悄悄丢弃图片。

## 目录自动更新和型号淘汰

启动后 30 秒开始刷新，每 6 小时一次，可用 `AI_MODEL_CATALOG_REFRESH_MS` 调整。仅请求已配置、当前区域内平台的模型目录，不发送业务输入，也不调用付费推理 API 做评测。

- 新模型进入目录候选列表，不自动获得调用权限。业务队列仍是运维明确配置的主流型号列表。
- `status` / `lifecycle` 标为 `deprecated`、`retired`、`disabled`、`unavailable`，或 `deprecated=true`、已到 `expiration_date` 的型号，会自动移出所有调用队列。
- 此前成功刷新时存在、后来完整目录中消失的型号，也会移出队列；重新出现且未标为弃用时恢复原配置中的位置。
- 首次目录不含某个配置别名，不单凭这一点认定它已下线，避免误删厂商未在目录列出的合法别名。
- 目录更新失败、分页不完整或数据格式错误时，保留上次成功结果，不把网络故障视为模型淘汰。Redis 保存成功目录及已下线记录，应用重启后恢复；Redis 出错时仅影响此缓存，目录刷新仍可继续。
- 供应商没有模型目录 API 时会报告刷新失败；人工配置的队列仍可使用，此平台无法据此自动判断下线。

按本次选择，不额外花费 API 费用评测模型，因此不自动声称某个型号能力落后或准确率不达标。现有输出校验和熔断只识别不可用、异常或业务格式不合格，不等于模型质量排名。

目录提供只读 Actuator endpoint `aimodels`，默认不在 HTTP exposure 列表中。运维若需要启用，应放在受认证的管理入口或仅内网监听；不要直接对公网开放管理接口。结果包含目录、下线集合、最后更新时间和刷新状态，不包含密钥或用户输入。Redis 恢复的结果在首次成功联网刷新前显示 `lastRefreshSucceeded=false`。

## 原配置迁移

- `OPENAI_API_KEY`、`CLAUDE_API_KEY`、`GEMINI_API_KEY`、`DEEPSEEK_API_KEY`、`DOUBAO_API_KEY` 继续有效；原 `*_OCR_MODEL` 用作 `ocr` 队列，可填写多个型号。
- 新增通用 `*_BASE_URL`，原 `*_OCR_BASE_URL` 继续作为后备读取。`CLAUDE_OCR_API_URL` 原来是完整 `/messages` URL，现在改用 `CLAUDE_BASE_URL=https://api.anthropic.com/v1`。
- 原 `AI_OCR_PROVIDER` 未实际控制旧降级链，现已从模板移除；现在由区域、业务队列和 priority 共同决定。
- DeepSeek 和 Claude 不再给 OCR 配置过时的默认型号，需明确填写当前视觉型号。现有线上覆盖值不会自动改写。
- 从旧版升级后默认 `MAINLAND`，旧配置中只有境外密钥时仅使用本地 OCR。启用境内厂商或另行批准境外部署的配置后，才会调用对应 AI 服务。

## 协议依据

- [OpenAI 图像与视觉](https://developers.openai.com/api/docs/guides/images-vision)、[Models API](https://developers.openai.com/api/reference/resources/models/methods/list)
- [Claude Models API](https://docs.anthropic.com/en/api/models-list)
- [Gemini Models API](https://ai.google.dev/api/models)
- [OpenCode Inference API](https://opencode.ai/v2/docs/console/api/inference/)、[模型与数据处理地区](https://opencode.ai/v2/docs/console/models/)
- [通义 OpenAI 兼容接口](https://help.aliyun.com/zh/model-studio/qwen-api-via-openai-chat-completions/)、[DeepSeek Vision](https://api-docs.deepseek.com/guides/vision/)
