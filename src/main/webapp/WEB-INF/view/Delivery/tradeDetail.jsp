<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>全民快递</title>
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
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/delivery/delivery.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div>
    <div class="weui_cells_title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">交易单信息</h1>
    </div>
</div>

<c:choose>

    <c:when test="${DetailType==0 || DetailType==2}">

        <div class="weui-cells__title">全民快递交易信息</div>
        <div class="weui-cells">
            <div class="weui-cell" href="javascript:">
                <div class="weui-cell__bd">
                    <p>交易号</p>
                </div>
                <div class="weui-cell__ft">${DeliveryTrade.tradeId}</div>
            </div>
            <div class="weui-cell" href="javascript:">
                <div class="weui-cell__bd">
                    <p>订单号</p>
                </div>
                <div class="weui-cell__ft">${DeliveryTrade.orderId}</div>
            </div>
            <div class="weui-cell" href="javascript:">
                <div class="weui-cell__bd">
                    <p>交易时间</p>
                </div>
                <div class="weui-cell__ft">
                    <fmt:formatDate value="${DeliveryTrade.createTime}" pattern="yyyy年MM月dd日 HH:mm:dd"/>
                </div>
            </div>
            <a class="weui-cell weui-cell_access" href="javascript:"
               onclick="window.location.href='/profile/user/${DeliveryTrade.username}'">
                <div class="weui-cell__bd">
                    <p>接单人</p>
                </div>
                <div class="weui-cell__ft">${DeliveryTrade.username}</div>
            </a>
            <div class="weui-cell" href="javascript:">
                <div class="weui-cell__bd">
                    <p>交易状态</p>
                </div>
                <div class="weui-cell__ft">
                    <c:choose>
                        <c:when test="${DeliveryTrade.state==0}">
                            等待交付
                        </c:when>
                        <c:otherwise>
                            已交付
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <c:if test="${DetailType==0 && DeliveryTrade.state==0}">
            <p class="weui-btn-area">
                <a href="javascript:" onclick="finishTrade(${DeliveryTrade.tradeId})" class="weui-btn weui-btn_primary">确认交付</a>
            </p>
        </c:if>

    </c:when>

    <c:otherwise>

        <div class="weui-msg">
            <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
            <div class="weui-msg__text-area">
                <h2 class="weui-msg__title">没有权限查看</h2>
                <p class="weui-msg__desc">你没有权限查看该交易记录信息</p>
            </div>
        </div>

    </c:otherwise>

</c:choose>

<!-- 提交中弹框 -->
<div class="weui_mask" style="display: none"></div>
<div id="loadingToast" class="weui_loading_toast" style="display: none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <div class="weui_loading">
            <div class="weui_loading_leaf weui_loading_leaf_0"></div>
            <div class="weui_loading_leaf weui_loading_leaf_1"></div>
            <div class="weui_loading_leaf weui_loading_leaf_2"></div>
            <div class="weui_loading_leaf weui_loading_leaf_3"></div>
            <div class="weui_loading_leaf weui_loading_leaf_4"></div>
            <div class="weui_loading_leaf weui_loading_leaf_5"></div>
            <div class="weui_loading_leaf weui_loading_leaf_6"></div>
            <div class="weui_loading_leaf weui_loading_leaf_7"></div>
            <div class="weui_loading_leaf weui_loading_leaf_8"></div>
            <div class="weui_loading_leaf weui_loading_leaf_9"></div>
            <div class="weui_loading_leaf weui_loading_leaf_10"></div>
            <div class="weui_loading_leaf weui_loading_leaf_11"></div>
        </div>
        <p class="weui_toast_content">提交中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
