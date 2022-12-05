<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>绑定邮箱</title>
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
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/email/email.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">修改绑定电子邮件</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui-toptips weui_warn js_tooltips"></div>

<div class="weui-cells">
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label" style="width: 3rem">地址</label>
        </div>
        <div class="weui-cell__bd">
            <input id="email" class="weui-input" type="email" placeholder="请输入电子邮件地址">
        </div>
    </div>
    <div class="weui-cell weui-cell_vcode">
        <div class="weui-cell__hd">
            <label class="weui-label" style="width: 3rem">验证码</label>
        </div>
        <div class="weui-cell__bd">
            <input id="verification_code" class="weui-input" type="number" maxlength="6" placeholder="请输入验证码"/>
        </div>
        <div class="weui-cell__ft">
            <button id="verfication_button" class="weui-btn weui-btn_default weui-vcode-btn"
                    onclick="getVerificationCode()">获取验证码
            </button>
        </div>
    </div>
</div>

<div class="weui-btn_area">
    <a class="weui-btn weui-btn_primary close-popup" href="javascript:"
       onclick="saveEmail()">保存</a>
</div>

</body>
</html>