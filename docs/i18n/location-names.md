# 地区显示名来源与边界

四端共享 4279 个历史目录节点的业务码、中文源名称、顺序和层级；语言切换只调整显示名，数据库与用户输入的自由文本不被翻译替换。

本次为 2144 个省市或重复国家层级补显式国际显示名。加上原有国家名称，en/ja/ko 各有 2380 个显式名称；zh-HK/zh-TW 各覆盖全部 4279 个节点。日本 47 都道府县已完整覆盖。旧源“枥木”保留其 code/name，在六种语言的显示名中纠正为栃木/Tochigi。

另纠正 GUF 的外部 ISO 标记为 GF（原为 GY），依据 [GeoNames countryInfo](https://download.geonames.org/export/dump/countryInfo.txt) 的 GF/GUF/French Guiana 与 GY/GUY/Guyana 对应关系。法属圭亚那的六语言显示名来自 Node 24.14.1 的 Intl/CLDR 国家名，业务码和原始名称保留，含糊的历史“圭亚那”不强行解释为任一国家。纽约市采用 GeoNames 5128581 主记录 `New York City`，与州名 `New York` 区分，亦见 [纽约市政府](https://www.nyc.gov/main/about-our-content)。

地名数据来自 [GeoNames 官方每日导出](https://download.geonames.org/export/dump/) 2026-10-06 快照，使用 allCountries.zip 与 alternateNamesV2.zip。GeoNames 数据许可为 [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/)，此目录为该数据的筛选和改编。归属：GeoNames（https://www.geonames.org/）。栃木名称另核对 [栃木县政府](https://www.pref.tochigi.lg.jp/index.html)。重复国家层级沿用原有国家译名，不将其归为 GeoNames 新译名。

匹配限制为国家、中文名称和已确定的上级行政区；多个候选只有在三种语言名称一致或已确认同一县级辖区的城市/行政记录时采用。没有按人口猜测同名城市。日、韩没有源译名时采用数据源的英文或当地原名，不生成中文拼音“译名”。已有人工核对的广东/广州/汕头/佛山标签优先保留。

剩余 1899 个旧节点因名称歧义、历史行政区划或没有匹配数据仍保留原有回退。这部分不是人工审核过的日/韩译名，也不宣称全球目录已经全译。更新它们需要逐条明确地理身份，不能凭业务码猜测 GeoNames/ISO 代码。

可追溯输入为 frontend/scripts/location-names-geonames.json，包含源 geonameId、匹配依据和实际采用的显示名。运行 `node frontend/scripts/apply-location-names.mjs` 更新 Web 生成目录，`--check` 检查输入与产物是否一致；源数据只参与开发时生成，不新增运行时服务或依赖。
