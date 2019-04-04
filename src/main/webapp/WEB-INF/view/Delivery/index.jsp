<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>全民快递</title>
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
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/index/index${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/delivery/delivery.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <style>
        .avatar {
            width: 100%;
            height: 100%;
            border-radius: 50px;
        }

        .avatarDiv {
            width: 100px;
            height: 100px;
            margin: 25px auto
        }

        .kickname {
            width: 60%;
            text-align: center;
            margin: 0 auto 1.5rem;
        }

        .kickname h2 {
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
    </style>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="avatarDiv">
    <img class="avatar" src="${AvatarURL==null ? "/img/avatar/default.png" : AvatarURL}">
</div>

<div class="kickname">
    <h2 class="weui-msg__title">${KickName}</h2>
</div>

<div class="phone">
    <div class="links">
        <div onclick="window.location.href='/delivery/order'">
            <i style="background: url(/img/delivery/order.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>我要下单</p>
        </div>
        <div onclick="window.location.href='/delivery/accept'">
            <i style="background: url(/img/delivery/take.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>我要接单</p>
        </div>
        <div onclick="window.location.href='/delivery/personal'">
            <i style="background: url(/img/delivery/personal.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>个人中心</p>
        </div>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
