//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//显示网络错误提示
function showNetworkErrorTip() {
    $(".weui_warn").text('网络连接失败,请检查网络连接').show().delay(2000).hide(0);
}

//显示服务端错误提示
function showCustomErrorTip(message) {
    $(".weui_warn").text(message).show().delay(2000).hide(0);
}

//提交删除账号请求
function postCloseRequest() {
    if ($("#password").val().length > 0 && $("#password").val().length <= 35) {
        weui.confirm('您将删除您的广东二师助手账号，账号后您的账号相关信息将被清空且无法恢复。删除账号前，请确保您的账号的所有社交功能平台的信息已处理完成无纠纷，同时账号未处于被限制或封禁的状态。点击确认删除则代表您已阅读并同意《用户协议》中注销账号相关的内容。', {
            title: '删除账号',
            buttons: [{
                label: '取消删除',
                type: 'primary',
                onClick: function () {

                }
            }, {
                label: '确认删除',
                type: 'default',
                onClick: function () {
                    $("#loadingToast").show();
                    $.ajax({
                        url: '/api/close/submit',
                        method: 'post',
                        data: {
                            password: $("#password").val()
                        },
                        success: function (result) {
                            $("#loadingToast").hide();
                            $(".close-account-request").hide();
                            //显示删除账号结果
                            if (result.success) {
                                //显示删除账号成功提示信息
                                $(".weui-icon-success").show();
                                $(".weui-icon-warn").hide();
                                $(".weui-msg__title").text("删除账号成功");
                                $(".weui-msg__desc").text("后续若仍需要使用广东二师助手服务，可以重新进行账号注册");
                                //显示按钮
                                $(".weui-btn_primary").show();
                            } else {
                                //显示删除账号失败提示信息
                                $(".weui-icon-warn").show();
                                $(".weui-icon-success").hide();
                                if (result.data == null) {
                                    //通用类型错误
                                    $(".weui-msg__title").text("删除账号失败");
                                    $(".weui-msg__desc").text(result.message);
                                } else {
                                    //删除条件不符合
                                    $(".weui-msg__title").text("删除账号失败");
                                    $(".weui-msg__desc").text("抱歉，因为以下原因，账号无法注销");
                                    let map = result.data;
                                    map.forEach(function (key, value) {
                                        $(".weui-cells__group").append("<div class='weui-cells'><a class='weui-cell' href='javascript:'><span class='weui-cell__bd'>" +
                                            "<span>" + key + "</span><div class='weui-cell__desc'>" + value + "</div></span></a></div>")
                                    })
                                    $(".weui-msg__custom-area").show();
                                }
                            }
                            $(".weui-msg__extra-area").show();
                            $(".close-account-result").show();
                        },
                        error: function () {
                            $("#loadingToast").hide();
                            showNetworkErrorTip();
                        }
                    });
                }
            }]
        });
    } else {
        showCustomErrorTip("账号密码长度不合法");
    }
}