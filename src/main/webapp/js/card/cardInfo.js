$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //查询校园卡信息
    queryCardInfo();
});

//查询校园卡信息
function queryCardInfo() {
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/cardinfoquery',
        type: 'post',
        success: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                $("#name").text(result.data.name);
                $("#number").text(result.data.number);
                $("#cardNumber").text(result.data.cardNumber);
                $("#cardBalance").text(result.data.cardBalance);
                $("#cardInterimBalance").text(result.data.cardInterimBalance);
                $("#cardLostState").text(result.data.cardLostState);
                $("#cardFreezeState").text(result.data.cardFreezeState);
            }
            else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
            }
        },
        error: function () {
            $("#loadingToast, .weui_mask").hide();
            $(".weui_warn").text("网络连接失败，请检查网络连接").show().delay(2000).hide(0);
        }
    })
}