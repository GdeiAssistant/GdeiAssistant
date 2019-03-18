//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//发送登录请求
function postLoginForm() {
    if ($("#username").val() === "" || $("#password").val() === "") {
        $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
    } else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/wechat/userattach',
            method: 'post',
            data: {
                username: $("#username").val(),
                password: $("#password").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    weui.alert('绑定教务系统账号成功', {
                        title: '绑定成功',
                        buttons: [{
                            label: '进入功能主页',
                            type: 'primary',
                            onClick: function () {
                                window.location.href = '/index';
                            }
                        }]
                    });
                } else {
                    $("#loadingToast, .weui_mask").hide();
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function () {
                $(".weui_warn").text("网络连接失败，请检查网络连接！").show().delay(2000).hide(0);
            }
        })
    }
}