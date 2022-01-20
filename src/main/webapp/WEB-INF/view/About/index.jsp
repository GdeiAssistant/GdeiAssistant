<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <title>广东第二师范学院校园助手-广东第二师范学院必备校园服务应用</title>
    <meta name="keywords" content="校园,四六级,查分,课程表,二师助手,广二师助手,广东二师助手,广东第二师范学院">
    <meta name="description"
          content="广东第二师范学院校园助手，是为广东第二师范学院专属打造的校园服务应用。它不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、话题等社区交流平台。广东二师助手旨在为广东第二师范学院的在校师生们提供最优质的教育教学、校园生活、社团活动、文化娱乐和教务服务等信息。四年时光，广东二师助手陪你一起走过。">
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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/about/about.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/common/swiper.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <jsp:include page="/js/about/about.jsp"/>
</head>
<body>

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
                        <a class="dropdown-item" href="/about/security">安全技术规格说明</a>
                        <a class="dropdown-item" href="/about/account">校园网络账号说明</a>
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
                        <a class="dropdown-item" href="http://www.gd.gov.cn">广东省人民政府</a>
                        <a class="dropdown-item" href="http://www.gz.gov.cn">广州市人民政府</a>
                        <a class="dropdown-item" href="http://www.edu.cn">中国教育科研网</a>
                        <a class="dropdown-item" href="https://www.cnki.net">中国知网</a>
                        <a class="dropdown-item" href="http://www.gdei.edu.cn">广东第二师范学院</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" href="http://cpc.people.com.cn/index.html">中国共产党新闻网</a>
                        <a class="dropdown-item" href="http://chuxin.people.cn/GB/index.html">“不忘初心、牢记使命”主题教育</a>
                        <a class="dropdown-item" href="http://dangshi.people.cn">党史学习教育</a>
                        <a class="dropdown-item" href="https://www.enaea.edu.cn">干部在线学习中心</a>
                        <a class="dropdown-item" href="http://cpc.people.com.cn/19th">十九大专题报道</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item"
                           href="http://www.nhc.gov.cn/xcs/xxgzbd/gzbd_index.shtml">新型冠状病毒肺炎疫情防控专题</a>
                        <a class="dropdown-item" href="https://ncov.dxy.cn/ncovh5/view/pneumonia">新型冠状病毒肺炎疫情实时动态</a>
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
            </ul>
        </div>
    </nav>
</div>

<div class="banxin">

    <div class="app_logo margin-bottom60">
        <img src="/img/about/logo.png" width="120" height="120">
    </div>

    <!-- 功能模块启用状态检查结果提示信息 仅开发模式下显示 -->
    <div class="function-module-check-result margin-bottom60">
        <text class='core-module-check-result-text'></text>
        <text class='module-check-result-text'></text>
    </div>

    <div class="download">
        <div class="download_button" onclick="window.location.href='/login'">
            <div>
                <img width="35px" height="35px" src="/img/about/web_icon.png">
                <strong>
                    <a href="javascript:">点击进入系统</a>
                </strong>
            </div>
        </div>
        <div class="download_button" onclick="downloadAPP()">
            <div>
                <img width="35px" height="35px" src="/img/about/app_icon.png">
                <strong>
                    <a href="javascript:">下载APP客户端</a>
                </strong>
            </div>
        </div>
        <div class="download_button" onclick="showWechatQRCode()">
            <div>
                <img width="35px" height="35px" src="/img/about/wechat_icon.png">
                <strong>
                    <a href="javascript:">公众号和小程序</a>
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
                        <div style="">
                            <img src="/img/about/preview_0.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/preview_1.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/preview_2.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/preview_3.jpg">
                        </div>
                    </td>
                    <td>
                        <div style="">
                            <img src="/img/about/preview_4.jpg">
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
    </div>

    <hr class="margin-bottom60">
    <div class="foot">
        <p>Copyright © 2016 - 2021 GdeiAssistant</p>
        <p>All rights reserved</p>
        <div class="beian_area">
            <p class="beian_p"><a class="beian" href="http://www.beian.miit.gov.cn">粤ICP备17087427号-1</a></p>
            <p class="beian_p"><a class="beian"
                                  href="http://www.beian.gov.cn/portal/registerSystemInfo?recordcode=44010502001297">粤公网安备44010502001297号</a>
            </p>
        </div>
    </div>
</div>

<div id="rnw_cookies_banner" style="display: none">
    <a class="spritefont-close" id="rnw_cookies_banner_button_close" title="关闭"
       onclick="$('#rnw_cookies_banner').hide();localStorage.setItem('cookiePolicy',1);" href="javascript:">×</a>
    <div id="rnw_cookies_banner_text">
        本网站使用Cookie的目的是为您提供更加简捷和个性化的上网体验。Cookie将有用的信息存储在您的电脑上，可帮助我们改善您浏览我们网站的效率以及对您的实用性。在某些情况下，它们是网站正常运行所必不可少的。如果您访问本网站，即表示您同意我们使用Cookie。
        请点击<a href="/policy/cookie" id="rnw_cookies_banner_button_more">Cookie政策</a>了解更多信息。
    </div>
</div>


<br>

</body>
</html>