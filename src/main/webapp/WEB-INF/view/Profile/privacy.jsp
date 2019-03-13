<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>隐私设置</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/profile/privacy.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">隐私设置</h1>
</div>

<div class="weui-cells__title">个人资料</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的性别</div>
        <div class="weui-cell__ft">
            <input id="gender" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(0)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的性取向</div>
        <div class="weui-cell__ft">
            <input id="genderOrientation" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(1)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的院系</div>
        <div class="weui-cell__ft">
            <input id="faculty" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(2)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的专业</div>
        <div class="weui-cell__ft">
            <input id="major" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(3)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的国家/地区</div>
        <div class="weui-cell__ft">
            <input id="region" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(4)">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的个人简介</div>
        <div class="weui-cell__ft">
            <input id="introduction" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(5)">
        </div>
    </div>
</div>

<div class="weui-cells__title">数据缓存</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">缓存我的教务数据</div>
        <div class="weui-cell__ft">
            <input id="cache" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(6)">
        </div>
    </div>
</div>

</body>
</html>
