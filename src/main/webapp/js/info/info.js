$(function () {
    loadAnnouncement();
    loadingReadingInfo();
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

//加载专题阅读信息
function loadingReadingInfo() {
    $.ajax({
        url: '/api/reading',
        method: 'GET',
        success: function (result) {
            if (result.success) {
                result.data.forEach(function (element) {
                    $(".recommendation:eq(0) .weui-cells:eq(0)")
                        .append("<a class='weui-cell weui-cell_access' href='" + element.link + "'>" +
                            "<div class='weui-cell__bd'><p>" + element.title + "</p>" +
                            "<p style='font-size:13px;color:#999'>" + element.description + "</p></div> " +
                            "<div class='weui-cell__ft'></div></a>")
                });
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