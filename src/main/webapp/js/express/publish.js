//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//显示错误信息
function showWarningMessage(text) {
    $("#warning").text(text);
    $(".layui-m-layer").show().delay(2000).hide(0);
}

//检测Input文字长度是否超过限制并进行实时字数反馈
function inputLengthCheck(str, maxLen) {
    if (str.value.length > maxLen) {
        str.value = str.value.substr(0, maxLen);
    }
    $("#textCount").text(250 - str.value.length);
}

//发布表白信息
function publishExpress() {
    if ($("#nickname").val() == '') {
        showWarningMessage("你的昵称不能为空");
    } else if ($("#nickname").val().length > 10) {
        showWarningMessage("你的昵称长度不能超过10字");
    } else if ($("#realname").val().length > 10) {
        showWarningMessage("你的真实姓名长度不能超过10字");
    } else if ($("#name").val() == '') {
        showWarningMessage("对方名字不能为空");
    } else if ($("#name").val().length > 10) {
        showWarningMessage("对方名字长度不能超过10字");
    } else if ($("#content").val() == '') {
        showWarningMessage("表白内容不能为空");
    } else if ($("#content").val().length > 250) {
        showWarningMessage("表白内容长度不能超过250字");
    } else {
        $("#submit").attr("disabled", true);
        $("#loading").show();
        $.ajax({
            url: '/api/express',
            type: 'post',
            data: {
                nickname: $("#nickname").val(),
                realname: $("#realname").val(),
                selfGender: $("#self_gender").val(),
                name: $("#name").val(),
                personGender: $("#person_gender").val(),
                content: $("#content").val()
            },
            success: function (result) {
                $("#loading").hide();
                $("#submit").attr("disabled", false);
                if (result.success === true) {
                    window.location.href = '/express';
                } else {
                    showWarningMessage(result.message);
                }
            },
            error: function () {
                $("#loading").hide();
                $("#submit").attr("disabled", false);
                showWarningMessage("网络异常，请检查网络连接");
            }
        });
    }
}
