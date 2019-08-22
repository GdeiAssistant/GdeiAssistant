//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

function changePassword() {
    if ($("#old_password").val().length == 0 || $("#new_password").val().length == 0) {
        $.toptip('密码不能为空', 'error');
    } else if ($("#old_password").val().length > 35 || $("#new_password").val().length > 35) {
        $.toptip('密码长度不合法', 'error');
    } else if ($("#old_password").val() == $("#new_password").val()) {
        $.toptip('旧密码和新密码不能相同', 'error');
    } else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/api/password',
            data: {
                oldPassword: $("#old_password").val(),
                newPassword: $("#new_password").val()
            },
            type: 'post',
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success === true) {
                    $("#old_password").val("");
                    $("#new_password").val("");
                    $.toptip('修改密码成功', 'success');
                } else {
                    $.alert({
                        title: '修改密码失败',
                        text: result.message
                    });
                }
            },
            error: function () {
                $("#loadingToast, .weui_mask").hide();
                if (result.status) {
                    $.toptip(result.responseJSON.message, 'error');
                } else {
                    $.toptip('网络连接异常，请检查网络连接', 'error');
                }
            }
        })
    }
}