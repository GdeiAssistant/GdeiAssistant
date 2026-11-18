# Shared Design Tokens (`Warm Editorial Lingnan Academy`)

`tokens.json` (version 2) defines the shared **Lingnan Editorial** visual baseline across Web, Android, iOS, and WeChat Mini Program:

- `colors`: 25 light/dark roles: surfaces (`bg`, `surface`, `surface-sunken`, `surface-elevated`, `border`, `divider`), text (`text-1..3`), primary (`primary`, `primary-hover`, `primary-soft`, `primary-ink`, `on-primary`, `band`, `on-band`) and status (`danger`, `warning`, `success`, each with `-ink` and `-soft`). Fills and stripes use the plain role; any text uses the `-ink` role.
- `accents`: per-accent overrides (`kapok`, `haizhu`, `bauhinia`) of the six accent-dependent primary roles. `banyan` is the base palette.
- `course`: eight timetable/category pairs. `course-N-bg` and `course-N-ink` are the fill and its text; `course-N-stripe` is the 4px stripe (hue in light, ink in dark).
- 6 reference font sizes (`28/22/17/15/14/12`), `10`-unit card radius, and `16`-unit page spacing.

Usage rule: dark `text-3` must not be set on dark `primary-soft` (4.11:1); use `text-2` there (4.73:1).

Run `node design/generate-tokens.mjs` from any working directory to regenerate:

- `frontend/src/styles/design-tokens.generated.css` (`px` CSS custom properties)
- `design/generated/GeneratedDesignTokens.kt` (Compose `sp` / `dp` object)
- `design/generated/GeneratedDesignTokens.swift` (SwiftUI `CGFloat` / RGB constants)
- `design/generated/design-tokens.wxss` (`2 rpx` per reference unit for WeChat Mini Program)

Run `node design/generate-tokens.mjs --check` to verify parity without rewriting files. Frontend CI enforces this check on every pull request.

The generated `light` palette models warm paper (`#F7F6F2` canvas, `#FFFFFF` focal surface, `#0E7C61` banyan accent), while `dark` models nocturnal ink (`#0D1311` canvas, `#151E1B` surface, `#2DD4BF` teal accent).

Each client layers four orthogonal campus theme accents (`banyan`, `kapok`, `haizhu`, `bauhinia`) and native Dynamic Type / font scaling on top of these shared baseline roles without hand-editing any generated file.
