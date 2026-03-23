# GdeiAssistant Release Verification Checklist

**Date:** 2026-03-23
**Scope:** Full-stack release gate covering backend, iOS, Android, WeChat Mini Program, and Web.

---

## 1. Backend Tests

```bash
cd /Users/lisiyi/Projects/GdeiAssistant
./gradlew test
```

- [ ] All tests pass (expect 151+ tests)
- [ ] No new test failures compared to previous release
- [ ] Security/auth regression tests green (session, JWT validation, anonymization)
- [ ] Performance regression tests green (batch query assertions, no N+1 regressions)

## 2. iOS Tests

```bash
cd /Users/lisiyi/Projects/GdeiAssistant-iOS
xcodebuild test -scheme GdeiAssistant-iOS -destination 'platform=iOS Simulator,name=iPhone 17 Pro'
```

- [ ] All tests pass (expect 23+ tests)
- [ ] Bootstrap/assembly tests green (Core, Campus, Community, Profile assemblies)
- [ ] No duplicate-fetch regression tests failing
- [ ] Mapper and repository-style tests green

## 3. Android Unit Tests

```bash
cd /Users/lisiyi/Projects/GdeiAssistant-Android
./gradlew testDebugUnitTest
```

- [ ] All unit tests pass
- [ ] DisplayMapper tests green (all 7 mappers: Grade, Schedule, Card, CET, Book, Exam, Classroom)
- [ ] Repository tests confirm raw domain data output (no UI string references)

## 4. WeChat Mini Program Tests

```bash
cd /Users/lisiyi/Projects/GdeiAssistant-WechatApp
node --test tests/*.test.js
```

- [ ] All tests pass (expect 76+ tests)
- [ ] Registry handler tests green (all 8 module handlers)
- [ ] Request-count / lazy-load tests green (inbox, photograph detail)
- [ ] i18n page-logic tests green

## 5. Web Build and Audit

```bash
cd /Users/lisiyi/Projects/GdeiAssistant-Vue
npm run build
npm audit
```

- [ ] Build completes without errors
- [ ] `npm audit` reports no critical or high vulnerabilities
- [ ] No new warnings introduced since last release

## 6. Environment Variable Validation

The following environment variables **must** be set in production. The application is designed to fail fast if they are missing or empty.

| Variable | Purpose | Fail-Fast Behavior |
|---|---|---|
| `JWT_SECRET` | Signs and verifies JWT tokens for API authentication | Application refuses to issue or validate tokens; startup fails |
| `CRON_SECRET` | Authenticates scheduled-task HTTP triggers | Cron endpoints reject all requests; must not be empty string |
| `ENCRYPT_ENABLE` | Master toggle for field-level encryption | When `true`, encryption subsystem activates |
| `ENCRYPT_PRIVATE_KEY` | RSA private key for field-level decryption | Encryption subsystem fails fast on missing key when encryption is enabled |

Verification steps:

- [ ] Confirm `JWT_SECRET` is set and non-empty in the deployment environment
- [ ] Confirm `CRON_SECRET` is set and non-empty in the deployment environment
- [ ] Confirm `ENCRYPT_ENABLE` is set to the intended value (`true` or `false`)
- [ ] If `ENCRYPT_ENABLE=true`, confirm `ENCRYPT_PRIVATE_KEY` is set and contains a valid RSA private key

## 7. Production Startup Checks

After deployment, verify the following fail-fast behaviors are working:

- [ ] **Encryption fail-fast:** If `ENCRYPT_ENABLE=true` and `ENCRYPT_PRIVATE_KEY` is missing or malformed, the application must fail to start (not silently degrade)
- [ ] **JWT fail-fast:** If `JWT_SECRET` is missing or empty, JWT token operations must fail immediately rather than falling back to a default or empty key
- [ ] **Cron secret not empty:** If `CRON_SECRET` is empty or unset, cron-triggered endpoints must reject requests with an authentication error, not accept them silently

## 8. Final Sign-Off

- [ ] All test suites above are green
- [ ] All required environment variables confirmed in production config
- [ ] Startup fail-fast behaviors verified in staging
- [ ] No critical or high `npm audit` findings
- [ ] Release notes updated with changes since last release
