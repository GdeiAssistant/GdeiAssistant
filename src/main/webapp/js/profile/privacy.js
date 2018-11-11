//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//获取用户隐私设置
$(function () {
    $.ajax({
            url: '/rest/privacy',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data.gender === true) {
                        $("#gender").prop("checked", true);
                    }
                    if (result.data.genderOrientation === true) {
                        $("#genderOrientation").prop("checked", true);
                    }
                    if (result.data.faculty === true) {
                        $("#faculty").prop("checked", true);
                    }
                    if (result.data.major === true) {
                        $("#major").prop("checked", true);
                    }
                    if (result.data.region === true) {
                        $("#region").prop("checked", true);
                    }
                    if (result.data.introduction === true) {
                        $("#introduction").prop("checked", true);
                    }
                    if (result.data.cache === true) {
                        $("#cache").prop("checked", true);
                    }
                }
                else {
                    $.toptip(result.message, 'error');
                }
            },
            error: function () {
                $.toptip('网络连接失败，请检查网络连接', 'error');
            }
        }
    );
});

//更改隐私设置
function changePrivacySetting(index) {
    let state = $("input:eq(" + index + ")").prop("checked");
    $.ajax({
        url: '/rest/privacy',
        type: 'post',
        data: {
            index: index,
            state: state
        },
        success: function (result) {
            if (result.success === true) {
                $.toptip("更新成功", 'success');
            }
            else {
                if (state === true) {
                    $("input:eq(" + index + ")").prop("checked", false);
                }
                else {
                    $("input:eq(" + index + ")").prop("checked", true);
                }
                $.toptip(result.message, 'error');
            }
        },
        error: function () {
            if (state === true) {
                $("input:eq(" + index + ")").prop("checked", false);
            }
            else {
                $("input:eq(" + index + ")").prop("checked", true);
            }
            $.toptip('网络连接失败，请检查网络连接', 'error');
        }
    });
}