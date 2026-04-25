# Third-Party Notices

本项目包含前端、后端、构建、运行时和部署层面的第三方依赖。实际依赖集合以源码、锁文件、构建脚本和最终发布产物为准。

## 说明

- 前端依赖通常以 `frontend/package-lock.json`、`frontend/package.json` 或其他实际存在的锁文件为准。
- 后端依赖通常以 `build.gradle`、Gradle 解析结果和发布产物中的实际依赖为准。
- Docker、基础镜像和运行时环境还可能引入额外的第三方组件及其许可证义务。

## 当前可从仓库确认的主要技术栈类别

- Vue 3
- Vite
- Spring Boot
- Gradle
- Docker
- MySQL / Redis / MongoDB（按部署配置启用）

以上仅为技术栈类别说明，不代表完整依赖清单。

## 维护建议

正式版本发布前，维护者应生成或更新第三方依赖许可证清单，并核对：

- 前端包管理器锁文件
- Gradle 依赖树
- Docker 基础镜像和运行时镜像
- 实际打包进发布物的第三方资源

## 建议的核对命令

以下命令可作为整理清单时的起点，不会自动生成完整许可证报告，仅用于排查实际依赖：

```bash
npm --prefix frontend ls --depth=0
./gradlew dependencies
docker compose config
```

## TODO

- 在正式发布流程中补充可复用的第三方许可证清单生成步骤
- 根据实际发布物补齐依赖名称、版本和许可证信息

最近更新：2026年4月25日
