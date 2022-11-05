//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

let attributionPickerItems = [];

let attributionMap = new Map();

$(function () {
    loadPhoneAttributionMap();
});

//加载国际手机代号表
function loadPhoneAttributionMap() {
    $.ajax({
        url: '/rest/phone/attribution',
        type: 'get',
        success: function (result) {
            if (result.success === true) {
                result.data.forEach(function (item) {
                    attributionMap.set(item.code, item);
                    attributionPickerItems.push({
                        label: item.flag + " " + item.name + " +" + item.code,
                        value: item.code
                    });
                });
            } else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
            }
        },
        error: function (result) {
            if (result.status) {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text('网络连接失败,请检查网络连接').show().delay(2000).hide(0);
            }
        }
    });
}

//修改国际手机区号
function changePhoneCode() {
    weui.picker(attributionPickerItems,
        {
            defaultValue: ['86'],
            container: 'body',
            onConfirm: function (result) {
                //设置国际区号和国家/地区旗帜
                $("#phone_code").val(result[0].value);
                $("#phone_flag").text(attributionMap.get(result[0].value).flag + " +" + result[0].value);
            }
        });
}

//获取手机验证码
function getVerificationCode() {
    if ($("#phone").val() == '') {
        $(".weui_warn").text("手机号不能为空").show().delay(2000).hide(0);
    } else if ($("#phone").val().length < 7 || $("#phone").val().length > 11) {
        $(".weui_warn").text("手机号格式不合法").show().delay(2000).hide(0);
    } else {
        let loading = weui.loading('正在请求验证码');
        $("#verfication_button").attr("disabled", true);
        $.ajax({
            url: '/api/phone/verification',
            method: 'POST',
            data: {
                code: $("#phone_code").val(),
                phone: $("#phone").val()
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

//提交绑定请求
function savePhoneNumber() {
    if ($("#phone").val() == '') {
        $(".weui_warn").text("手机号不能为空").show().delay(2000).hide(0);
    } else if ($("#phone").val().length < 7 || $("#phone").val().length > 11) {
        $(".weui_warn").text("手机号格式不合法").show().delay(2000).hide(0);
    } else if ($("#verification_code").val() == '') {
        $(".weui_warn").text("验证码不能为空").show().delay(2000).hide(0);
    } else if ($("#verification_code").val().length != 6 || !/^\d+$/.test($("#verification_code").val())) {
        $(".weui_warn").text("验证码必须为6位的数字").show().delay(2000).hide(0);
    } else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/api/phone/attach',
            method: 'POST',
            data: {
                code: $("#phone_code").val(),
                phone: $("#phone").val(),
                randomCode: $("#verification_code").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    $.alert({
                        text: '你已成功绑定手机号',
                        title: '绑定成功',
                        onOK: function () {
                            history.go(-1);
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
                    $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        });
    }
}

//解绑手机号
function unattachPhoneNumber() {
    $.confirm({
        title: '解绑手机号',
        text: '解绑手机号不会同步清除你的实名认证信息，且解绑后此手机号将不能接收到系统的通知信息，你是否确认要解绑？',
        onOK: function () {
            $("#loadingToast, .weui_mask").show();
            $.ajax({
                url: '/api/phone/unattach',
                method: 'POST',
                success: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.success) {
                        window.location.reload();
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.status) {
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            });
        }
    });
}