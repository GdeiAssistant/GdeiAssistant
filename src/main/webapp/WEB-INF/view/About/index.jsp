<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>广东二师助手-广东第二师范学院必备校园服务应用</title>
    <meta name="keywords" content="校园,四六级,查分,课程表,二师助手,广二师助手,广东二师助手,广东第二师范学院">
    <meta name="description"
          content="广东二师助手，别名易小助，是为广东第二师范学院专属打造的校园服务应用。它不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、话题等社区交流平台。广东二师助手旨在为广东第二师范学院的在校师生们提供最优质的教育教学、校园生活、社团活动、文化娱乐和教务服务等信息。四年时光，广东二师助手陪你一起走过。">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telphone=no, email=no">
    <meta name="renderer" content="webkit">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="HandheldFriendly" content="true">
    <meta name="MobileOptimized" content="320">
    <meta name="screen-orientation" content="portrait">
    <meta name="x5-orientation" content="portrait">
    <meta name="full-screen" content="yes">
    <meta name="x5-fullscreen" content="true">
    <meta name="browsermode" content="application">
    <meta name="x5-page-mode" content="app">
    <meta name="msapplication-tap-highlight" content="no">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" type="text/css" href="/css/about/about.css">
    <link rel="stylesheet" type="text/css" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" type="text/css" href="/css/common/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/common/swiper.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/about/about.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<input type="hidden" id="android_url" value="#">
<input type="hidden" id="android_google_play_url" value="#">
<input type="hidden" id="android_amazon_url" value="#">
<input type="hidden" id="uwp_url" value="#">

<div class="margin-bottom60">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <a class="navbar-brand" href="javascript:">广东二师助手</a>
        <button class="navbar-toggler" type="button" data-toggle="collapse" data-target="#navbarSupportedContent"
                aria-controls="navbarSupportedContent" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="navbarSupportedContent">
            <ul class="navbar-nav mr-auto">
                <li class="nav-item">
                    <a class="nav-link" href="javascript:">主页</a>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="javascript:" role="button"
                       data-toggle="dropdown"
                       aria-haspopup="true" aria-expanded="false">
                        使用帮助
                    </a>
                    <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="/about/wechat">微信查询使用说明</a>
                        <a class="dropdown-item" href="/about/security">安全技术规格说明</a>
                        <a class="dropdown-item" href="/about/account">教务系统账号说明</a>
                        <a class="dropdown-item" href="/about/graduation">毕业处理方案说明</a>
                    </div>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="javascript:" role="button"
                       data-toggle="dropdown"
                       aria-haspopup="true" aria-expanded="false">
                        协议与政策
                    </a>
                    <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="/agreement">用户协议</a>
                        <a class="dropdown-item" href="/policy/privacy">隐私政策</a>
                        <a class="dropdown-item" href="/policy/social">社区准则</a>
                        <a class="dropdown-item" href="/license">开源协议</a>
                        <a class="dropdown-item" href="/policy/cookie">Cookie政策</a>
                    </div>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="javascript:" role="button"
                       data-toggle="dropdown"
                       aria-haspopup="true" aria-expanded="false">
                        友情链接
                    </a>
                    <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="http://www.moe.gov.cn">教育部门户网</a>
                        <a class="dropdown-item" href="http://edu.gd.gov.cn">广东省教育厅</a>
                        <a class="dropdown-item" href="http://www.gzedu.gov.cn">广州市教育局</a>
                        <a class="dropdown-item" href="http://www.cnki.net">中国知网平台</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" href="http://www.sysu.edu.cn">中山大学</a>
                        <a class="dropdown-item" href="https://www.jnu.edu.cn">暨南大学</a>
                        <a class="dropdown-item" href="https://www.scut.edu.cn">华南理工大学</a>
                        <a class="dropdown-item" href="http://www.scnu.edu.cn">华南师范大学</a>
                        <a class="dropdown-item" href="http://www.gdei.edu.cn">广东第二师范学院</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" href="http://www.gdsyzx.edu.cn">广东实验中学</a>
                        <a class="dropdown-item" href="http://www.gyzx.edu.cn">广东广雅中学</a>
                        <a class="dropdown-item" href="https://www.zhixinhs.cn">广州市执信中学</a>
                        <a class="dropdown-item" href="https://www.gztlms.com">广州市真光中学</a>
                        <a class="dropdown-item" href="http://www.gz41zx.com">广州市第四十一中学</a>
                        <a class="dropdown-item" href="http://www.hsfz.net.cn">华南师范大学附属中学</a>
                    </div>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link" href="mailto:report@gdeiassistant.cn">违规举报</a>
                    <!-- 具体违规举报分类的下拉选择选项组件已弃用 -->
                    <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="http://www.12377.cn">违法和不良信息举报</a>
                        <a class="dropdown-item" href="https://www.12321.cn">网络不良与垃圾信息举报</a>
                        <a class="dropdown-item" href="http://www.12389.gov.cn/clue_evilNotice.action">涉黑涉恶犯罪线索举报</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" href="http://www.shdf.gov.cn/shdf/channels/740.html">儿童色情信息举报</a>
                        <a class="dropdown-item" href="http://www.cyberpolice.cn">网络违法犯罪举报</a>
                        <a class="dropdown-item" href="https://jubao.nifa.org.cn/ipnifa/index.html">非法校园借贷举报</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item"
                           href="http://www.moe.gov.cn/jyb_hygq/hygq_tsjb/201505/t20150520_184529.html">教育部统一监督举报</a>
                        <a class="dropdown-item" href="https://dxss.miit.gov.cn">工信部电信用户申诉</a>
                        <a class="dropdown-item" href="http://sswz.spb.gov.cn">邮政快递消费者申诉</a>
                        <a class="dropdown-item" href="http://www.12315.cn">市场监督管理局申诉</a>
                        <a class="dropdown-item" href="http://1.202.247.200/netreport/netreport/index">环境保护与污染举报</a>
                        <a class="dropdown-item" href="http://www.12331.org.cn">食品药品监管局举报</a>
                        <a class="dropdown-item" href="http://jbts.mct.gov.cn/12318">文化与娱乐市场举报</a>
                        <a class="dropdown-item"
                           href="http://www.sapprft.gov.cn/sapprft/channels/6979.shtml">互联网盗版侵权举报</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" href="mailto:report@gdeiassistant.cn">社区用户违规举报</a>
                    </div>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="https://github.com/GdeiAssistant/GdeiAssistant">程序源代码</a>
                </li>
            </ul>
        </div>
    </nav>
