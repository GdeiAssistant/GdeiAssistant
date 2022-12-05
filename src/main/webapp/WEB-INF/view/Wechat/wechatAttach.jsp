<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>微信-绑定校园网络账号</title>
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
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script type="text/javascript" src="/js/wechat/attach.js"></script>
</head>
<body>

<div class="hd">
    <h1 class="page_title">绑定教务系统</h1>
    <p class="page_desc">请登录教务系统</p>
</div>

<!-- 提交的用户信息表单 -->
<div class="weui-cells weui-cells_form">
    <form>
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

<!-- 登录中弹框 -->
<div role="alert" id="loadingToast" style="display: none;">
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast">
              <span class="weui-primary-loading__dot"></span>
            </span>
        <p class="weui-toast__content">登录中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui-toptips weui_warn js_tooltips"></div>

<p class="page_desc" style="margin-top: 25px">点击登录按钮表示你已阅读并同意
    <a class="page_desc" onclick="window.location.href = '/agreement'">《用户协议》</a>
</p>

</body>
</html>
