# Audited shared design tokens

`tokens.json` transcribes page 6 of the approved **前端 UI 重构方案.pdf**:
eight light/dark colors, the six reference font sizes (30/24/17/15/13/12),
14-unit card radius and 16-unit page spacing. No other design roles are added.

Run `node design/generate-tokens.mjs` from any working directory to regenerate:

- `frontend/src/styles/design-tokens.generated.css` (px)
- `design/generated/GeneratedDesignTokens.kt` (Compose sp/dp)
- `design/generated/GeneratedDesignTokens.swift` (SwiftUI reference CGFloat sizes)
- `design/generated/design-tokens.wxss` (2 rpx per reference unit)

Run `node design/generate-tokens.mjs --check` to fail on missing or stale outputs.
The frontend CI checks these files without rewriting them.

The Web stylesheet imports the generated palette and card radius and maps the
six Tailwind text utilities to the generated font variables. Existing
hover colors, semantic roles and component styles retain their current values.
The Web typography work is tracked separately in PR #287; this change provides
the six reference sizes without duplicating its component changes. Its
`text-headline`, `text-body` and `text-callout` consumers resolve through this
source after the changes are combined.

The other three outputs are generated integration artifacts, not an assertion
that the separate Android, iOS and WeChat repositories already consume them.
Native integrations must retain font scaling, existing semantic color roles and
platform accessibility. In particular, SwiftUI's reference sizes must be mapped
through the existing Dynamic Type styles rather than setting every view to an
unscaled fixed font. Navigation, homepage ordering and service icons are separate
approved targets and are not changed by this token-source commit.
