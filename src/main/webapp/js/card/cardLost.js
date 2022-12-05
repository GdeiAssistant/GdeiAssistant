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
        } else {
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
        weui.topTips('请输入6位的校园卡查询密码');
    } else {
        let loading = weui.loading('挂失中');
        $.ajax({
            url: '/api/cardlost',
            type: 'post',
            data: {
                cardPassword: password
            },
            success: function (result) {
                loading.hide();
                if (result.success) {
                    weui.alert('请尽快前往办卡处进行校园卡补办', { title: '挂失校园卡成功' });
                } else {
                    weui.topTips(result.message);
                }
            },
            error: function (result) {
                loading.hide();
                if (result.status) {
                    //网络连接超时
                    weui.topTips(result.responseJSON.message);
                } else {
                    weui.topTips('网络连接异常，请检查网络连接');
                }
            }
        })
    }
}