<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>修改密码</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <!-- 引入CSS样式 -->
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link title="pink" rel="alternate stylesheet" href="/css/common/jquery-weui.min_pink.css">
    <link title="blue" rel="alternate stylesheet" href="/css/common/jquery-weui.min_blue.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script type="text/javascript" src="/js/account/password.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">修改密码</h1>
</div>

<!-- 提交的用户信息表单 -->
<div class="weui_cells">
    <div class="weui_cell">
        <div class="weui_cell_hd">
            <label class="weui_label">旧密码</label>
        </div>
        <div class="weui_cell_bd weui_cell_primary">
            <input id="old_password" class="weui_input" type="password" maxlength="35" name="old_password"
                   placeholder="请输入你的旧密码">
        </div>
    </div>
    <div class="weui_cell">
        <div class="weui_cell_hd">
            <label class="weui_label">新密码</label>
        </div>
        <div class="weui_cell_bd weui_cell_primary">
            <input id="new_password" class="weui_input" type="password" maxlength="35" name="new_password"
                   placeholder="请输入你的新密码">
        </div>
    </div>
</div>

<!-- 提交按钮 -->
<div class="weui_btn_area">
    <a class="weui_btn weui_btn_primary" href="javascript:" onclick="changePassword()">修改密码</a>
</div>

<p class="page_desc" style="margin-top: 25px">仅支持毕业用户账号在应用中修改账号密码
    <br>
    学生用户和教师用户请登录学院统一认证平台修改
    <br>
    关于登录账号请阅读
    <a class="page_desc"
       onclick="window.location.href = '/about/account'">《教务系统账号说明》
    </a>
</p>

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
        <p class="weui_toast_content">请稍候</p>
    </div>
</div>

</body>

</html>