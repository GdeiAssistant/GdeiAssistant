<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>实名认证</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <!-- 引入CSS样式 -->
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        $(function () {
            FastClick.attach(document.body);
            loadAuthenticationData();
        });

        //加载实名认证信息
        function loadAuthenticationData() {
            $.ajax({
                url: '/api/authentication',
                type: 'GET',
                success: function (result) {
                    if (result.success) {
                        if (result.data) {
                            $("#name").text(result.data.realname);
                            $("#authenticateState").text("已认证");
                            $("#nameCell").show();
                        } else {
                            $("#name").text("");
                            $("#authenticateState").text("未认证");
                            $("#nameCell").hide();
                            //弹出提示进行实名认证的弹窗
                            $.alert({
                                text: '根据《中华人民共和国网络安全法》第二十四条的规定要求，为保障你使用广东二师助手不受限制，' +
                                    '请尽快完成实名认证',
                                title: '未进行实名认证'
                            });
                        }
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function (result) {
                    if (result.status) {
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            })
        }

        //与易班校方认证信息进行同步
        function authenticateWithYiBan() {
            <c:choose>
            <c:when test="${sessionScope.yiBanUserID!=null}">
            //易班登录
            $.confirm({
                text: '我们将依照《隐私政策》保护你的个人信息，若你点击确定按钮，将视为你已阅读并同意《隐私政策》',
                title: '授权广东二师助手获取你的易班用户信息',
                onOK: function () {
                    $("#loadingToast, .weui_mask").show();
                    $.ajax({
                        url: '/api/authentication',
                        type: 'POST',
                        data: {
                            method: 2
                        },
                        success: function (result) {
                            $("#loadingToast, .weui_mask").hide();
                            if (result.success) {
                                $.alert({
                                    text: '已完成实名认证，你现在可以使用广东二师助手所有功能了',
                                    title: '实名认证成功',
                                    onOK: function () {
                                        loadAuthenticationData();
                                    }
                                });
                            } else {
                                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                            }
                        },
                        error: function (result) {
                            $("#loadingToast, .weui_mask").hide();
                            if (result.status) {
                                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                            } else {
                                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                            }
                        }
                    });
                }
            });
            </c:when>
            <c:otherwise>
            $.alert({
                text: '请使用易班客户端登录并完成认证',
                title: '错误提示'
            });
            </c:otherwise>
            </c:choose>
        }

        //与教务系统身份认证信息进行同步
        function authenticateWithSystem() {
            $.confirm({
                text: '我们将依照《隐私政策》保护你的个人信息，若你点击确定按钮，将视为你已阅读并同意《隐私政策》',
                title: '授权广东二师助手登录你的教务系统并获取你的个人信息',
                onOK: function () {
                    $("#loadingToast, .weui_mask").show();
                    $.ajax({
                        url: '/api/authentication',
                        type: 'POST',
                        data: {
                            method: 0
                        },
                        success: function (result) {
                            $("#loadingToast, .weui_mask").hide();
                            if (result.success) {
                                $.alert({
                                    text: '已完成实名认证，你现在可以使用广东二师助手所有功能了',
                                    title: '实名认证成功',
                                    onOK: function () {
                                        loadAuthenticationData();
                                    }
                                });
                            } else {
                                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                            }
                        },
                        error: function (result) {
                            $("#loadingToast, .weui_mask").hide();
                            if (result.status) {
                                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                            } else {
                                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                            }
                        }
                    });
                }
            });
        }

        //弹出调用摄像头拍照的提示
        function showCameraTip() {
            $.alert({
                text: '请使用摄像头拍摄身份证个人信息页，并上传供广东二师助手进行实名认证',
                title: '申请访问摄像头权限',
                onOK: function () {
                    getVideoMedia();
                }
            });
        }

        //申请摄像头权限并获取视频流
        function getVideoMedia(reload) {
            if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
                navigator.mediaDevices.getUserMedia({video: {facingMode: "environment"}}).then(function (stream) {
                    $("#camera")[0].srcObject = stream;
                    if (!reload) {
                        $("#cameraPopup").popup();
                    }
                }, function () {
                    $.toptip('用户拒绝了摄像头权限或你的浏览器不支持相关API', 'error');
                });
                return true;
            }
            return false;
        }

        //用户点击拍照按钮，从视频流中截取图片
        function capturePhoto() {
            //从视频流截取图片
            let canvas = document.createElement('canvas');
            let context = canvas.getContext('2d');
            context.canvas.width = $("#camera").width();
            context.canvas.height = $("#camera").height();
            if (!$("#camera")[0].paused) {
                $("#camera")[0].pause();
            }
            context.drawImage($("#camera")[0], 0, 0, $("#camera").width(), $("#camera").height());
            let base64 = canvas.toDataURL('image/jpeg', 0.8);
            authenticateWithIdentityImage(base64);
        }

        //上传身份证照片进行认证
        function authenticateWithIdentityImage(base64) {
            base64 = base64.split(',')[1];
            base64 = window.atob(base64);
            let ia = new Uint8Array(base64.length);
            for (let i = 0; i < base64.length; i++) {
                ia[i] = base64.charCodeAt(i);
            }
            let blob = new Blob([ia], {type: "image/jpg"});
            let formData = new FormData();
            formData.append('image', blob);
            formData.append('method', 1);
            $("#popupLoadingToast, .weui_mask").show();
            $(".weui_btn").attr("disabled", true);
            //上传身份证图片到服务器
            $.ajax({
                url: "/api/authentication",
                type: "post",
                data: formData,
                processData: false,
                contentType: false,
                success: function (result) {
                    $("#popupLoadingToast, .weui_mask").hide();
                    if (result.success) {
                        $.closePopup();
                        $.toptip('实名认证成功', 'success');
                        loadAuthenticationData();
                    } else {
                        $.toptip(result.message, 'error');
                        $(".weui_btn").attr("disabled", false);
                        getVideoMedia(true);
                    }
                },
                error: function () {
                    $("#popupLoadingToast, .weui_mask").hide();
                    $(".weui_btn").attr("disabled", false);
                    if (result.status) {
                        $.toptip(result.responseJSON.message, 'error');
                    } else {
                        $.toptip('网络连接超时，请重试', 'error');
                    }
                    getVideoMedia(true);
                }
            });
        }

    </script>
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