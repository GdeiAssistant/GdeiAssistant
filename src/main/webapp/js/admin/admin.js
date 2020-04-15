$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //获取管理员选项设置
    LoadAdminSetting();
});

//获取管理员选项设置
function LoadAdminSetting() {
    $.ajax({
            url: '/api/admin',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data.grayscale === true) {
                        $("#grayscale").prop("checked", true);
                    }
                    if (result.data.prideThemeLogo === true) {
                        $("#pridethemelogo").prop("checked", true);
                    }
                    if (result.data.pinkThemeLogo === true) {
                        $("#pinkthemelogo").prop("checked", true);
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
                    if (result.data.authenticationTopic === true) {
                        $("#authentication_topic").prop("checked", true);
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
                        $("#authentication_topic").attr("disabled", true);
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
}

//更改管理员选项设置
function changeAdminSetting(index) {
    $.ajax({
        url: '/api/admin',
        type: 'post',
        data: {
            index: index,
            state: state
        },
        success: function (result) {
            LoadAdminSetting();
            if (result.success === true) {
                $.toptip("更新成功", 'success');
            } else {
                $.toptip(result.message, 'error');
            }
        },
        error: function () {
            LoadAdminSetting();
            $.toptip('网络连接失败，请检查网络连接', 'error');
        }
    });
}