<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>删除账号</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <!-- 引入CSS样式 -->
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/account/close.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
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
        <p class="weui-msg__desc">3、全民快递平台无待接单的订单和未交付的交易</p>
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
                   placeholder="请输入你的校园网密码">
        </div>
    </div>
</div>

<!-- 注销按钮 -->
<div class="weui_btn_area">
    <a class="weui_btn weui_btn_warn" href="javascript:" onclick="postCloseRequest()">注销</a>
</div>

<p class="page_desc" style="margin-top: 15px">无法自助注销？点击
    <a class="page_desc"
       onclick="window.location.href = 'mailto:support@gdeiassistant.cn?subject=注销账号&body=请尽可能补全此模板邮件中的空缺信息，并通过附件上传身份证/学生证/教师工作证/教务系统或学信网个人信息界面截图（截图需包含显示操作系统当前时间，' +
               '若截图无操作系统当前信息或显示时间与发送邮件时间相差超过7天，我们将视其为无效的证明信息）等其中一项或多项可以证明账号所属权的扫描件或照片，一并发送到support@gdeiassistant.cn。如你遗忘了部分资料信息，可以留空遗忘项。' +
               '提供尽可能多的账号资料，可以提高你通过人工审核注销的成功率。若我们认为你提供的资料无法证明你为当前账号的所有者，我们会驳回你的人工审核注销申请。' +
               '你提供的信息将被严格保密，并在完成账号注销审核后的48小时内进行删除。%0d%0a用户名：${sessionScope.username}%0d%0a注销原因：%0d%0a注册时间：（具体到月份，如2016年6月）%0d%0a注册地点：（具体到城市，如广东省广州市）%0d%0a常用设备型号：（如填写iPhoneXR/魅蓝E3）'">联系客服进行注销
    </a>
</p>

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

</html>
