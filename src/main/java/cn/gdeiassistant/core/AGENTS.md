# core 模块架构约束

`src/main/java/cn/gdeiassistant/core/` 下的业务模块遵循统一边界约定，由 `src/test/java/cn/gdeiassistant/architecture/LayerDependencyGuardTest.java` 在 CI 中守卫基线。

## 依赖与分层

- `core/<module>` 之间禁止新增直接注入其他模块的 `mapper.*Mapper`，跨领域调用统一经由目标模块的 `*Service` 或 `common/spi/*` 接口。
- 禁止新增 `core/*` 模块之间的双向包依赖环；清理历史跨模块 Mapper 或双向环后须同步缩减守卫测试中的遗留基线。
- 禁止向 `common/` 或 `integration/` 反向引入 `core.*` 依赖；新增跨层能力先在 `common/spi/` 定义接口，再由 `core/` 实现。

## 序列化与网络客户端

- 新增类统一使用 Spring / Jackson 与 `integration/httpclient` / OkHttp。
- 禁止在任何 Java 类中新增 `com.alibaba.fastjson2`、`com.google.gson` 或 `org.apache.http.nio`（`httpasyncclient`）依赖。

## 改动后验证

修改 `core/` 或架构边界后，提交前执行：

```bash
./gradlew test --tests "cn.gdeiassistant.architecture.*"
```
