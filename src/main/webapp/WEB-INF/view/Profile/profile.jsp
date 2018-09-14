<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/15
  Time: 17:30
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="profile" uri="/WEB-INF/tld/profile.tld" %>
<html>
<head>
    <title>修改个人资料</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link rel="stylesheet" href="/css/common/cropper.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/cropper.min.js"></script>
    <script type="text/javascript" src="/js/common/exif.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <jsp:include page="/js/profile/profile.jsp"/>
    <style>
        img {
            max-width: 100%;
        }
    </style>
</head>

<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">个人资料</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<!-- 头像文件上传表单 -->
<form id="uploadForm" enctype="multipart/form-data">
    <input id="avatarFileInput" name="avatar" accept="image/*" type="file" hidden="hidden">
</form>

<!-- 头像裁剪参数 -->
<input id="x" type="hidden">
<input id="y" type="hidden">
<input id="width" type="hidden">
<input id="height" type="hidden">

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="selectAvatarImage()">
        <div class="weui-cell__bd">
            <p>头像</p>
        </div>
        <div class="weui-cell__ft">
            <img id="avatar" style="border-radius: 50%;width: 50px;height: 50px;"
                 src="/img/avatar/default.png"/>
        </div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showKicknameDialog()">
        <div class="weui-cell__bd">
            <p>昵称</p>
        </div>
        <div id="kickname_text" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>姓名</p>
        </div>
        <div id="realname" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeGender()">
        <div class="weui-cell__bd">
            <p>性别</p>
        </div>
        <div id="gender" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeGenderOrientation()">
        <div class="weui-cell__bd">
            <p>性取向</p>
        </div>
        <div id="genderOrientation" class="weui-cell__ft"></div>
    </div>
</div>

<input type="hidden" id="kickname_val" name="kickname_val">

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="changeRegion()">
        <div class="weui-cell__bd">
            <p>所在地</p>
        </div>
        <div id="location" class="weui-cell__ft"></div>
    </div>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/introduction'">
        <div class="weui-cell__bd">
            <p>个人简介</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/privacy'">
        <div class="weui-cell__bd">
            <p>隐私设置</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<!-- 裁剪头像弹窗 -->
<div id="drawImageDialog" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <a href="javascript:" style="right:0" class="picker-button close-popup" onclick="drawImageAndUpload()">使用</a>
                <h1 class="title">上传头像</h1>
            </div>
        </div>
        <div class="modal-content img-container" style="text-align:center">
            <img id="drawImage" style=" width:80% ; height: 60% ;margin-top:1.17647059em"/>
        </div>
    </div>
</div>

<!-- 修改昵称弹窗 -->
<div id="changeKickname" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">更改昵称</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells__title">昵称将作为你的个人资料公开显示</div>
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">昵称</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="kickname" class="weui-input" type="text" onkeyup="inputLengthCheck(this,24)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="changeKickname()">确认</a>
            </div>
        </div>
    </div>
</div>

<!-- 自定义性别窗口 -->
<div id="customGender" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">自定义性别</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells__title">请输入你的性别</div>
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">性别</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="customGenderName" class="weui-input" type="text" onkeyup="inputLengthCheck(this,50)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="submitCustomGender()">确认</a>
            </div>
        </div>
    </div>
</div>

</body>
</html>