<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>管理员选项</title>
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
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/admin/admin.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">管理员选项</h1>
</div>

<div class="weui-cells__title">外观选项</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">使用黑白网页</div>
        <div class="weui-cell__ft">
            <input id="grayscale" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(0)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">使用Pride主题</div>
        <div class="weui-cell__ft">
            <input id="pridetheme" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(1)">
        </div>
    </div>
</div>

<div class="weui-cells__title">实名认证</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">强制实名认证</div>
        <div class="weui-cell__ft">
            <input id="authentication_force" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(2)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">二手交易启用实名</div>
        <div class="weui-cell__ft">
            <input id="authentication_ershou" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(3)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">失物招领启用实名</div>
        <div class="weui-cell__ft">
            <input id="authentication_lostandfound" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(4)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">校园树洞启用实名</div>
        <div class="weui-cell__ft">
            <input id="authentication_secret" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(5)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">全民快递启用实名</div>
        <div class="weui-cell__ft">
            <input id="authentication_delivery" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(6)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">拍好校园启用实名</div>
        <div class="weui-cell__ft">
            <input id="authentication_photograph" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(7)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">表白墙启用实名</div>
        <div class="weui-cell__ft">
            <input id="authentication_express" class="weui-switch" type="checkbox"
                   onchange="changeAdminSetting(8)">
        </div>
    </div>
</div>

<div class="weui-cells__title">资讯信息</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/announcement/publish'">
        <div class="weui-cell__bd">
            <p>发布通知公告</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

</body>
</html>