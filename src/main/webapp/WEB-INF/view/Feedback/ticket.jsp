<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>故障工单提交</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
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
    <script>document.write("<script type='text/javascript' src='/js/feedback/ticket.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">故障工单提交</h1>
</div>

<div class="weui-cells__title">选择问题分类</div>
<div class="weui-cells">
    <div class="weui-cell weui-cell_select weui-cell_select-after">
        <div class="weui-cell__hd">
            <label class="weui-label">分类</label>
        </div>
        <div class="weui-cell__bd">
            <select id="type" class="weui-select">
                <option value="闪退、卡顿或界面错位">闪退、卡顿或界面错位</option>
                <option value="用户登录和密码设置">用户登录和密码设置</option>
                <option value="个人资料和隐私设置">个人资料和隐私设置</option>
                <option value="实名认证和手机绑定">实名认证和手机绑定</option>
                <option value="账号绑定、管理和注销">账号绑定、管理和注销</option>
                <option value="应用功能异常">应用功能异常</option>
                <option value="其它">其它</option>
            </select>
        </div>
    </div>
</div>

<div class="weui-cells__title">问题描述</div>
<div class="weui-cells">
    <!-- 具体问题输入框 -->
    <div class="weui-cell">
        <div class="weui-cell__bd">
            <textarea id="content" class="weui-textarea" placeholder="请描述具体问题，不超过250字" rows="3"
                      onkeyup="inputLengthCheck(this,250)"></textarea>
            <div class="weui-textarea-counter"><span id="words">0</span>/250</div>
        </div>
    </div>
    <!-- 图片上传 -->
    <div class="weui-gallery" id="gallery">
        <span class="weui-gallery__img" id="galleryImg" index="0"></span>
        <div class="weui-gallery__opr">
            <a href="javascript:" class="weui-gallery__del" onclick="deleteImage()">
                <i class="weui-icon-delete weui-icon_gallery-delete"></i>
            </a>
        </div>
    </div>
    <div class="weui-cell">
        <div class="weui-cell__bd">
            <div class="weui-uploader">
                <div class="weui-uploader__hd">
                    <p class="weui-uploader__title">图片上传（可选）</p>
                    <div class="weui-uploader__info">0/9</div>
                </div>
                <div class="weui-uploader__bd">
                    <ul class="weui-uploader__files" id="uploaderFiles"></ul>
                    <div class="weui-uploader__input-box">
                        <input id="uploaderInput" class="weui-uploader__input" type="file" accept="image/*">
                    </div>
                </div>
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

<!-- 提交按钮 -->
<div class="weui-btn_area">
    <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postFeedbackForm()">提交</a>
</div>

</body>
</html>
