$(function () {
    loadAnnouncement();
});

//加载通知公告
function loadAnnouncement() {
    $.ajax({
        url: '/api/announcement',
        method: 'GET',
        success: function (result) {
            if (result.success) {
                if (result.data) {
                    $(".announcement:eq(0) h2:eq(0)").text(result.data.title);
                    $(".announcement:eq(0) h2:eq(1)").text("时间：" + result.data.publishTime);
                    $(".announcement:eq(0) section").text(result.data.content);
                    $(".announcement:eq(0)").show();
                }
            } else {
                $.toptip(result.message, 'error');
            }
        },
        error: function (result) {
            if (result.status) {
                $.toptip(result.responseJSON.message, 'error');
            } else {
                $.toptip('网络连接异常，请检查并重试', 'error');
            }
        }
    })
}