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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/close/close.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/account/close.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui-toptips weui_warn js_tooltips"></div>

<div class="close-account-request">
    <div class="weui-msg">
        <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
        <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">确认删除账号？</h2>
            <p class="weui-msg__desc">删除账号后，您在广东二师助手留存的信息将被清空且无法找回，具体包括：</p>
            <p class="weui-msg__desc">1、个人资料、手机号和邮箱地址等身份信息</p>
            <p class="weui-msg__desc">2、添加的自定义课程以及保存的四六级准考证号信息</p>
            <p class="weui-msg__desc">3、社交功能平台的用户数据和交易记录</p>
        </div>
    </div>

    <!-- 提交的用户信息表单 -->
    <div class="weui-cells">
        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">密码</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="password" class="weui-input" type="password" maxlength="35" name="password"
                       placeholder="请输入你的校园网密码">
            </div>
        </div>
    </div>

    <!-- 注销按钮 -->
    <div class="weui-btn_area">
        <a class="weui-btn weui-btn_warn" href="javascript:" onclick="postCloseRequest()">注销</a>
    </div>

    <p class="page_desc" style="margin-top: 15px">注销账号前请认真阅读并同意
        <a class="page_desc"
           onclick="window.location.href = '/policy/deletion'">《注销协议》</a>
    </p>

</div>

<div class="close-account-result">
    <div class="weui-msg">
        <div class="weui-msg__icon-area">
            <i style="display: none" class="weui-icon-warn weui-icon_msg"></i>
            <i style="display: none" class="weui-icon-success weui-icon_msg"></i>
        </div>
        <div class="weui-msg__text-area">
            <h2 class="weui-msg__title"></h2>
            <p class="weui-msg__desc">

            </p>
        </div>
        <div style="display: none" class="weui-msg__custom-area">
            <div class="weui-cells__group weui-cells__group_form">

            </div>
        </div>
        <div class="weui-msg__opr-area">
            <p class="weui-btn-area">
                <a style="display: none" href="javascript:localStorage.clear();window.location.href = '/logout';"
                   role="button"
                   class="weui-btn weui-btn_primary">确定</a>
            </p>
        </div>
        <div style="display: none" class="weui-msg__extra-area">
            <div class="weui-footer">
                <p class="weui-footer__text">如对以上内容有疑问，请联系客服</p>
            </div>
        </div>
    </div>
</div>

<!-- 注销中弹框 -->
<div role="alert" id="loadingToast" style="display: none;">
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast">
              <span class="weui-primary-loading__dot"></span>
            </span>
        <p class="weui-toast__content">注销中</p>
    </div>
</div>

</body>

</html>
