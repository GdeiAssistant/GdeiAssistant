//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//切换到日期选择页面
function switchToPickerPage() {
    $("#result").hide();
    $("#input").show();
}

//选择日期
function showDatePicker() {
    let date = new Date();
    let startDate = new Date(date.getTime() - (365 * 24 * 3600 * 1000));
    let endDate = new Date(date.getTime() + (24 * 3600 * 1000));
    weui.datePicker({
        start: startDate,
        end: endDate,
        defaultValue: [date.getFullYear(), date.getMonth() + 1, date.getDate()],
        onConfirm: function (cetQueryResultEnum) {
            $(".page_desc").text("当前选中日期:" + cetQueryResultEnum[0] + "-" + cetQueryResultEnum[1] + "-" + cetQueryResultEnum[2]);
            $("#year").val(cetQueryResultEnum[0]);
            $("#month").val(cetQueryResultEnum[1]);
            $("#date").val(cetQueryResultEnum[2]);
        },
        id: 'datePicker'
    });
}

//提交查询表单
function postQueryForm() {
    if ($("#year").val() === '' || $("#month").val() === '' || $("#date").val() === '') {
        $(".weui_warn").text("请选择查询的日期").show().delay(2000).hide(0);
    } else {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/cardquery',
            method: 'post',
            data: {
                'year': $("#year").val(),
                'month': $("#month").val(),
                'date': $("#date").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    //清空查询结果
                    $("#cardQueryResult").empty();
                    $("#currentDate").text("当前查询日期：" + $("#year").val() + "年"
                        + $("#month").val() + "月" + $("#date").val() + "日");
                    if (result.data.cardList.length == 0) {
                        $("#currentDate").css("text-align", "center");
                        $("#errorTip").show();
                    } else {
                        $("#currentDate").css("text-align", "left");
                        $("#errorTip").hide();
                        result.data.cardList.forEach(function (card) {
                            let price = card.tradePrice.substr(0, 1) == '-' ? card.tradePrice : "+" + card.tradePrice;
                            $("#cardQueryResult").append("<div class='weui-cell'>" + "<div class='weui-cell__bd'><p>" + card.merchantName
                                + "【" + card.tradeName + "】</p><p style='font-size: 13px;color: #999'>"
                                + card.tradeTime + "</p></div><div class='weui-cell__ft' style='font-size: 15px'>"
                                + price + "元</div></div>");
                        });
                    }
                    $("#input").hide();
                    $("#result").show();
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
        })
    }
}