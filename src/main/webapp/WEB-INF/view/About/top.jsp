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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/about/about.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/bootstrap.min.js"></script>
    <script type="text/javascript" src="/js/common/swiper.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <jsp:include page="/js/about/about.jsp"/>
</head>
<body>

<div class="margin-bottom60">
    <nav class="navbar navbar-expand-lg navbar-light bg-light">
        <a class="navbar-brand" href="/">广东二师助手</a>
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
                        <a class="dropdown-item" href="/policy/intellectualproperty">知识产权声明</a>
                    </div>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="javascript:" role="button"
                       data-toggle="dropdown"
                       aria-haspopup="true" aria-expanded="false">
                        友情链接
                    </a>
                    <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="http://www.moe.gov.cn">教育部</a>
                        <a class="dropdown-item" href="https://edu.gd.gov.cn">广东省教育厅</a>
                        <a class="dropdown-item" href="http://jyj.gz.gov.cn">广州市教育局</a>
                        <a class="dropdown-item" href="https://www.gd.gov.cn">广东省人民政府</a>
                        <a class="dropdown-item" href="http://www.gz.gov.cn">广州市人民政府</a>
                        <a class="dropdown-item" href="http://www.edu.cn">中国教育科研网</a>
                        <a class="dropdown-item" href="https://www.cnki.net">中国知网</a>
                        <a class="dropdown-item" href="https://www.chsi.com.cn">学信网</a>
                        <div class="dropdown-divider"></div>
                        <a class="dropdown-item" href="https://www.scut.edu.cn">华南理工大学</a>
                        <a class="dropdown-item" href="https://www.sysu.edu.cn">中山大学</a>
                        <a class="dropdown-item" href="https://www.jnu.edu.cn">暨南大学</a>
                        <a class="dropdown-item" href="https://www.scnu.edu.cn">华南师范大学</a>
                        <a class="dropdown-item" href="http://www.gdei.edu.cn">广东第二师范学院</a>
                    </div>
                </li>
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle" href="javascript:" role="button"
                       data-toggle="dropdown"
                       aria-haspopup="true" aria-expanded="false">
                        智慧党建
                    </a>
                    <div class="dropdown-menu" aria-labelledby="navbarDropdown">
                        <a class="dropdown-item" href="http://cpc.people.com.cn/index.html">中国共产党新闻网</a>
                        <a class="dropdown-item"
                           href="http://chuxin.people.cn/GB/index.html">“不忘初心、牢记使命”主题教育</a>
                        <a class="dropdown-item"
                           href="http://cpc.people.com.cn/GB/67481/431601/index.html">人民战“疫”党旗飘扬</a>
                        <a class="dropdown-item" href="http://dangshi.people.cn">党史学习教育</a>
                        <a class="dropdown-item"
                           href="http://dangjian.people.com.cn/GB/136058/447038/index.html">我奋斗家国美</a>
                        <a class="dropdown-item" href="http://cpc.people.com.cn/20th">二十大专题报道</a>
                    </div>
                </li>
            </ul>
        </div>
    </nav>
</div>