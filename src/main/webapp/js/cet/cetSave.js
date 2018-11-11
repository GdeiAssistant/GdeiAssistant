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
            url: '/cet/number',
            type: 'post',
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    window.location.reload();
                }
                else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function () {
                $("#loadingToast, .weui_mask").hide();
                $(".weui_warn").text("网络连接失败，请稍候再试").show().delay(2000).hide(0);
            }
        })
    }
    else {
        if ($("#cetNumberInput").val().length === 15) {
            $("#loadingToast, .weui_mask").show();
            $.ajax({
                url: '/cet/number',
                type: 'post',
                data: {
                    number: $("#cetNumberInput").val()
                },
                success: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.success) {
                        window.location.reload();
                    }
                    else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function () {
                    $("#loadingToast, .weui_mask").hide();
                    $(".weui_warn").text("网络连接失败，请稍候再试").show().delay(2000).hide(0);
                }
            });
        }
        else {
            $(".weui_warn").text("输入的准考证号不正确！").show().delay(2000).hide(0);
        }
    }
}