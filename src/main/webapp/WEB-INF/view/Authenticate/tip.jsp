<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>未完成实名认证</title>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

    </script>
</head>
<body>

<div class="weui-msg">
    <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
    <div class="weui-msg__text-area">
        <h2 class="weui-msg__title">未完成实名认证</h2>
        <p class="weui-msg__desc">
            根据《中华人民共和国网络安全法》第二十四条的规定要求，从2019年8月1日起，用户使用广东二师助手需要首先完成实名认证，完成实名认证后用户才可使用正常使用广东二师助手。</p>
    </div>
    <div class="weui-msg__opr-area">
        <p class="weui-btn-area">
            <a onclick="window.location.href='/authentication'" href="javascript:"
               class="weui-btn weui-btn_primary">进入实名认证</a>
        </p>
        <p class="weui-btn-area">
            <a onclick="window.location.href='/index'" href="javascript:"
               class="weui-btn weui-btn_default">已完成实名认证</a>
        </p>
        <p class="weui-btn-area">
            <a onclick="localStorage.clear();window.location.href='/logout'" href="javascript:"
               class="weui-btn weui-btn_default">退出当前账号</a>
        </p>
    </div>
</div>

</body>
</html>
