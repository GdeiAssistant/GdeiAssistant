<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
