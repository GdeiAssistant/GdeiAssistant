<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>绑定手机</title>
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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/phone/phone.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">修改绑定手机</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<div class="weui-cells">
    <div class="weui-cell weui-cell_access weui-cell_select-before">
        <div class="weui-cell__hd">
            <input id="phone_code" type="hidden" value="86">
            <label id="phone_flag" class="weui-label" style="width: 4rem"
                   onclick="changePhoneCode()">🇨🇳 +86</label>
        </div>
        <div class="weui-cell__bd">
            <input id="phone" class="weui-input" type="number" pattern="[0-9]*" placeholder="请输入手机号">
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

<p class="page_desc" style="margin-top: 25px">运营商可能会收取短信或通话费用<br>为了保证你的账号安全，请仅绑定本人使用的手机号</p>

<div class="weui_btn_area">
    <a class="weui_btn weui_btn_primary close-popup" href="javascript:"
       onclick="savePhoneNumber()">保存</a>
</div>

</body>
</html>