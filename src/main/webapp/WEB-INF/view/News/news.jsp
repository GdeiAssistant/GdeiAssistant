<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>新闻通知</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/news/news.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-tab">

    <div class="weui-tab__panel">

        <div class="weui-cells__title" onclick="history.go(-1)">返回</div>

        <div class="hd">
            <h1 class="page_title">新闻通知</h1>
            <p style="text-align: center;color: lightgrey">新闻信息服务由广东第二师范学院网站提供，本站仅作收录和展示</p>
        </div>

        <div style="margin-top: 0;" class="weui-cells">

        </div>

        <p style="text-align: center;color: lightgrey;margin-top: 1rem;margin-bottom: 2rem" id="loadMore">点击加载更多新闻通知</p>

    </div>

    <div class="weui-tabbar">
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/course.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">教学信息</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/examine.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">考试信息</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/study.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">教务信息</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/admin.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">行政通知</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/school.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">综合信息</p>
        </a>
    </div>
</div>

</body>
</html>
