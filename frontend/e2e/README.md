# Frontend smoke boundary

Run the existing suite with `npm run test:e2e` from `frontend`.

All specs import `test` and `expect` from `./fixtures.js`. The shared context
fixture fulfills only the exact optional WeUI CDN script URL in `index.html`
with a no-op JavaScript response. It defines no SDK globals and exercises the
app's existing built-in controls, independently of CDN availability. The
production HTML and application behavior are unchanged. Existing assertions,
local-data setup and API/upload mocks remain in their individual specs.

The smoke suite verifies rendered application interactions with this SDK
unavailable. Passing it does not verify the external SDK, its network delivery
or production integration; those require separate integration evidence.

所有用例统一从 `./fixtures.js` 导入测试接口，只对准确的可选 WeUI CDN
脚本地址返回空操作脚本，不创建 SDK 全局变量。测试验证 SDK 不可用时
的既有内置控件；不修改生产 HTML、应用、断言或各用例的 API 模拟，
也不证明真实 CDN/SDK 集成成功。
