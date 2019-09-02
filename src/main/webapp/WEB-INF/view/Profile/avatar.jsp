<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>修改头像</title>
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
    <link rel="stylesheet" type="text/css" href="/css/common/cropper.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/cropper.min.js"></script>
    <script type="text/javascript" src="/js/common/exif.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/profile/avatar.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div style="width: 200px;height: 200px;margin: 25px auto">
    <c:choose>
        <c:when test="${AvatarURL!=null and AvatarURL!=''}">
            <img style="width: 100%;height: 100%;border-radius: 50%" src="${AvatarURL}">
        </c:when>
        <c:otherwise>
            <img style="width: 100%;height: 100%;border-radius: 50%" src="/img/avatar/default.png">
        </c:otherwise>
    </c:choose>
</div>

<div class="weui-msg__opr-area">
    <p class="weui-btn-area">
        <a onclick="selectAvatarImage()" href="javascript:"
           class="weui-btn weui-btn_primary">上传新头像</a>
    </p>
    <c:if test="${AvatarHDURL!=null && AvatarHDURL!=''}">
    <p class="weui-btn-area">
        <a onclick="window.location.href='${AvatarHDURL}'" href="javascript:"
           class="weui-btn weui-btn_default">查看高清头像</a>
    </p>
    </c:if>
    <c:if test="${AvatarURL!=null && AvatarURL!=''}">
    <p class="weui-btn-area">
        <a onclick="deleteAvatar()" href="javascript:"
           class="weui-btn weui-btn_default">删除头像</a>
    </p>
    </c:if>

    <!-- 头像文件上传表单 -->
    <form id="uploadForm" enctype="multipart/form-data">
        <input id="avatarFileInput" name="avatar" accept="image/*" type="file" hidden="hidden">
    </form>

    <!-- 头像裁剪参数 -->
    <input id="x" type="hidden">
    <input id="y" type="hidden">
    <input id="width" type="hidden">
    <input id="height" type="hidden">

    <!-- 裁剪头像弹窗 -->
    <div id="drawImageDialog" class="weui-popup__container">
        <div class="weui-popup__overlay"></div>
        <div class="weui-popup__modal">
            <div class="toolbar">
                <div class="toolbar-inner">
                    <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                    <a href="javascript:" style="right:0" class="picker-button close-popup"
                       onclick="drawImageAndUpload()">使用</a>
                    <h1 class="title">上传头像</h1>
                </div>
            </div>
            <div class="modal-content img-container" style="text-align:center">
                <img id="drawImage" style=" width:80% ; height: 60% ;margin-top:1.17647059em"/>
            </div>
        </div>
    </div>

</body>

</html>
