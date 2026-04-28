# Release Readiness Checklist

## 1. Scope

This checklist is used for manual pre-release confirmation for GdeiAssistant. It covers:

- GdeiAssistant/GdeiAssistant
- GdeiAssistant/GdeiAssistant-WechatApp
- GdeiAssistant/GdeiAssistant-Android
- GdeiAssistant/GdeiAssistant-iOS

This document is an internal release checklist. It is not a user agreement, privacy policy, legal-subject statement, filing statement, contact-information statement, or public policy page.

## 2. Current Repository Status

| Repository | Branch | Status | Release blockers |
|---|---|---|---|
| GdeiAssistant/GdeiAssistant | master | clean and synced | none known |
| GdeiAssistant/GdeiAssistant-WechatApp | master | clean and synced | none known |
| GdeiAssistant/GdeiAssistant-Android | master | clean and synced | none known |
| GdeiAssistant/GdeiAssistant-iOS | main | clean and synced | none known |

Latest read-only review conclusion:

- All four working trees were clean.
- Local branches were synced with their upstream branches.
- No local build artifacts, env files, signing files, keystores, provisioning profiles, or local configuration files were found in Git status.
- No known release-blocking issue remained at the time of review.

## 3. Completed Release-Blocking Work

### GdeiAssistant/GdeiAssistant

- PR #161: compliance notices and sensitive data redaction
- PR #162: campus credential consent persistence and controls
- PR #163: public policy copy cleanup
- PR #164: session log redaction and public demo credential cleanup
- PR #165: remove client-side HMAC trust from charge requests
- PR #166: retire legacy charge HMAC configuration
- PR #167: Redis Idempotency-Key support for charge requests
- PR #168: charge_order status tracking
- PR #169: charge order query APIs

### GdeiAssistant/GdeiAssistant-WechatApp

- PR #37: campus credential controls and sensitive data masking
- PR #38: public copy cleanup

### GdeiAssistant/GdeiAssistant-Android

- PR #40: public copy cleanup
- PR #41: campus credential controls
- PR #42: remove client-side charge HMAC secret
- PR #43: send Idempotency-Key for charge requests
- PR #44: charge order status UI
- PR #45: stale payment order and raw error message cleanup

### GdeiAssistant/GdeiAssistant-iOS

- PR #37: public copy cleanup
- PR #38: campus credential controls
- PR #39: remove client-side charge HMAC secret
- PR #40: send Idempotency-Key for charge requests
- PR #41: charge order status UI

## 4. Completed Capability Matrix

| Capability | Backend/Web | WeChatApp | Android | iOS | Status |
|---|---|---|---|---|---|
| Campus credential standalone consent | done | done | done | done | complete |
| Campus credential consent persistence | done | integrated | integrated | integrated | complete |
| Revoke campus credential consent | done | done | done | done | complete |
| Delete saved campus credentials | done | done | done | done | complete |
| Quick-auth control | done | done | done | done | complete |
| Sensitive data masking/redaction | done | done | done | done | complete |
| Public copy cleanup | done | done | done | done | complete |
| Client-side charge HMAC secret removal | backend no longer trusts HMAC | N/A | done | done | complete |
| Redis Idempotency-Key | done | N/A | sends key | sends key | complete |
| charge_order status tracking | done | N/A | consumes status | consumes status | complete |
| Charge order query APIs | done | N/A | integrated | integrated | complete |
| Charge order status UI | API support | N/A | done | done | complete |
| PAYMENT_SESSION_CREATED wording | explicit non-settlement wording | N/A | done | done | complete |

Notes:

- Web and WeChatApp currently have no charge entry point, so charge-client rows are marked N/A where applicable.
- PAYMENT_SESSION_CREATED means that the payment request or payment session was generated. It does not mean final balance settlement.

## 5. Release-Blocking Checks Resolved

