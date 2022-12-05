$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //查询校园卡信息
    queryCardInfo();
});

//查询校园卡信息
function queryCardInfo() {
    let loading = weui.loading('查询中');
    $.ajax({
        url: '/api/cardinfoquery',
        type: 'post',
        success: function (result) {
            loading.hide();
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
                weui.topTips(result.message);
            }
        },
        error: function (result) {
            loading.hide();
            if (result.status) {
                //网络连接超时
                weui.topTips(result.responseJSON.message);
            } else {
                weui.topTips('网络连接异常，请检查网络连接');
            }
        }
    })
}