# Third-Party Notices

本项目包含服务端、前端、构建脚本与公开演示资源层面的第三方开源组件及数据。实际依赖集合以仓库源码、依赖锁文件（`frontend/package-lock.json`、`build.gradle`）以及最终发布产物为准。

## 关键组件与数据归属

- **Spring Boot / Spring Framework / MyBatis-Plus**：Apache License 2.0
- **Vue 3 / Vue Router / Vue I18n / Vite / Tailwind CSS / Reka UI**：MIT License
- **Lucide Icons（`lucide-vue-next`）**：ISC License
- **Ip2Region Java 查询库**：Apache License 2.0（[lionsoul2014/ip2region](https://github.com/lionsoul2014/ip2region)）
- **OpenCC4j**：Apache License 2.0（离线生成简繁地名对照）
- **GeoNames 地名快照数据**：[Creative Commons Attribution 4.0](https://creativecommons.org/licenses/by/4.0/)，署名 GeoNames（`https://www.geonames.org/`），详情见 [`docs/i18n/location-names.md`](docs/i18n/location-names.md)
- **前端 Mock 演示图片**：CC0 / Public Domain（Wikimedia Commons 与项目合成素材，详情见 [`frontend/public/img/mock-demo/sources.md`](frontend/public/img/mock-demo/sources.md)）

## 依赖核对命令

正式构建或发行衍生版本前，可通过以下命令导出完整依赖树与容器运行时清单，核对具体版本与许可证义务：

```bash
npm --prefix frontend ls --all
./gradlew dependencies
docker compose config
```
