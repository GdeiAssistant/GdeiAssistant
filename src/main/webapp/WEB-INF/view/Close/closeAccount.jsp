<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>删除账号</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <!-- 引入CSS样式 -->
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/close/close.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">删除你的账号</h1>
</div>

<div class="weui-msg">
    <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
    <div class="weui-msg__text-area">
        <h2 class="weui-msg__title">确认删除账号？</h2>
        <p class="weui-msg__desc">将会删除你的账号，用户数据将无法恢复</p>
        <p class="weui-msg__desc">注销账号需要满足以下条件：</p>
        <p class="weui-msg__desc">1、二手交易平台所有商品已出售或下架</p>
        <p class="weui-msg__desc">2、失物招领平台所有物品已确认寻回</p>
        <p class="weui-msg__desc">3、恋爱交友平台已收回所有卖室友信息</p>
        <p class="weui-msg__desc">4、近期无账号异常或被封禁的情况</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<!-- 提交的用户信息表单 -->
<div class="weui_cells">
    <div class="weui_cell">
        <div class="weui_cell_hd">
            <label class="weui_label">密码</label>
        </div>
        <div class="weui_cell_bd weui_cell_primary">
            <input id="password" class="weui_input" type="password" maxlength="35" name="password"
                   placeholder="请输入你的教务系统密码">
        </div>
    </div>
</div>

<!-- 注销按钮 -->
<div class="weui_btn_area">
    <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postCloseRequest()">注销</a>
</div>

<!-- 注销中弹框 -->
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
        <p class="weui_toast_content">注销中</p>
    </div>
</div>

</body>

</body>
</html>
