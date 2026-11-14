# Shared Design Tokens (`Warm Editorial Lingnan Academy`)

`tokens.json` defines the shared **Lingnan Editorial** visual baseline across Web, Android, iOS, and WeChat Mini Program: 8 light/dark color pairs (`primary`, `primary-soft`, `bg`, `surface`, `text-1`, `text-2`, `danger`, `warning`), 6 reference font sizes (`28/22/17/15/14/12`), `10`-unit card radius, and `16`-unit page spacing.

Run `node design/generate-tokens.mjs` from any working directory to regenerate:

- `frontend/src/styles/design-tokens.generated.css` (`px` CSS custom properties)
- `design/generated/GeneratedDesignTokens.kt` (Compose `sp` / `dp` object)
- `design/generated/GeneratedDesignTokens.swift` (SwiftUI `CGFloat` / RGB constants)
- `design/generated/design-tokens.wxss` (`2 rpx` per reference unit for WeChat Mini Program)

Run `node design/generate-tokens.mjs --check` to verify parity without rewriting files. Frontend CI enforces this check on every pull request.

The generated `light` palette models warm paper (`#F7F6F2` canvas, `#FFFFFF` focal surface, `#0E7C61` banyan accent), while `dark` models nocturnal ink (`#0D1311` canvas, `#151E1B` surface, `#2DD4BF` teal accent).

Each client layers four orthogonal campus theme accents (`banyan`, `kapok`, `haizhu`, `bauhinia`) and native Dynamic Type / font scaling on top of these shared baseline roles without hand-editing any generated file.
