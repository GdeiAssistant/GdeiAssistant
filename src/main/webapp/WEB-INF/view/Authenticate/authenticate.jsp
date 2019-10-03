<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>实名认证</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <!-- 引入CSS样式 -->
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.min.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link title="pink" rel="alternate stylesheet" href="/css/common/jquery-weui.min_pink.css">
    <link title="blue" rel="alternate stylesheet" href="/css/common/jquery-weui.min_blue.css">
    <link rel="stylesheet" href="/css/authentication/authentication.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
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

<!-- Safari浏览器打开提示 -->
<div class="wxtip" id="JweixinTip">
    <span class="wxtip-icon"></span>
    <p class="wxtip-txt">iOS系统暂不支持微信端认证<br>点击右上角<br>选择在Safari浏览器中打开</p>
</div>

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
</div>

<div class="weui-cells__title">快捷认证</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="authenticateBySystem()">
        <div class="weui-cell__bd">
            <p>与教务系统个人信息同步</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="authenticateWithPhone()">
        <div class="weui-cell__bd">
            <p>手机号快捷实名认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">大陆居民认证</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="authenticateWithMainlandIDCard()">
        <div class="weui-cell__bd">
            <p>上传居民身份证照片认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="authenticateWithMainlandDocuments()">
        <div class="weui-cell__bd">
            <p>其他大陆证件类型认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">港澳居民认证</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="authenticateWithHongKongAndMacaoIDCard()">
        <div class="weui-cell__bd">
            <p>香港澳门身份证认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="authenticateWithHongKongAndMacaoExitAndEntryPermit()">
        <div class="weui-cell__bd">
            <p>港澳居民来往内地通行证认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">台湾居民认证</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="authenticateWithTaiwanExitAndEntryPermit()">
        <div class="weui-cell__bd">
            <p>台湾居民来往大陆通行证认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">海外居民认证</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="authenticateWithPassport()">
        <div class="weui-cell__bd">
            <p>护照认证</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div id="removeAuthentication" style="display: none">

    <div class="weui-cells__title">注销实名</div>

    <div class="weui-cells">
        <a class="weui-cell weui-cell_access" href="javascript:" onclick="removeAuthenticationData()">
            <div class="weui-cell__bd">
                <p>注销实名认证信息</p>
            </div>
            <div class="weui-cell__ft"></div>
        </a>
    </div>

</div>

<br>

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
            <video id="camera" width="85%" autoplay muted></video>
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