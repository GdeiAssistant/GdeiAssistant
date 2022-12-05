//可课室信息列表
var spareRoomList;

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//提交查询请求
function postQueryForm() {
    if (parseInt($("#startTime").val()) > parseInt($("#endTime").val())) {
        $.toptip('起始时间不能晚于结束时间', 'error');
    } else if (parseInt($("#minWeek").val()) >= parseInt($("#maxWeek").val())) {
        $.toptip('起始星期数不能晚于或等于结束星期数', 'error');
    } else {
        $.showLoading('查询中');
        $.ajax({
            url: '/api/sparequery',
            type: 'post',
            data: $("#spareForm").serialize(),
            success: function (result) {
                $.hideLoading();
                if (result.success) {
                    $("#form").hide();
                    $("#result").show();
                    spareRoomList = result.data;
                    for (var i = 0; i < result.data.length; i++) {
                        $("#resultList").append("<div class='weui-cell weui-cell_access open-popup' href='javascript:' onclick='showDetail(" + i + ")'>"
                            + "<div class='weui-cell__bd'><p>" + result.data[i].zone + "-" + result.data[i].type + "</p></div>"
                            + "<div class='weui-cell__ft'>" + result.data[i].name + "</div></div>");
                    }
                } else {
                    $.toptip(result.message, 'error');
                }
            },
            error: function (result) {
                $.hideLoading();
                if (result.status) {
                    //网络连接超时
                    $.toptip(result.responseJSON.message, 'error');
                } else {
                    $.toptip('网络连接异常，请检查网络连接', 'error');
                }
            }
        })
    }
}

//弹出详细信息
function showDetail(index) {
    $("#detail_number").text(spareRoomList[index].number);
    $("#detail_name").text(spareRoomList[index].name);
    $("#detail_type").text(spareRoomList[index].type);
    $("#detail_classSeating").text(spareRoomList[index].classSeating);
    $("#detail_examSeating").text(spareRoomList[index].examSeating);
    $("#detail_zone").text(spareRoomList[index].zone);
    $("#detail_seation").text(spareRoomList[index].section);
    $("#detail").popup();
}