- [x] No client-side charge HMAC shared secret remains in Android or iOS.
- [x] Backend no longer trusts hmac/timestamp as a security boundary.
- [x] REQUEST_VALIDATE_TOKEN is no longer an active deployment configuration.
- [x] README no longer exposes public test account/password pairs.
- [x] Session/token/password/hmac logging risks have been addressed.
- [x] Campus credential consent, revocation, deletion, and quick-auth controls are implemented across active clients.
- [x] Public user-facing pages do not contain Codex/ChatGPT/TODO/internal drafting notes.
- [x] Android stale payment order UI issue has been fixed.
- [x] Android raw exception messages are no longer displayed directly in charge UI.
- [x] iOS payment page uses the current session order and avoids stale order display.
- [x] Charge order APIs return safe fields only.
- [x] PAYMENT_SESSION_CREATED is not described as final balance settlement.

## 6. Pre-Release Manual Confirmation Checklist

- [ ] Production backend is deployed with master including PR #169 and later fixes.
- [ ] Database upgrade SQL has been executed, including campus credential controls and charge_order changes.
- [ ] Redis is available in production and TTL/capacity are suitable for charge idempotency.
- [ ] Android and iOS client versions are compatible with the deployed backend APIs.
- [ ] Old Android/iOS client compatibility strategy is confirmed.
- [ ] Android/iOS/WeChatApp release packages do not include mock environment, debug configuration, local test credentials, or signing secrets.
- [ ] Payment/charge flow has been verified in a controlled test environment.
- [ ] PAYMENT_SESSION_CREATED wording has been checked in released clients and does not imply final settlement.
- [ ] Online user agreement, privacy policy, third-party service list, and related About/Policy pages are reachable.
- [ ] ICP/security filing numbers match the actual production domain.
- [ ] JWT_SECRET, encryption keys, database passwords, object storage keys, SMTP credentials, SMS credentials, and other secrets are managed through deployment secret management.
- [ ] The actual third-party service inventory has been confirmed for the deployment environment.
- [ ] SMTP, SMS, object storage, AI/OCR, logging/monitoring, cloud hosting, database, Redis/cache, and backup services have been confirmed.
- [ ] Whether any service involves cross-border processing or overseas providers has been confirmed.
- [ ] Incident/reporting channels for security, privacy requests, and user complaints are operational.

Do not fill this checklist with real secrets, real accounts, or real service credentials.

## 7. Deployment-Specific Third-Party Service Confirmation

| Service category | Enabled? | Provider / region | Data involved | Cross-border? | Notes |
|---|---|---|---|---|---|
| School systems |  |  | campus account, query data |  |  |
| SMTP / email |  |  | email address, message metadata |  |  |
| SMS |  |  | phone number, verification records |  |  |
| Object storage |  |  | avatar/images/files |  |  |
| AI/OCR |  |  | captcha/image/request metadata |  |  |
| Cloud hosting |  |  | logs, runtime metadata |  |  |
| MySQL/MongoDB |  |  | user and business data |  |  |
| Redis/cache |  |  | sessions, idempotency records |  |  |
| Logging/monitoring |  |  | logs and diagnostics |  |  |
| Backup |  |  | backup data |  |  |

Reminders:

- Do not publish secrets in this table.
- Keep public privacy notices aligned with the actual deployment.

## 8. Post-Release Enhancement Backlog

The following items are not blockers for this release:

- [ ] UNKNOWN automatic reconciliation for charge orders.
- [ ] MANUAL_REVIEW tools for unresolved charge orders.
- [ ] Confirmation of school-side order number, transaction number, or settlement status.
- [ ] Broader personal information export/deletion workflow.
- [ ] Long-term maintenance of third-party service inventory.
- [ ] Remove legacy hmac/timestamp fields after compatibility window.
- [ ] Require Idempotency-Key after compatible clients are widely deployed.
- [ ] Evaluate server-side payment URL / cookie relay to reduce client exposure.
- [ ] Continue synthetic mock/demo data cleanup where needed.
- [ ] Continue reviewing new features for campus credential, privacy, logging, and sensitive-data consistency.

## 9. Release Recommendation

Current recommendation:

- No known release-blocking code changes remain based on the latest read-only review.
- Proceed to manual deployment and operational verification.
- Do not continue adding code changes before release unless a new blocker is discovered.
- Track post-release enhancement items separately.
