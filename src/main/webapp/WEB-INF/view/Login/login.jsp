<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>广东二师助手—广东第二师范学院必备校园服务应用</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="keywords" content="校园,四六级,查分,课程表,二师助手,广二师助手,广东二师助手,广东第二师范学院">
    <meta name="description"
          content="广东二师助手是为广东第二师范学院专属打造的校园服务应用，不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、话题等社区交流平台。为广东第二师范学院的校友们带来便携的教务、社交服务，给学生们提供最快最便捷获取校园生活、社团、信息的方式。四年时光，广东二师助手陪你一起走过。">
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link rel="stylesheet" type="text/css" href="/css/login/login.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/login/login.js?time=" + Date.now() + "'><\/script>");</script>
</head>

<body>

<div class="hd">
    <h1 class="page_title">广东二师助手</h1>
    <p class="page_desc">请登录校园网系统</p>
</div>

<!-- 提交的用户信息表单 -->
<div class="weui-cells weui-cells_form">
    <form>
        <input type="hidden" id="redirect" name="redirect" value="${RedirectURL}">
        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">账号</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="username" class="weui-input" type="text" maxlength="20" name="username"
                       placeholder="请输入你的校园网账号">
            </div>
        </div>
        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">密码</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="password" class="weui-input" type="password" maxlength="35" name="password"
                       placeholder="请输入你的校园网密码">
            </div>
        </div>
    </form>
</div>

<!-- 登录按钮 -->
<div class="weui-btn_area">
    <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postLoginForm()">登录</a>
</div>

<!-- 提示信息 -->
<p class="page_desc" style="margin-top: 25px">关于登录账户请阅读
    <a class="page_desc"
       onclick="window.location.href = '/about/account'">《校园网络账号说明》
    </a>
    <br>
    使用前请仔细阅读
    <a onclick="window.location.href = '/agreement'">《用户协议》</a>和<a
            onclick="window.location.href = '/policy/privacy'">《隐私政策》</a>
</p>

<!-- 快速登录 -->
<div class="quick-login">
    <div class="quick-login-text">
        <p>——&nbsp;&nbsp;&nbsp;其他方式登录&nbsp;&nbsp;&nbsp;——</p>
    </div>
    <div class="quick-login-button">
        <img src="/img/login/qq.png" width="30" height="30"/>
        <img src="/img/login/wechat.png" width="30" height="30"/>
        <img src="/img/login/weibo.png" width="30" height="30"/>
        <img src="/img/login/apple.png" width="30" height="30"/>
    </div>
</div>

<p class="page_desc"></p>

</body>

</html>
