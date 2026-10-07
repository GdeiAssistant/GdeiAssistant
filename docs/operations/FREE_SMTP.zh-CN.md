# 演示环境免费 SMTP 配置

核对日期：2026-10-07。当前没有 SMTP 账户或已验证发件人，因此本次只完成代码及配置说明；邮箱模块保持未启用，没有发送真实邮件。

## 选择

优先考虑 **Brevo Free**：每日 300 封，不累积到次日，免费邮件含 Brevo 标识。超额事务邮件可能排队，验证码有 5 分钟有效期，因此不能把“SMTP 接受”当作“及时送达”，演示前检查当日余量。[官方免费限制](https://help.brevo.com/hc/en-us/articles/208580669-FAQs-What-are-the-limits-of-the-Free-plan)

可选 **SMTP2GO Free**：每月 1,000 封；注册页要求工作/自有域名邮箱，且有验证和新账户审核，个人公共邮箱不能直接满足注册条件。[官方定价及注册](https://www.smtp2go.com/pricing/)

有可验证域名时，也可考虑 **Resend Free**：每月 3,000 封、每天 100 封，支持 SMTP。发给普通收件人之前需要完成域名验证，测试域名不能替代正式发件身份。[官方定价](https://resend.com/pricing)、[SMTP 配置](https://resend.com/docs/send-with-smtp)

以上免费额度不等于已开通；不要为演示自动订阅付费套餐。用户目前没有账户，下一次接通需要选定服务、完成账户及发件人验证，并指定一个测试收件人。

## Brevo 操作

1. 在 Brevo 注册 Free 账户，完成网站要求的身份及邮箱验证。
2. 按网站步骤添加并认证发件域名，创建事务邮件发件人。DNS 中只填网站实际提供的 DKIM 等记录，不抄写其他账户的值。
3. 打开 SMTP 页面，生成专用 **SMTP key**。它不是网站登录密码，也不是 HTTP API key。
4. 将下面设置通过现有 Azure App Service 的受保护配置页面填写；不要写入仓库、聊天、命令参数或截图。使用网站显示的真实 SMTP login 和已验证发件人。

| 项目设置 | Brevo 值/来源 |
|---|---|
| `EMAIL_SMTP_HOST` | `smtp-relay.brevo.com` |
| `EMAIL_SMTP_PORT` | `587`（STARTTLS） |
| `EMAIL_SMTP_SSL` | `false`（587 强制 STARTTLS；不是关闭加密） |
| `EMAIL_SMTP_USERNAME` | SMTP 页面显示的登录名，可能是生成的地址 |
| `EMAIL_SMTP_PASSWORD` | 专用 SMTP key，仅放受保护配置 |
| `EMAIL_FROM` | 已验证的发件邮箱，与 SMTP login 分开 |

也可用 `465` + `EMAIL_SMTP_SSL=true`，应用会使用隐式 TLS。所有模式校验证书主机名，连接、读取、写入均有有限超时。不要改为不校验证书或明文 SMTP。[Brevo 官方连接说明](https://help.brevo.com/hc/en-us/articles/7924908994450-Send-transactional-emails-using-Brevo-SMTP)

## 接通后的验收

保存配置并按既有流程重启/部署，检查 `/api/module/state/detail` 的邮箱配置状态；配置存在只能说明具备连接条件。然后仅向指定测试邮箱发送一次验证码，核对 Brevo 事务日志、实际收件及验证码校验：错误码不消耗，正确码只能消费一次，超过有效期拒绝。

发送失败或额度耗尽时保留模块错误，不伪造成功，不循环向收件人重试。真实验证码和地址不要写入公开日志。后续需要轮换 SMTP key 时，更新受保护配置并撤销旧 key。
