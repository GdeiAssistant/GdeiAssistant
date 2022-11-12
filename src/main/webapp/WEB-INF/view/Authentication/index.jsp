<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
  <title>实名认证</title>
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
  <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
  <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
  <script type="application/javascript" src="/js/common/fastclick.js"></script>
  <script type="application/javascript" src="/js/common/themeLoader.js"></script>
  <script>document.write("<script type='text/javascript' src='/js/authentication/authentication.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
  <h1 class="page_title">实名认证</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<div class="weui-cells">
  <div class="weui-cell" href="javascript:" onclick="">
    <div class="weui-cell__bd">
      <p>认证状态</p>
    </div>
    <div class="weui-cell__ft">
      <c:choose>
        <c:when test="${Authentication!=null}">
          已验证
        </c:when>
        <c:otherwise>
          未验证
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<!-- 选项按钮 -->
<c:choose>
  <c:when test="${Authentication==null}">
    <div class="weui_btn_area">
      <a class="weui_btn weui_btn_primary" href="javascript:"
         onclick="window.location.href='/authentication/update'">进行认证</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="weui_btn_area">
      <a class="weui_btn weui-btn_warn" href="javascript:"
         onclick="deleteAuthentication()">取消认证</a>
    </div>
  </c:otherwise>
</c:choose>

</body>
</html>
