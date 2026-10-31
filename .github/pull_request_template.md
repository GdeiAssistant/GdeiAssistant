## What changed

<!-- Describe the change and resulting behavior. -->

## Why

<!-- Explain the problem this change resolves. -->

## Verification

```bash
./gradlew test

cd frontend
npm ci && npm run build && npm run test:unit
```

Record the commands actually run and their results. Explain any checks that could not run.

## Checklist

- [ ] The checks above passed, or the reason they could not run is recorded.
- [ ] Documentation is updated for behavior changed by this PR.
- [ ] The change contains no credentials, verification codes, session data or personal information.
- [ ] The change contains no private keys, certificates, `local.properties` or private environment files.

## Privacy, permissions and data flow

<!--
For changes to login, campus credentials, third-party services, uploads, logging or exports,
describe the data source, storage, access and log exposure. Write `Not applicable` otherwise.
-->

## Related issue

<!-- Closes #123 -->

## AI assistance

Which AI tool and model were used? Write `None` if no AI was used.

## Risk

<!-- Record the risk tier, affected paths and relevant compatibility or operational impact. -->
