# 资料页三端 i18n 协议升级设计

## 背景

当前 iOS、Android、微信端都依赖以下接口读取个人资料相关数据：

- `GET /api/user/profile`
- `GET /api/profile/options`
- `GET /api/profile/locations`

现在这组接口的主要问题是：

- `user/profile` 返回的是最终展示字符串，例如 `faculty`、`major`、`location`、`hometown`
- 三端编辑和展示依赖这些字符串，无法稳定按 code 回填
- mock 和真实请求语义不一致，容易出现一边能本地化、一边不能本地化的情况
- 后端就算按 `Accept-Language` 返回翻译结果，接口本身仍然是“展示字符串协议”，后面继续扩展会很别扭

这次直接升级协议，不保留兼容字段。

## 目标

把资料页相关接口统一升级成“结构化数据 + 当前语言显示名”的新协议，支持以下语言：

- `zh-CN`
- `zh-HK`
- `zh-TW`
- `en`
- `ja`
- `ko`

升级后要求：

- 后端根据 `Accept-Language` 返回当前语言的 `label`、`name`、`displayName`
- 三端都基于结构化字段消费，不再依赖旧的最终字符串字段
- mock 和真实请求使用同一套协议
- 保存接口继续使用 code 或结构化 location 字段，不再依赖中文名匹配

## 非目标

这次不处理以下内容：

- 非资料页模块的业务数据国际化
- 前端页面文案本身的通用 i18n 机制
- 旧接口兼容层
- 院系、专业、地区以外的全部业务字典重构

## 设计原则

1. 后端负责数据语义和当前语言显示名生成
2. 三端负责展示、编辑态回填和少量异常处理
3. code 是业务主键，label 只是当前语言显示值
4. 同一份业务数据在三端必须有一致的结构定义
5. mock 必须模拟真实接口，不再单独维护旧字符串模型

## 新协议设计

### 1. `GET /api/profile/options`

返回院系和专业选项，专业从字符串升级成结构化对象。

```json
{
  "facultyOptions": [
    {
      "code": 11,
      "label": "School of Computer Science",
      "majors": [
        {
          "code": "software_engineering",
          "label": "Software Engineering"
        }
      ]
    }
  ]
}
```

字段说明：

- `facultyOptions[].code`: 院系唯一标识，沿用现有院系 code
- `facultyOptions[].label`: 当前语言显示名
- `facultyOptions[].majors[].code`: 专业唯一标识，必须稳定，不允许继续使用中文专业名充当标识
- `facultyOptions[].majors[].label`: 当前语言显示名

### 2. `GET /api/profile/locations`

返回结构化地区树，保留 code，同时返回当前语言 `name`。

```json
{
  "regions": [
    {
      "code": "CN",
      "name": "China",
      "states": [
        {
          "code": "GD",
          "name": "Guangdong",
          "cities": [
            {
              "code": "GZ",
              "name": "Guangzhou"
            }
          ]
        }
      ]
    }
  ]
}
```

字段说明：

- `region/state/city code` 是稳定标识
- `name` 是根据请求语言生成的显示名

### 3. `GET /api/user/profile`

院系、专业、所在地、家乡改成对象，不再返回旧字符串字段。

```json
{
  "name": "test",
  "gender": 1,
  "faculty": {
    "code": 11,
    "label": "School of Computer Science"
  },
  "major": {
    "code": "software_engineering",
    "label": "Software Engineering"
  },
  "location": {
    "region": "CN",
    "state": "GD",
    "city": "GZ",
    "displayName": "China Guangdong Guangzhou"
  },
  "hometown": {
    "region": "CN",
    "state": "GD",
    "city": "ST",
    "displayName": "China Guangdong Shantou"
  }
}
```

字段说明：

- `faculty.code` / `major.code` 用于编辑态回填和保存
- `faculty.label` / `major.label` 用于直接展示
- `location` / `hometown` 的 `region/state/city` 用于编辑态回填和保存
- `displayName` 用于详情展示

## 后端设计

### 数据来源

- 院系：继续使用现有院系 code
- 专业：必须补齐稳定 code
- 地区：继续使用现有 region/state/city code

如果专业当前只保存中文名，需要在后端建立专业 code 映射来源。优先顺序：

1. 直接在业务配置或数据库中补专业 code
2. 如果短期不能改库，先在后端配置层维护专业 code 映射

无论使用哪种方式，最终都必须保证：

- 同一个专业在所有语言下共用同一个 code
- 保存和读取都以 code 为准，不再依赖当前语言文案匹配

### 服务层拆分

新增一层资料国际化展示服务，例如：

- `ProfileLocalizationService`

职责：

