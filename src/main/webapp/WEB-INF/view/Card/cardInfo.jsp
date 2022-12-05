<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的校园卡</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/card/cardInfo.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">我的校园卡</h1>
</div>

<div class="weui-cells__title">校园卡基本信息</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>姓名</p>
        </div>
        <div id="name" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>学号</p>
        </div>
        <div id="number" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡号</p>
        </div>
        <div id="cardNumber" class="weui-cell__ft"></div>
    </div>
</div>

<div class="weui-cells__title">校园卡余额</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡余额</p>
        </div>
        <div id="cardBalance" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡过渡余额</p>
        </div>
        <div id="cardInterimBalance" class="weui-cell__ft"></div>
    </div>
</div>

<div class="weui-cells__title">校园卡状态</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡挂失状态</p>
        </div>
        <div id="cardLostState" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡冻结状态</p>
        </div>
        <div id="cardFreezeState" class="weui-cell__ft"></div>
    </div>
</div>

<p class="page_desc" style="margin-top: 25px;margin-bottom: 25px">校园卡遗失？点击
    <a class="page_desc" onclick="window.location.href='/cardlost'">校园卡挂失</a>
</p>

</body>
</html>
