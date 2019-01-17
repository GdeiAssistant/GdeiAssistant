//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//保存准考证号
function saveCetNumber() {
    if ($("#cetNumberInput").val().length === 0) {
        //删除保存的准考证号
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/api/cet/number',
            type: 'post',
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    window.location.reload();
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function () {
                $("#loadingToast, .weui_mask").hide();
                if (result.status == 503) {
                    //网络连接超时
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        })
    } else {
        if ($("#cetNumberInput").val().length === 15) {
            $("#loadingToast, .weui_mask").show();
            $.ajax({
                url: '/api/cet/number',
                type: 'post',
                data: {
                    number: $("#cetNumberInput").val()
                },
                success: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.success) {
                        window.location.reload();
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.status == 503) {
                        //网络连接超时
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            });
        } else {
            $(".weui_warn").text("输入的准考证号不正确！").show().delay(2000).hide(0);
        }
    }
}