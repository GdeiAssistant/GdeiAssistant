<%@ page import="java.util.Date" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>广东二师助手</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/index/index${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/cropper.min.js"></script>
    <script type="text/javascript" src="/js/common/exif.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div class="weui-tab">

    <div class="weui-tab__panel">

        <div class="phone">

            <div class="tabPanelItem">

                <jsp:include page="/WEB-INF/view/Function/function.jsp"/>

            </div>

            <div class="tabPanelItem" style="display: none">

                <jsp:include page="/WEB-INF/view/Info/info.jsp"/>

            </div>

            <div class="tabPanelItem" style="display: none;">

                <jsp:include page="/WEB-INF/view/Profile/profile.jsp"/>

            </div>

        </div>

    </div>

    <div class="weui-tabbar">
        <a href="javascript:" onclick="switchTabPanel(0)" class="weui-tabbar__item weui-bar__item_on">
            <img src="/img/index/home.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">功能主页</p>
        </a>
        <a href="javascript:" onclick="switchTabPanel(1)" class="weui-tabbar__item">
            <img src="/img/index/info.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">资讯信息</p>
        </a>
        <a href="javascript:" onclick="switchTabPanel(2)" class="weui-tabbar__item">
            <img src="/img/index/personal.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">个人中心</p>
        </a>
    </div>
</div>

</body>
</html>
