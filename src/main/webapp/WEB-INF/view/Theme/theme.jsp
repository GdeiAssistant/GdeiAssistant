<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>主题设置</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
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
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/theme/theme.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">主题配置</h1>
</div>

<div class="weui-cells__title">选择你的主题</div>
<div class="weui-cells weui-cells_radio">
    <label class="weui-cell weui-check__label" for="default">
        <div class="weui-cell__bd">
            <p>默认主题</p>
        </div>
        <div class="weui-cell__ft">
            <input type="radio" class="weui-check" name="theme" id="default" checked="checked">
            <span class="weui-icon-checked"></span>
        </div>
    </label>
    <label class="weui-cell weui-check__label" for="pink">
        <div class="weui-cell__bd">
            <p>粉色主题</p>
        </div>
        <div class="weui-cell__ft">
            <input type="radio" class="weui-check" name="theme" id="pink">
            <span class="weui-icon-checked"></span>
        </div>
    </label>
    <label class="weui-cell weui-check__label" for="blue">
        <div class="weui-cell__bd">
            <p>蓝色主题</p>
        </div>
        <div class="weui-cell__ft">
            <input type="radio" class="weui-check" name="theme" id="blue">
            <span class="weui-icon-checked"></span>
        </div>
    </label>
</div>

<br>

</body>
</html>