</div>

<div class="banxin">

    <div class="app_logo margin-bottom60">
        <c:choose>
            <c:when test="${applicationScope.get('pridetheme')}">
                <img src="/img/download/logo_pride.png" width="120" height="120">
            </c:when>
            <c:otherwise>
                <img src="/img/download/logo.png" width="120" height="120">
            </c:otherwise>
        </c:choose>
    </div>

    <div class="download">
        <div class="web" onclick="window.location.href='/login'">
            <h2>
                <strong>
                    <a href="javascript:">网页版</a>
                </strong>
            </h2>
        </div>
        <div class="android_download" onclick="androidDownload()">
            <h2>
                <strong>
                    <a href="javascript:">Android下载</a>
                </strong>
            </h2>
        </div>
        <div class="google_play_download" onclick="androidGooglePlayDownload()">
            <h2>
                <strong>
                    <a href="javascript:">Google Play下载</a>
                </strong>
            </h2>
        </div>
        <div class="amazon_download" onclick="androidAmazonDownload()">
            <h2>
                <strong>
                    <a href="javascript:">Amazon下载</a>
                </strong>
            </h2>
        </div>
        <div class="ios_download" onclick="iOSDownload()">
            <h2>
                <strong>
                    <a href="javascript:">App Store下载</a>
                </strong>
            </h2>
        </div>
        <div class="uwp_download" onclick="UWPDownload()">
            <h2>
                <strong>
                    <a href="javascript:">Windows Store下载</a>
                </strong>
            </h2>
        </div>
        <div class="wechat" onclick="Wechat()">
            <h2>
                <strong>
                    <a>微信公众号</a>
                </strong>
            </h2>
        </div>
        <div class="wechat_app" onclick="WechatApp()">
            <h2>
                <strong>
                    <a>微信小程序</a>
                </strong>
            </h2>
        </div>
        <div class="alipay_app" onclick="AlipayApp()">
            <h2>
                <strong>
                    <a>支付宝小程序</a>
                </strong>
            </h2>
        </div>
        <div class="quick_app" onclick="QuickApp()">
            <h2>
                <strong>
                    <a href="javascript:">快应用</a>
                </strong>
            </h2>
        </div>
    </div>

    <hr class="margin-bottom60">
    <div class="desp">
        <h2>应用介绍</h2>
        <div class="dsp margin-bottom60">
            <section>
                广东二师助手，别名易小助，是为广东第二师范学院专属打造的校园服务应用。它不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、教学质量评价、电费查询、黄页信息查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、话题、匿名评教等社区交流平台。广东二师助手旨在为广东第二师范学院的在校师生们提供最优质的教育教学、校园生活、社团活动、文化娱乐和教务服务等信息。四年时光，广东二师助手陪你一起走过。
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
                        <div style="">
                            <img src="/img/download/screenshot_01.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/download/screenshot_02.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/download/screenshot_05.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/download/screenshot_04.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/download/screenshot_03.jpg">
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <hr class="margin-bottom60">
    <div class="foot">
        <p>Copyright © 2016 - 2019 GdeiAssistant</p>
        <p>All rights reserved</p>
        <div class="beian_area">
            <p class="beian_p"><a class="beian" href="http://www.beian.miit.gov.cn">粤ICP备17087427号-1</a></p>
            <p class="beian_p"><img src="/img/about/ghs.png">
                <a class="beian" href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297">粤公网安备44010502001297号</a>
            </p>
        </div>
    </div>

</div>

<div class="wxtip" id="JweixinTip">
    <span class="wxtip-icon"></span>
    <p class="wxtip-txt">点击右上角<br>选择在其他浏览器中打开</p>
</div>

</body>
</html>
