<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
    <script>document.write("<script type='text/javascript' src='/js/delivery/delivery.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <style>
        .weui-cells {
            margin-top: 0;
        }
    </style>
</head>
<body>

<div>
    <div class="weui-cells__title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">在线下单</h1>
    </div>
</div>

<div class="weui-cells__title">个人信息</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">姓名</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
            <input id="name" class="weui-input" type="text" maxlength="10" name="name"
                   placeholder="请输入姓名">
        </div>
    </div>
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">学号</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
            <input id="number" class="weui-input" type="number" maxlength="11"
                   name="number" pattern="[0-9]*" placeholder="请输入学号" onkeyup="inputLengthCheck(this,11)">
        </div>
    </div>
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">手机号</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
            <input id="phone" class="weui-input" type="number" maxlength="11"
                   name="phone" pattern="[0-9]*" placeholder="请输入手机号" onkeyup="inputLengthCheck(this,11)">
        </div>
    </div>
</div>

<div class="weui-cells__title">交易信息</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">报酬</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
            <input id="price" class="weui-input" type="number" name="price" placeholder="不超过99元，数值将四舍五入">
        </div>
    </div>
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">地址</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
            <input id="address" class="weui-input" type="text" maxlength="50" name="address" placeholder="请输入送往地址">
        </div>
    </div>
</div>

<div class="weui-cells__title">快递信息</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">公司</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
            <input id="company" class="weui-input" type="text" maxlength="10" name="company" placeholder="请输入快递公司名称">
        </div>
    </div>
    <div class="weui-cell">
        <div class="weui-cell__hd">
            <label class="weui-label">备注</label>
        </div>
        <div class="weui-cell__bd weui-cell_primary">
         <textarea id="remarks" onkeyup="textAreaInputLengthCheck(this,100)"
                   placeholder="请输入备注" type="text"
                   class="weui-textarea"></textarea>
            <div class="weui-textarea-counter">
                <span id="length">0</span>/100
            </div>
        </div>
    </div>
</div>

<p class="weui-btn-area">
    <a href="javascript:" onclick="submitForm()" class="weui-btn weui-btn_primary">提交</a>
</p>

<br>

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
