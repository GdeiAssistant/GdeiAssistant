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
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/delivery/delivery.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div>
    <div class="weui-cells__title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">个人中心</h1>
    </div>
</div>

<c:if test="${empty OrderList && empty AcceptedOrderList}">
    <div class="weui-msg">
        <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
        <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">没有任何记录</h2>
            <p class="weui-msg__desc">没有找到任何下单和接单记录</p>
        </div>
    </div>
</c:if>

<c:if test="${!empty OrderList}">
    <div class="weui-cells__title">我的下单</div>
    <div class="weui-cells">
        <c:forEach var="DeliveryOrder" items="${OrderList}">
            <a class="weui-cell weui-cell_access" href="/delivery/order/id/${DeliveryOrder.orderId}">
                <div class="weui-cell__bd">
                    <p>订单号：${DeliveryOrder.orderId}</p>
                    <p style="font-size:13px;color:#999">下单时间：
                        <fmt:formatDate value="${DeliveryOrder.orderTime}" pattern="yyyy年MM月dd日 HH:mm:dd"/></p>
                    <p style="font-size:13px;color:#999">订单状态：
                        <c:choose>
                            <c:when test="${DeliveryOrder.state==0}">
                                待接单
                            </c:when>
                            <c:when test="${DeliveryOrder.state==1}">
                                已接单
                            </c:when>
                            <c:when test="${DeliveryOrder.state==2}">
                                已删除
                            </c:when>
                        </c:choose>
                    </p>
                </div>
                <div class="weui-cell__ft"></div>
            </a>
        </c:forEach>
    </div>
</c:if>

<c:if test="${!empty AcceptedOrderList}">
    <div class="weui-cells__title">我的接单</div>
    <div class="weui-cells">
        <c:forEach var="DeliveryOrder" items="${AcceptedOrderList}">
            <a class="weui-cell weui-cell_access" href="/delivery/order/id/${DeliveryOrder.orderId}">
                <div class="weui-cell__bd">
                    <p>订单号：${DeliveryOrder.orderId}</p>
                    <p style="font-size:13px;color:#999">下单时间：
                        <fmt:formatDate value="${DeliveryOrder.orderTime}" pattern="yyyy年MM月dd日 HH:mm:dd"/></p>
                    <p style="font-size:13px;color:#999">订单状态：
                        <c:choose>
                            <c:when test="${DeliveryOrder.state==0}">
                                待接单
                            </c:when>
                            <c:otherwise>
                                已接单
                            </c:otherwise>
                        </c:choose>
                    </p>
                </div>
                <div class="weui-cell__ft"></div>
            </a>
        </c:forEach>
    </div>
</c:if>

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
