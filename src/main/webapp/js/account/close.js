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
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/api/close/submit',
            method: 'post',
            data: {
                password: $("#password").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    var alert = weui.alert('删除账号成功，即将返回主页', function () {
                        return false;
                    }, {
                        title: '注销成功',
                        buttons: [{
                            label: '确定',
                            type: 'primary',
                            onClick: function () {
                                alert.hide();
                                window.location.href = '/logout';
                            }
                        }]
                    });
                }
                else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function () {
                $("#loadingToast, .weui_mask").hide();
                showNetworkErrorTip();
            }
        })
    }
    else {
        showCustomErrorTip("账号密码长度不合法");
    }
}