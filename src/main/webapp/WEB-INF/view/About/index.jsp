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
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.min.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link title="pink" rel="alternate stylesheet" href="/css/common/jquery-weui.min_pink.css">
    <link title="blue" rel="alternate stylesheet" href="/css/common/jquery-weui.min_blue.css">
    <link rel="stylesheet" type="text/css" href="/css/common/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/about/about.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/common/swiper.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
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
                        <div class="dropdown-divider"></div>
                        <!-- This item has been removed -->
                        <!-- This item has been removed -->
                    </div>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="https://github.com/GdeiAssistant/GdeiAssistant">程序源代码</a>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link" href="/announcement/equalrights">反歧视声明</a>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link" href="/announcement/suicideprevention">青少年自杀干预热线</a>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link" href="https://ncov.dxy.cn/ncovh5/view/pneumonia">新型冠状病毒肺炎疫情实时动态</a>
                </li>
            </ul>
        </div>
    </nav>
</div>

<div class="banxin">

    <div class="app_logo margin-bottom60">
        <c:choose>
            <c:when test="${applicationScope.get('pinkthemelogo')}">
                <img src="/img/about/logo_pink.png" width="120" height="120">
            </c:when>
            <c:when test="${applicationScope.get('pridethemelogo')}">
                <img src="/img/about/logo_pride.png" width="120" height="120">
            </c:when>
            <c:otherwise>
                <img src="/img/about/logo.png" width="120" height="120">
            </c:otherwise>
        </c:choose>
    </div>

    <div class="download">
        <div class="download_button" onclick="window.location.href='/login'">
            <img width="35px" height="35px" src="/img/about/chrome_icon.png">
            <strong>
                <a href="javascript:">网页版</a>
            </strong>
        </div>
        <div class="download_button" onclick="androidDownload()">
            <img width="35px" height="35px" src="/img/about/android_icon.png">
            <strong>
                <a href="javascript:">Android</a>
            </strong>
        </div>
        <div class="download_button" onclick="iOSDownload()">
            <img width="35px" height="35px" src="/img/about/ios_icon.png">
            <strong>
                <a href="javascript:">iOS</a>
            </strong>
        </div>
        <div class="download_button" onclick="androidGooglePlayDownload()">
            <img width="35px" height="35px" src="/img/about/google_play_icon.png">
            <strong>
                <a href="javascript:">Google Play</a>
            </strong>
        </div>
        <div class="download_button" onclick="UWPDownload()">
            <img width="35px" height="35px" src="/img/about/uwp_icon.png">
            <strong>
                <a href="javascript:">Windows Store</a>
            </strong>
        </div>
        <div class="download_button" onclick="QuickApp()">
            <img width="35px" height="35px" src="/img/about/quick_app_icon.png">
            <strong>
                <a href="javascript:">快应用</a>
            </strong>
        </div>
        <div class="download_button" onclick="Wechat()">
            <img width="35px" height="35px" src="/img/about/wechat_icon.png">
            <strong>
                <a href="javascript:">微信公众号</a>
            </strong>
        </div>
        <div class="download_button" onclick="WechatApp()">
            <img width="35px" height="35px" src="/img/about/wechat_app_icon.png">
            <strong>
                <a href="javascript:">微信小程序</a>
            </strong>
        </div>
        <div class="download_button" onclick="QQApp()">
            <img width="35px" height="35px" src="/img/about/qq_app_icon.png">
            <strong>
                <a href="javascript:">QQ小程序</a>
            </strong>
        </div>
        <div class="download_button" onclick="AlipayApp()">
            <img width="35px" height="35px" src="/img/about/alipay_icon.png">
            <strong>
                <a href="javascript:">支付宝小程序</a>
            </strong>
        </div>
    </div>

    <hr class="margin-bottom60">
    <div class="desp">
        <h2>应用介绍</h2>
        <div class="dsp margin-bottom60">
            <section>
                广东二师助手，别名易小助，是为广东第二师范学院专属打造的校园服务应用。它不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、教学质量评价、电费查询、黄页信息查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、拍好校园、话题、匿名评教等社区交流平台。广东二师助手旨在为广东第二师范学院的在校师生们提供最优质的教育教学、校园生活、社团活动、文化娱乐和教务服务等信息。四年时光，广东二师助手陪你一起走过。
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
                            <img src="/img/about/screenshot_01.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/screenshot_02.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/screenshot_05.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/screenshot_04.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/screenshot_03.jpg">
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <hr class="margin-bottom60">
    <div class="foot">
        <p>Copyright © 2016 - 2020 GdeiAssistant</p>
        <p>All rights reserved</p>
        <div class="beian_area">
            <p class="beian_p"><a class="beian" href="http://www.beian.miit.gov.cn">粤ICP备17087427号-1</a></p>
            <p class="beian_p"><a class="beian"
                                  href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297">粤公网安备44010502001297号</a>
            </p>
        </div>
    </div>

</div>

<div class="wxtip" id="JweixinTip">
    <span class="wxtip-icon"></span>
    <p class="wxtip-txt">点击右上角<br>选择在其他浏览器中打开</p>
</div>

<div id="rnw_cookies_banner" style="display: none">
    <a class="spritefont-close" id="rnw_cookies_banner_button_close" title="关闭"
       onclick="$('#rnw_cookies_banner').hide();localStorage.setItem('cookiePolicy',1);" href="javascript:">×</a>
    <div id="rnw_cookies_banner_text">
        本网站使用Cookie的目的是为您提供更加简捷和个性化的上网体验。Cookie将有用的信息存储在您的电脑上，可帮助我们改善您浏览我们网站的效率以及对您的实用性。在某些情况下，它们是网站正常运行所必不可少的。如果您访问本网站，即表示您同意我们使用Cookie。
        请点击<a href="/policy/cookie" id="rnw_cookies_banner_button_more">Cookie政策</a>了解更多信息。
    </div>
</div>

</body>
</html>