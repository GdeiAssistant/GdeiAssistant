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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/phone/phone.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">绑定手机</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui-toptips weui_warn js_tooltips"></div>

<!-- 解绑中弹框 -->
<div role="alert" id="loadingToast" style="display: none;">
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast">
              <span class="weui-primary-loading__dot"></span>
            </span>
        <p class="weui-toast__content">解绑中</p>
    </div>
</div>

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="">
        <div class="weui-cell__bd">
            <p>手机号</p>
        </div>
        <div class="weui-cell__ft">
            <c:choose>
                <c:when test="${Phone!=null}">
                    +${Phone.code}-${Phone.phone}
                </c:when>
                <c:otherwise>
                    未绑定
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<!-- 提交按钮 -->
<div class="weui-btn_area">
    <a class="weui-btn weui-btn_primary" href="javascript:"
       onclick="window.location.href='/phone/change'">修改</a>
</div>

<c:if test="${Phone!=null}">
    <!-- 解绑按钮 -->
    <div class="weui-btn_area">
        <a class="weui-btn weui-btn_default" href="javascript:"
           onclick="unattachPhoneNumber()">解绑</a>
    </div>
</c:if>

</body>
</html>
