$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
});

//获取用户隐私设置
$(function () {
    $.ajax({
            url: '/api/admin',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data.grayscale === true) {
                        $("#grayscale").prop("checked", true);
                    }
                    if (result.data.pridetheme === true) {
                        $("#pridetheme").prop("checked", true);
                    }
                    if (result.data.authenticationErshou === true) {
                        $("#authentication_ershou").prop("checked", true);
                    }
                    if (result.data.authenticationLostAndFound === true) {
                        $("#authentication_lostandfound").prop("checked", true);
                    }
                    if (result.data.authenticationSecret === true) {
                        $("#authentication_secret").prop("checked", true);
                    }
                    if (result.data.authenticationDelivery === true) {
                        $("#authentication_delivery").prop("checked", true);
                    }
                    if (result.data.authenticationPhotograph === true) {
                        $("#authentication_photograph").prop("checked", true);
                    }
                    if (result.data.authenticationExpress === true) {
                        $("#authentication_express").prop("checked", true);
                    }
                    if (result.data.authenticationForce === true) {
                        $("#authentication_force").prop("checked", true);
                        $("#authentication_ershou").prop("checked", true);
                        $("#authentication_lostandfound").prop("checked", true);
                        $("#authentication_secret").prop("checked", true);
                        $("#authentication_delivery").prop("checked", true);
                        $("#authentication_photograph").prop("checked", true);
                        $("#authentication_express").prop("checked", true);
                        $("#authentication_ershou").attr("disabled", true);
                        $("#authentication_lostandfound").attr("disabled", true);
                        $("#authentication_secret").attr("disabled", true);
                        $("#authentication_delivery").attr("disabled", true);
                        $("#authentication_photograph").attr("disabled", true);
                        $("#authentication_express").attr("disabled", true);
                    }
                } else {
                    $.toptip(result.message, 'error');
                }
            },
            error: function () {
                $.toptip('网络连接失败，请检查网络连接', 'error');
            }
        }
    );
});

//更改管理员选项设置
function changeAdminSetting(index) {
    let state = $("input:eq(" + index + ")").prop("checked");
    $.ajax({
        url: '/api/admin',
        type: 'post',
        data: {
            index: index,
            state: state
        },
        success: function (result) {
            if (result.success === true) {
                if (index === 2) {
                    //强制实名认证启用/停用后，关闭/开启各功能模块实名认证设置可用性
                    $("#authentication_ershou").prop("checked", state);
                    $("#authentication_lostandfound").prop("checked", state);
                    $("#authentication_secret").prop("checked", state);
                    $("#authentication_delivery").prop("checked", state);
                    $("#authentication_photograph").prop("checked", state);
                    $("#authentication_express").prop("checked", state);
                    $("#authentication_ershou").attr("disabled", state);
                    $("#authentication_lostandfound").attr("disabled", state);
                    $("#authentication_secret").attr("disabled", state);
                    $("#authentication_delivery").attr("disabled", state);
                    $("#authentication_photograph").attr("disabled", state);
                    $("#authentication_express").attr("disabled", state);
                }
                $.toptip("更新成功", 'success');
            } else {
                if (state === true) {
                    $("input:eq(" + index + ")").prop("checked", false);
                } else {
                    $("input:eq(" + index + ")").prop("checked", true);
                }
                $.toptip(result.message, 'error');
            }
        },
        error: function () {
            if (state === true) {
                $("input:eq(" + index + ")").prop("checked", false);
            } else {
                $("input:eq(" + index + ")").prop("checked", true);
            }
            $.toptip('网络连接失败，请检查网络连接', 'error');
        }
    });
}