- 根据请求语言生成院系 `label`
- 根据请求语言生成专业 `label`
- 根据请求语言生成地区 `name`
- 根据请求语言拼装 `location/hometown.displayName`
- 为 `profile/options` 和 `user/profile` 共用这套逻辑

这样可以避免：

- 选项接口和资料接口各自维护一套翻译逻辑
- controller 和 mapper 里分散出现显示名拼接代码

### Controller / Mapper 改动

#### `GET /api/profile/options`

- `ProfileOptionsFacade` 继续作为入口
- 输出对象从“院系 + 专业字符串列表”改成“院系对象 + 专业对象列表”
- label 由 `ProfileLocalizationService` 生成

#### `GET /api/profile/locations`

- 地区树结构保持不变
- `name` 改成当前语言值
- code 原样返回

#### `GET /api/user/profile`

- `ProfileResponseMapper` 不再输出旧字符串字段
- 直接输出：
  - `FacultyValueObject`
  - `MajorValueObject`
  - `LocationValueObject`
- `displayName` 不再由零散 formatter 直接拼中文，而是统一走 `ProfileLocalizationService`

### 语言策略

- 后端只接受并识别这 6 个 locale：`zh-CN`、`zh-HK`、`zh-TW`、`en`、`ja`、`ko`
- 不支持的语言统一回退到 `zh-CN`
- `Accept-Language` 是唯一语言来源

## 三端改动设计

### iOS

- 升级 profile DTO、mapper、viewmodel
- 展示时改为读取：
  - `faculty.label`
  - `major.label`
  - `location.displayName`
  - `hometown.displayName`
- 编辑态回填使用：
  - `faculty.code`
  - `major.code`
  - `location.region/state/city`
  - `hometown.region/state/city`
- mock 仓库输出与真实接口一致的新结构

### Android

- 升级 API DTO、repository、domain model
- 去掉对旧 `faculty/location/hometown` 字符串的依赖
- 编辑态回填使用 code 和结构化 location
- `MockProfileProvider` 改成返回新协议

### 微信

- 升级 service 层 profile 数据模型
- 页面展示直接读取对象字段中的 `label` 和 `displayName`
- 编辑态回填使用 code 和结构化 location
- mock 数据改为新协议

## Mock 设计

mock 的目标不是“看起来像”，而是“和真实接口结构一致”。

要求：

- iOS、Android、微信的 mock 数据字段名与真实接口完全一致
- mock 数据也必须按当前语言返回对应的 `label/name/displayName`
- 不再保留旧的字符串型资料摘要模型

## 测试设计

### 后端

- `profile/options` contract test
  - 验证院系和专业返回结构
  - 验证不同 `Accept-Language` 下 label 变化
- `profile/locations` contract test
  - 验证地区树结构不变
  - 验证不同 `Accept-Language` 下 name 变化
- `user/profile` contract test
  - 验证新对象字段存在
  - 验证旧字符串字段被移除
  - 验证 displayName 随请求语言变化
- `ProfileLocalizationService` 单元测试
  - 验证 6 种语言映射
  - 验证非法语言回退简中
  - 验证 location displayName 拼接

### iOS

- profile DTO / mapper 测试
- 资料页 viewmodel 回填测试
- mock 与真实结构一致性测试

### Android

- repository / dto mapping 测试
- mock provider 返回结构测试

### 微信

- profile service 数据解析测试
- 关键页面展示和编辑态回填验证

## 实施顺序

1. 后端先改接口和测试，稳定新协议
2. iOS 跟进消费新协议并改 mock
3. Android 跟进消费新协议并改 mock
4. 微信跟进消费新协议并改 mock
5. 三端分别跑资料页相关验证

## 风险与处理

### 风险 1：专业没有稳定 code

影响：

- 无法稳定回填、保存、跨语言识别

处理：

- 本次升级前先补专业 code 规则
- 实现中禁止继续使用专业中文名作为业务标识

### 风险 2：三端切换时协议不一致

影响：

- 某一端先发版后可能直接解析失败

处理：

- 因为这次不保留兼容，所以需要后端改完后，三端在同一轮开发内一起切换并验证
- 本地 mock 和联调环境都使用新协议

### 风险 3：后端多语言资源不完整

影响：

- 某些院系、专业或地区名称在部分语言下缺失

处理：

- 后端测试中增加语言完整性校验
- 缺失项统一回退简中，避免接口返回空值

## 成功标准

- 三端资料页都能基于新协议正确显示院系、专业、所在地、家乡
- 三端编辑态都能稳定回填已保存数据
- mock 和真实请求结果字段一致
- `Accept-Language` 在 6 种语言下都能返回正确显示值
- 旧字符串字段从这 3 个接口中完全移除
