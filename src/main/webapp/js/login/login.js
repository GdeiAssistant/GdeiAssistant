﻿//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//提交表单数据登录
function postLoginForm() {
    if ($("#username").val() === "" || $("#password").val() === "") {
        weui.topTips('请将信息填写完整');
    } else {
        //弹出用户协议和隐私政策提示
        weui.confirm('在您使用广东二师助手前，请您认真阅读并了解《用户协议》和《隐私政策》。如您是未满18周岁的未成年人，你还需通知您的监护人共同阅读用户协议和隐私政策，点击"确定"即表示您已阅读并同意全部条款。若您不同意，请点击"取消"并退出应用。若您未注册广东二师助手账号，点击登录时系统将自动为您创建一个广东二师助手账号。', {
            title: '用户协议和隐私政策提示',
            buttons: [{
                label: '取消',
                type: 'default',
                onClick: function () {
                }
            }, {
                label: '确定',
                type: 'primary',
                onClick: function () {
                    let loading = weui.loading('登录中');
                    $.ajax({
                        url: '/api/userlogin',
                        method: 'POST',
                        data: {
                            username: $("#username").val(),
                            password: $("#password").val()
                        },
                        success: function (result) {
                            loading.hide();
                            if (result.success) {
                                if ($("#redirect").val() != '') {
                                    window.location.href = $("#redirect").val();
                                } else {
                                    window.location.href = '/index';
                                }
                            } else {
                                weui.topTips(result.message);
                            }
                        },
                        error: function (result) {
                            loading.hide();
                            if (result.status) {
                                weui.topTips(result.responseJSON.message);
                            } else {
                                weui.topTips('网络访问异常，请检查网络连接');
                            }
                        }
                    });
                }
            }]
        });
    }
}
