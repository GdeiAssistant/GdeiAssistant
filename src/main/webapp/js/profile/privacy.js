$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //配置个人资料页隐私设置小红点图标显示配置
    if (!localStorage.getItem("privacyBadge")) {
        localStorage.setItem("privacyBadge", 1);
    }
});

//获取用户隐私设置
$(function () {
    $.ajax({
            url: '/api/privacy',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data.genderOpen === true) {
                        $("#gender").prop("checked", true);
                    }
                    if (result.data.facultyOpen === true) {
                        $("#faculty").prop("checked", true);
                    }
                    if (result.data.majorOpen === true) {
                        $("#major").prop("checked", true);
                    }
                    if (result.data.locationOpen === true) {
                        $("#location").prop("checked", true);
                    }
                    if (result.data.hometownOpen === true) {
                        $("#hometown").prop("checked", true);
                    }
                    if (result.data.introductionOpen === true) {
                        $("#introduction").prop("checked", true);
                    }
                    if (result.data.enrollmentOpen === true) {
                        $("#enrollerment").prop("checked", true);
                    }
                    if (result.data.ageOpen === true) {
                        $("#age").prop("checked", true);
                    }
                    if (result.data.degreeOpen === true) {
                        $("#degree").prop("checked", true);
                    }
                    if (result.data.professionOpen === true) {
                        $("#profession").prop("checked", true);
                    }
                    if (result.data.primarySchoolOpen === true) {
                        $("#primary_school").prop("checked", true);
                    }
                    if (result.data.juniorHighSchoolOpen === true) {
                        $("#junior_high_school").prop("checked", true);
                    }
                    if (result.data.highSchoolOpen === true) {
                        $("#high_school").prop("checked", true);
                    }
                    if (result.data.colleges === true) {
                        $("colleges").prop("checked", true);
                    }
                    if (result.data.cacheAllow === true) {
                        $("#cache").prop("checked", true);
                    }
                    if (result.data.robotsIndexAllow === true) {
                        $("#robots").prop("checked", true);
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

//更改隐私设置
function changePrivacySetting(event) {
    console.log(event);
    let state = $("input:eq(" + index + ")").prop("checked");
    $.ajax({
        url: '/api/privacy',
        type: 'post',
        data: {
            index: index,
            state: state
        },
        success: function (result) {
            if (result.success === true) {
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