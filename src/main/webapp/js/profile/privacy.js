$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
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
                    weui.topTips(result.message);
                }
            },
            error: function () {
                weui.topTips('网络连接失败，请检查网络连接');
            }
        }
    );
});

//更改隐私设置
function changePrivacySetting(event) {
    let tag = event.id;
    let state = $("#" + tag).prop("checked");
    $.ajax({
        url: '/api/privacy',
        type: 'post',
        data: {
            tag: tag,
            state: state
        },
        success: function (result) {
            if (result.success === true) {
                weui.toast('更新成功');
            } else {
                if (state === true) {
                    $("input:eq(" + index + ")").prop("checked", false);
                } else {
                    $("input:eq(" + index + ")").prop("checked", true);
                }
                weui.topTips(result.message);
            }
        },
        error: function () {
            if (state === true) {
                $("input:eq(" + index + ")").prop("checked", false);
            } else {
                $("input:eq(" + index + ")").prop("checked", true);
            }
            weui.topTips('网络连接失败，请检查网络连接');
        }
    });
}