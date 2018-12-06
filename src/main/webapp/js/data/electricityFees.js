//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
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

//提交电费查询请求
function postQueryForm() {
    if ($("#name").val() === '' || $("#number").val() === '' || $("#year").val() === '') {
        $(".weui_warn").text("请将信息填写完整").show().delay(2000).hide(0);
    } else if (!new RegExp("^[0-9]*$").test($("#number").val()) || $("#number").val().length !== 11) {
        $(".weui_warn").text("请输入正确的学号").show().delay(2000).hide(0);
    } else if ($("#year").val() < 2016 || $("#year").val() > 2050) {
        $(".weui_warn").text("请选择正确的年份").show().delay(2000).hide(0);
    } else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/api/data/electricfees',
            method: 'GET',
            data: {
                name: $("#name").val(),
                number: $("#number").val(),
                year: $("#year").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    $("#result").show();
                    $("#edit").hide();
                    //填入查询结果
                    $("#result .weui-cell__ft:eq(0)").text(result.data.year);
                    $("#result .weui-cell__ft:eq(1)").text(result.data.buildingNumber + result.data.roomNumber);
                    $("#result .weui-cell__ft:eq(2)").text(result.data.peopleNumber);
                    $("#result .weui-cell__ft:eq(3)").text(result.data.usedElectricAmount);
                    $("#result .weui-cell__ft:eq(4)").text(result.data.freeElectricAmount);
                    $("#result .weui-cell__ft:eq(5)").text(result.data.feeBasedElectricAmount);
                    $("#result .weui-cell__ft:eq(6)").text(result.data.electricPrice);
                    $("#result .weui-cell__ft:eq(7)").text(result.data.totalElectricBill);
                    $("#result .weui-cell__ft:eq(8)").text(result.data.averageElectricBill);
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.status) {
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查并重试").show().delay(2000).hide(0);
                }
            }
        })
    }
}