<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>保存四六级准考证号</title>
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
    <script>document.write("<script type='text/javascript' src='/js/cet/cetSave.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">四六级准考证号</h1>
</div>

<!-- 错误提示 -->
<c:if test="${ErrorMessage!=null}">
    <script>
        $(".weui_warn").text("${ErrorMessage}").show().delay(2000).hide(0);
    </script>
</c:if>

<!-- 用户的准考证号 -->
<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="">
        <div class="weui-cell__bd">
            <p>我的准考证号</p>
        </div>
        <div id="cetNumberValue" class="weui-cell__ft">
            <c:choose>
                <c:when test="${CetNumber!=null}">
                    ${CetNumber}
                </c:when>
                <c:otherwise>
                    没有考号
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<!-- 提交按钮 -->
<div class="weui-btn_area">
    <a class="weui-btn weui-btn_primary" href="javascript:"
       onclick="$('#changeCetNumber').popup()">修改</a>
</div>

<!-- 修改准考证号弹窗 -->
<div id="changeCetNumber" class="weui-popup__container" style="display: none">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">请输入准考证号</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells weui-cells_form">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">
                            准考证号
                        </label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="cetNumberInput" class="weui-input" maxlength="15"
                               placeholder="请输入15位准考证号" type="number" value="${CetNumber}">
                    </div>
                </div>
            </div>
            <div class="weui-btn_area">
                <a class="weui-btn weui-btn_primary close-popup" href="javascript:"
                   onclick="saveCetNumber()">保存</a>
            </div>
        </div>
    </div>
</div>

<!-- 提交中弹框 -->
<div role="alert" id="loadingToast" style="display: none;">
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast">
              <span class="weui-primary-loading__dot"></span>
            </span>
        <p class="weui-toast__content">提交中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui-toptips weui_warn js_tooltips"></div>

</body>
</html>
