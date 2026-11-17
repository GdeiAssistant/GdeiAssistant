# Frontend Smoke 测试边界

在 `frontend` 目录执行 `npm run test:e2e` 运行 Playwright 端到端冒烟测试。

所有测试文件统一从 `./fixtures.js` 导入 `test` 与 `expect`。共享上下文夹具仅对 `index.html` 中引用的可选 WeUI CDN 脚本地址返回空操作脚本响应，不定义任何 SDK 全局变量，使测试不依赖外部 CDN 可用性。

该测试套件验证外部 SDK 不可用时应用内置控件的真实渲染与交互，不修改生产 HTML、断言或接口模拟逻辑；它不能替代真实外部 CDN、SDK 网络投递或线上环境端到端集成验证。
