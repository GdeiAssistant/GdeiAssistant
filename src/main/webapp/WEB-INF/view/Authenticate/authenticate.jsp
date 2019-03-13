<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>实名认证</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <!-- 引入CSS样式 -->
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <jsp:include page="/js/authenticate/authenticate.jsp"/>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">实名认证</h1>
    <p class="page_desc">若你需要更改实名认证信息<br>可以选择任意一种认证方式重新认证</p>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

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
        <p class="weui_toast_content">请稍候</p>
    </div>
</div>

<div class="weui-cells__title">认证信息</div>
<div class="weui-cells">
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>认证状态</p>
        </div>
        <div id="authenticateState" class="weui-cell__ft">

        </div>
    </div>
    <div id="nameCell" class="weui-cell" style="display: none;" href="javascript:">
        <div class="weui-cell__bd">
            <p id="authenticationName">姓名</p>
        </div>
        <div id="name" class="weui-cell__ft">

        </div>
    </div>
</div>

<div class="weui-cells__title">认证方式</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="authenticateWithSystem()">
        <div class="weui-cell__bd">
            <p>与教务系统个人信息同步</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="authenticateWithYiBan()">
        <div class="weui-cell__bd">
            <p>与易班校方认证信息同步</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="showCameraTip()">
        <div class="weui-cell__bd">
            <p>上传身份证照片认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div id="cameraPopup" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal" style="text-align:center">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">上传身份证照片认证</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="hd">
                <h1 class="page_title">拍照并上传</h1>
                <p class="page_desc">请拍摄并上传居民身份证个人信息面图片</p>
            </div>
            <div class="weui-msg" style="padding-top:10px">
                <div class="weui-msg__text-area">
                    <p class="weui-msg__desc">注意事项：</p>
                    <p class="weui-msg__desc">1、证件的信息需完整且清晰可辨，无反光、遮挡、水印、证件套、LOGO等，不缺边角</p>
                    <p class="weui-msg__desc">2、证件必须真实拍摄，不能使用复印件，不能对镜</p>
                    <p class="weui-msg__desc">3、仅支持中华人民共和国第二代居民身份证，不支持临时身份证和其他类型证件</p>
                </div>
            </div>
            <video id="camera" width="80%" autoplay muted></video>
            <!-- 认证中弹框 -->
            <div class="weui_mask" style="display: none"></div>
            <div id="popupLoadingToast" class="weui_loading_toast" style="display: none">
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
                    <p class="weui_toast_content">认证中</p>
                </div>
            </div>
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="capturePhoto()">拍照</a>
            </div>
        </div>
    </div>
</div>

</body>