//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//检测Input文字长度是否超过限制
function inputLengthCheck(str, maxLen) {
    var w = 0;
    for (var i = 0; i < str.value.length; i++) {
        var c = str.value.charCodeAt(i);
        if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
            w++;
        }
        else {
            w += 2;
        }
        if (w > maxLen) {
            str.value = str.value.substr(0, i);
            break;
        }
    }
}

//提交挂失请求
function postCardLostRequest() {
    var password = $("#password").val();
    if (password.length === 0 || password.length > 6) {
        $(".weui_warn").text("请输入6位的校园卡查询密码").show().delay(2000).hide(0);
    }
    else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/setcardlost',
            type: 'post',
            data: {
                cardPassword: password
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    weui.alert('请尽快前往办卡处进行校园卡补办', {
                        title: '挂失校园卡成功',
                        buttons: [{
                            label: '好',
                            type: 'primary'
                        }]
                    });
                }
                else {
                    $(".weui_warn").text(result.errorMessage).show().delay(2000).hide(0);
                }
            },
            error: function () {
                $("#loadingToast, .weui_mask").hide();
                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
            }
        })
    }
}