//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//获取邮件验证码
function getVerificationCode() {
    if ($("#email").val() == '') {
        $(".weui_warn").text("电子邮件地址不能为空").show().delay(2000).hide(0);
    } else if (!(/^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/).test($("#email").val())) {
        $(".weui_warn").text("电子邮件地址格式不合法").show().delay(2000).hide(0);
    } else {
        let loading = weui.loading('正在请求验证码');
        $("#verfication_button").attr("disabled", true);
        $.ajax({
            url: '/api/email/verification',
            method: 'POST',
            data: {
                email: $("#email").val()
            },
            success: function (result) {
                loading.hide();
                if (result.success === true) {
                    //设置验证码获取等待间隔
                    $("#verfication_button").attr("disabled", true);
                    $("#verfication_button").css("color", "#bfbfbf");
                    let second = 59;
                    let waittingInterval = setInterval(function () {
                        $("#verfication_button").text("等待" + second + "秒");
                        second = second - 1;
                    }, 1000);
                    let waittingTimeout = setTimeout(function () {
                        clearInterval(waittingInterval);
                        $("#verfication_button").attr("disabled", false);
                        $("#verfication_button").text("获取验证码");
                        $("#verfication_button").css("color", "#3cc51f");
                    }, 60000);
                } else {
                    $("#verfication_button").attr("disabled", false);
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                loading.hide();
                $("#verfication_button").attr("disabled", false);
                if (result.status) {
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        })
    }
}

//提交绑定电子邮件地址请求
function saveEmail() {
    if ($("#email").val() == '') {
        $(".weui_warn").text("电子邮件地址不能为空").show().delay(2000).hide(0);
    } else if (!(/^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\.[a-zA-Z0-9_-]+)+$/).test($("#email").val())) {
        $(".weui_warn").text("电子邮件地址格式不合法").show().delay(2000).hide(0);
    } else if ($("#verification_code").val() == '') {
        $(".weui_warn").text("验证码不能为空").show().delay(2000).hide(0);
    } else if ($("#verification_code").val().length != 6 || !/^\d+$/.test($("#verification_code").val())) {
        $(".weui_warn").text("验证码必须为6位的数字").show().delay(2000).hide(0);
    } else {
        $("#loadingToast").show();
        $.ajax({
            url: '/api/email/bind',
            method: 'POST',
            data: {
                email: $("#email").val(),
                randomCode: $("#verification_code").val()
            },
            success: function (result) {
                $("#loadingToast").hide();
                if (result.success) {
                    weui.alert('你已成功绑定电子邮件地址', {
                        title: '绑定成功',
                        buttons: [{
                            label: '确定',
                            type: 'primary',
                            onClick: function(){
                                history.go(-1);
                            }
                        }]
                    });
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                $("#loadingToast").hide();
                if (result.status) {
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        });
    }
}

//解绑电子邮件地址
function unBindEmail() {
    weui.confirm('你是否确认要解绑电子邮件地址？', {
        title: '解绑电子邮件地址',
        buttons: [{
            label: '取消',
            type: 'default',
            onClick: function(){}
        }, {
            label: '确定',
            type: 'primary',
            onClick: function(){
                $("#loadingToast").show();
                $.ajax({
                    url: '/api/email/unbind',
                    method: 'POST',
                    success: function (result) {
                        $("#loadingToast").hide();
                        if (result.success) {
                            window.location.reload();
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function (result) {
                        $("#loadingToast").hide();
                        if (result.status) {
                            $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                        } else {
                            $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                        }
                    }
                });
            }
        }]
    });
}