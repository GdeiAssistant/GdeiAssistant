<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:include page="top.jsp"/>

<div class="banxin">

    <div class="app_logo margin-bottom60">
        <img src="/img/about/application/logo.png" width="120" height="120">
    </div>

    <!-- 功能模块启用状态检查结果提示信息 仅开发模式下显示 -->
    <div class="function-module-check-result margin-bottom60">
        <text class='core-module-check-result-text'></text>
        <text class='module-check-result-text'></text>
    </div>

    <div class="main-function-items-area">
        <div class="login-items" onclick="window.location.href='/login'">
            <div>
                <strong>
                    <a href="javascript:">进入系统</a>
                </strong>
            </div>
        </div>
    </div>

    <hr class="margin-bottom60">
    <div class="desp">
        <h2>应用介绍</h2>
        <div class="dsp margin-bottom60">
            <section>
                广东第二师范学院校园助手，是为广东第二师范学院专属打造的校园服务应用。它不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、教学质量评价、电费查询、黄页信息查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、拍好校园、话题、匿名评教等社区交流平台。广东二师助手旨在为广东第二师范学院的在校师生们提供最优质的教育教学、校园生活、社团活动、文化娱乐和教务服务等信息。四年时光，广东二师助手陪你一起走过。
            </section>
        </div>
    </div>
    <hr class="margin-bottom60">

    <div>
        <h2>应用截图</h2>
        <div class="table-responsive">
            <table class="table">
                <tbody>
                <tr>
                    <td>
                        <div>
                            <img src="/img/about/application/preview_0.jpg">
                        </div>
                    </td>
                    <td>
                        <div>
                            <img src="/img/about/application/preview_1.jpg">
                        </div>
                    </td>
                    <td>
                        <div>
                            <img src="/img/about/application/preview_2.jpg">
                        </div>
                    </td>
                    <td>
                        <div>
                            <img src="/img/about/application/preview_3.jpg">
                        </div>
                    </td>
                    <td>
                        <div>
                            <img src="/img/about/application/preview_4.jpg">
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <hr class="margin-bottom60">

<jsp:include page="bottom.jsp"/>