//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

$(document).ready(function () {
    getIntroduction();
});

//检测Input文字长度是否超过限制并进行实时字数反馈
function inputLengthCheck(str, maxLen) {
    if (str.value.length > maxLen) {
        str.value = str.value.substr(0, maxLen);
    }
    $("#introductionLength").text(str.value.length);
}

//弹出修改个人简介窗口
function showIntroductionDialog() {
    $("#introduction_input").val($("#introduction_cache").val());
    $("#changeIntroduction").popup();
    $("#introductionLength").text($("#introduction_input").val().length);
}

//获取个人简介内容
function getIntroduction() {
    $.ajax({
        url: '/api/introduction',
        type: 'get',
        success: function (result) {
            if (result.success === true) {
                if (result.data === '') {
                    $("#introduction_input").val("");
                    $("#introduction_cache").val("");
                    $("#introduction_content").text('这个人很懒，什么都没写_(:3 」∠)_');
                } else {
                    $("#introduction_input").val(result.data);
                    $("#introduction_cache").val(result.data);
                    $("#introduction_content").text(result.data);
                }
            } else {
                showCustomErrorTip(result.message);
            }
        },
        error: function () {
            showNetworkErrorTip();
        }
    })
}

//显示网络错误提示
function showNetworkErrorTip() {
    $(".weui_warn").text('网络连接失败,请检查网络连接').show().delay(2000).hide(0);
}

//显示服务端错误提示
function showCustomErrorTip(message) {
    $(".weui_warn").text(message).show().delay(2000).hide(0);
}

//修改个人简介
function changeIntroduction() {
    var introductionInput = $("#introduction_input").val();
    var introductionCache = $("#introduction_cache").val();
    $.closePopup();
    if (introductionInput !== introductionCache) {
        $.ajax({
            url: '/api/introduction',
            data: {
                introduction: introductionInput
            },
            type: 'post',
            success: function (result) {
                if (result.success === true) {
                    getIntroduction();
                } else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function () {
                showNetworkErrorTip();
            }
        })
    }
}