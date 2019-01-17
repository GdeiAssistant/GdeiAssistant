//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

$(function () {
    updateCheckCode();
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

//导入保存的准考证号
function importNumber() {
    $.ajax({
        url: '/api/cet/number',
        type: 'get',
        success: function (result) {
            if (result.success) {
                if (result.data == null) {
                    $(".weui_warn").text("你未保存准考证号").show().delay(2000).hide(0);
                } else {
                    $("#number").val(result.data);
                }
            } else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
            }
        },
        error: function () {
            if (result.status == 503) {
                //网络连接超时
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
            }
        }
    });
}

//重新查询
function reQuery() {
    $("#name").val("");
    $("#number").val("");
    $("#result").hide();
    $("#edit").show();
    updateCheckCode();
}

function updateCheckCode() {
    $.ajax({
        url: '/api/cet/checkcode',
        type: 'get',
        success: function (result) {
            $("#checkcode").val("");
            $("#checkcodeImage").attr("src", "data:image/jpg;base64," + result.data);
        },
        error: function () {
            $(".weui_warn").text("网络连接失败，请检查网络连接").show().delay(2000).hide(0);
        }
    })
}

//提交查询请求
function postQueryForm() {
    if ($("#name").val() === "" || $("#number").val() === "" || $("#checkcode").val() === "") {
        $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
    } else if ($('#number').val().length !== 15) {
        $(".weui_warn").text("准考证号长度不正确！").show().delay(2000).hide(0);
    } else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/rest/cetquery',
            type: 'post',
            data: {
                name: $("#name").val(),
                number: $("#number").val(),
                checkcode: $("#checkcode").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    $("#edit").hide();
                    //加载成绩信息
                    $("#cetName").text(result.cet.name);
                    $("#cetType").text("考试类型：" + result.cet.type);
                    $("#cetSchool").text("考生学校：" + result.cet.school);
                    $("#cetTotalScore").text("考试总分：" + result.cet.totalScore);
                    $("#cetListeningScore").text("听力分数：" + result.cet.listeningScore);
                    $("#cetreadingScore").text("阅读分数：" + result.cet.readingScore);
                    $("#cetWritingAndTranslatingScore").text("写作翻译：" + result.cet.writingAndTranslatingScore);
                    $("#result").show();
                } else {
                    updateCheckCode();
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                updateCheckCode();
                $("#loadingToast, .weui_mask").hide();
                if (result.status == 503) {
                    //网络连接超时
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        });
    }
}