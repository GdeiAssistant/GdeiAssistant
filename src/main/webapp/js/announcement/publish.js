//检测Input文字长度是否超过限制并进行实时字数反馈
function inputLengthCheck(str, maxLen) {
    if (str.value.length > maxLen) {
        str.value = str.value.substr(0, maxLen);
    }
    $("#words").text(str.value.length);
}

//提交通知公告信息
function publishAnnouncement() {
    if ($("#title").val().length == 0 || $("#title").val().length > 50) {
        $(".weui_warn").text("标题长度不合法").show().delay(2000).hide(0);
    } else if ($("#content").val().length == 0 || $("#content").val().length > 250) {
        $(".weui_warn").text("内容长度不合法").show().delay(2000).hide(0);
    } else {
        $(".weui-btn").attr("disabled", true);
        let loading = weui.loading('提交中');
        $.ajax({
            url: '/api/announcement',
            type: 'post',
            data: {
                title: $("#title").val(),
                content: $("#content").val()
            },
            success: function (result) {
                $(".weui-btn").attr("disabled", false);
                loading.hide();
                if (result.success === true) {
                    weui.toast('提交成功');
                } else {
                    weui.topTips(result.message);
                }
            },
            error: function () {
                $(".weui-btn").attr("disabled", false);
                loading.hide();
                weui.topTips('网络异常，请检查网络连接');
            }
        });
    }
}