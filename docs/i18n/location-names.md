# 地区显示名来源与边界

四端共享 4279 个历史目录节点的业务码、中文源名称、顺序和层级；语言切换只调整显示名，数据库与用户输入的自由文本不被翻译替换。

目录中 2144 个省市及重复国家层级带有显式国际显示名，加上原有国家名称，`en`、`ja`、`ko` 各覆盖 2380 个显式名称，`zh-HK` 与 `zh-TW` 覆盖全部 4279 个节点，日本 47 都道府县均已完整覆盖。旧源节点「枥木」保留其原始业务代码与源名称，在六种语言显示名中更正为「栃木 / Tochigi」。

`GUF` 的外部 ISO 标记按 [GeoNames countryInfo](https://download.geonames.org/export/dump/countryInfo.txt) 更正为 `GF`（对应法属圭亚那，与圭亚那 `GUY` / `GY` 区分），法属圭亚那的六语言显示名取自 Node 24.14.1 的 Intl/CLDR 国家名，保留原业务码；仅含糊写作「圭亚那」的历史文本不猜测归属。纽约市采用 GeoNames 5128581 主记录 `New York City` 以区分纽约州 `New York`，参见 [纽约市政府](https://www.nyc.gov/main/about-our-content)。

地名数据来源于 [GeoNames 官方每日导出](https://download.geonames.org/export/dump/)（2026-10-06 快照 `allCountries.zip` 与 `alternateNamesV2.zip`），按 [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/) 许可筛选并改编，归属：GeoNames（`https://www.geonames.org/`）。栃木名称亦核对 [栃木县政府](https://www.pref.tochigi.lg.jp/index.html)，重复国家层级沿用原有国家译名。

匹配条件严格限定为国家、中文名称与已确定的上级行政区，多个候选仅在三语名称一致或确认同一县级辖区时采用；日、韩无源译名时采用英文或当地原名，不生成中文拼音译名，并保留人工核对的广东、广州、汕头、佛山常用标签。

其余 1899 个旧节点因重名歧义、历史区划变更或缺乏匹配记录保留原有回退显示，不表示全球目录已全量人工核译；后续补充需逐条确认地理实体，不可按业务码猜测。

可追溯数据源为 `frontend/scripts/location-names-geonames.json`（含 `geonameId`、匹配依据及显示名）。运行 `node frontend/scripts/apply-location-names.mjs` 生成 Web 目录，运行 `--check` 校验一致；源快照仅用于离线构建生成，不引入运行时服务或依赖。
