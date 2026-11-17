# 演示环境免费 SMTP 配置

本文说明 GdeiAssistant 演示环境对接免费事务邮件 SMTP 服务（Brevo、SMTP2GO、Resend）的配置方式与校验要点。专用 SMTP 密钥只保存在部署平台的受保护配置中，不得提交到仓库。

## 服务商对比

- **Brevo Free**：每日 300 封（不跨日累计），免费邮件含 Brevo 标识。超额事务邮件可能排队，验证码有效期 5 分钟，不应把“SMTP 接受”等同于“及时送达”，测试前需留意当日配额。[官方免费限制](https://help.brevo.com/hc/en-us/articles/208580669-FAQs-What-are-the-limits-of-the-Free-plan)
- **SMTP2GO Free**：每月 1,000 封；注册要求工作或自有域名邮箱并经过人工审核，个人公共邮箱通常不满足注册条件。[官方定价及注册](https://www.smtp2go.com/pricing/)
- **Resend Free**：每月 3,000 封、每天 100 封，支持 SMTP；面向普通收件人发送前须完成发件域名验证，测试域名不能替代正式发件身份。[官方定价](https://resend.com/pricing)、[SMTP 配置](https://resend.com/docs/send-with-smtp)

免费额度不构成送达 SLA。使用公共邮箱作为发件人时，部分供应商可能重写 `From` 地址；若需保持一致发件身份，应配置并验证自有域名。

## Brevo 配置步骤

1. 注册 Brevo 免费账户并完成身份及邮箱验证。
2. 在后台添加并认证发件域名与事务邮件发件人，按引导配置真实 DNS / DKIM 记录。
3. 在 SMTP 设置页面生成专用 **SMTP key**（独立于网站登录密码和 HTTP API key）。
4. 在部署平台（如 Azure App Service）的受保护应用配置中填写以下环境变量，勿写入代码库、命令行历史或公开截图：

| 项目设置 | Brevo 值 / 来源 |
| --- | --- |
| `EMAIL_SMTP_HOST` | `smtp-relay.brevo.com` |
| `EMAIL_SMTP_PORT` | `587`（STARTTLS） |
| `EMAIL_SMTP_SSL` | `false`（587 端口由应用启用 STARTTLS，并非明文传输） |
| `EMAIL_SMTP_USERNAME` | SMTP 页面显示的登录名 |
| `EMAIL_SMTP_PASSWORD` | 专用 SMTP key（仅放受保护配置） |
| `EMAIL_FROM` | 已验证的发件邮箱（与 SMTP login 区分） |

也可改用 `465` 端口配合 `EMAIL_SMTP_SSL=true` 启用隐式 TLS。所有连接模式均校验证书主机名并设置有限超时，禁止关闭证书校验或回退明文传输。[Brevo 官方连接说明](https://help.brevo.com/hc/en-us/articles/7924908994450-Send-transactional-emails-using-Brevo-SMTP)

## 配置核验与行为边界

- 更新环境变量并重启服务后，可通过 `/api/module/state/detail` 检查邮箱模块配置状态（`enabled` 仅表示配置完备）。
- 在线投递核验须针对获准测试收件人检查供应商事务日志与验证码消费行为：错误验证码不消耗重试次数、正确验证码仅可单次消费、超期即失效。
- 未配置发送器或发送异常时，服务端直接返回发送失败并清理待用验证码，不伪造成功响应也不自动循环重试。
- 日志与错误响应不得暴露真实收件人地址、验证码原文或 SMTP key；密钥轮换时须在受保护配置更新后及时撤销旧 key。
