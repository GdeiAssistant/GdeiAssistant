<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>我的校园卡</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/card/cardInfo.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">我的校园卡</h1>
</div>

<div class="weui-cells__title">校园卡基本信息</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>姓名</p>
        </div>
        <div id="name" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>学号</p>
        </div>
        <div id="number" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡号</p>
        </div>
        <div id="cardNumber" class="weui-cell__ft"></div>
    </div>
</div>

<div class="weui-cells__title">校园卡余额</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡余额</p>
        </div>
        <div id="cardBalance" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡过渡余额</p>
        </div>
        <div id="cardInterimBalance" class="weui-cell__ft"></div>
    </div>
</div>

<div class="weui-cells__title">校园卡状态</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡挂失状态</p>
        </div>
        <div id="cardLostState" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>校园卡冻结状态</p>
        </div>
        <div id="cardFreezeState" class="weui-cell__ft"></div>
    </div>
</div>

<p class="page_desc" style="margin-top: 25px;margin-bottom: 25px">校园卡遗失？点击
    <a class="page_desc" onclick="window.location.href='/cardlost'">校园卡挂失</a>
</p>

<!-- 加载中弹框 -->
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
        <p class="weui_toast_content">加载数据中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
