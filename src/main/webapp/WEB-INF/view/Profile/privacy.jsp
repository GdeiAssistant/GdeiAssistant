<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>隐私设置</title>
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
    <script>document.write("<script type='text/javascript' src='/js/profile/privacy.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
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
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的院系</div>
        <div class="weui-cell__ft">
            <input id="faculty" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的专业</div>
        <div class="weui-cell__ft">
            <input id="major" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的国家/地区</div>
        <div class="weui-cell__ft">
            <input id="location" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的家乡</div>
        <div class="weui-cell__ft">
            <input id="hometown" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的个人简介</div>
        <div class="weui-cell__ft">
            <input id="introduction" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的入学年份</div>
        <div class="weui-cell__ft">
            <input id="enrollerment" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的年龄</div>
        <div class="weui-cell__ft">
            <input id="age" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的学历</div>
        <div class="weui-cell__ft">
            <input id="degree" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的职业</div>
        <div class="weui-cell__ft">
            <input id="profession" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的大专院校</div>
        <div class="weui-cell__ft">
            <input id="colleges" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的高中/职中</div>
        <div class="weui-cell__ft">
            <input id="primary_school" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的初中</div>
        <div class="weui-cell__ft">
            <input id="junior_high_school" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">公开我的小学</div>
        <div class="weui-cell__ft">
            <input id="high_school" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting()">
        </div>
    </div>
</div>

<div class="weui-cells__title">数据缓存</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">缓存我的教务数据</div>
        <div class="weui-cell__ft">
            <input id="cache" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(13)">
        </div>
    </div>
</div>


<div class="weui-cells__title">搜索引擎</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">让搜索引擎链接到我的个人资料页</div>
        <div class="weui-cell__ft">
            <input id="robots" class="weui-switch" type="checkbox"
                   onchange="changePrivacySetting(14)">
        </div>
    </div>
</div>

</body>
</html>
