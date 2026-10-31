## 这个 PR 做了什么

<!-- 一句话说清改了什么 -->

## 不改会怎样

<!-- 说明不合并这个改动会出现什么问题，而不只是改了什么 -->

## 怎么验证的

```bash
./gradlew test

cd frontend
npm ci && npm run build && npm run test:unit
```

跑不了的项请说明原因，不要勾上了事。

## 检查清单

- [ ] 上面列出的检查已通过，或有说明为什么没跑
- [ ] 改动涉及的行为变化，已在同一次 PR 里更新对应文档
- [ ] 没有提交真实账号、密码、验证码、Token、Cookie 或他人个人信息
- [ ] 没有提交密钥、证书、`local.properties` 或环境配置文件

## 涉及隐私、权限或数据流的改动

<!--
如果这个 PR 涉及登录、校园凭证、第三方服务、文件上传、日志内容或数据导出，
请说明：数据从哪来、存到哪、谁能读到、日志里会不会出现。
不涉及可以写「不涉及」。
-->

## 关联 Issue

<!-- Closes #123 -->

## AI assistance

Which AI tool and model were used? Write `None` if no AI was used.
