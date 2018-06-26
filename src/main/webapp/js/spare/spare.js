//可课室信息列表
var spareRoomList;

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//提交查询请求
function postQueryForm() {
    $(".weui_btn").attr("disabled", true);
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/sparequery',
        type: 'post',
        data: $("#spareForm").serialize(),
        success: function (result) {
            $(".weui_btn").attr("disabled", false);
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                $("#form").hide();
                $("#result").show();
                spareRoomList = result.data;
                for (var i = 0; i < result.data.length; i++) {
                    $("#resultList").append("<div class='weui-cell weui-cell_access open-popup' href='javascript:' onclick='showDetail(" + i + ")'>"
                        + "<div class='weui-cell__bd'><p>" + result.data[i].zone + "-" + result.data[i].type + "</p></div>"
                        + "<div class='weui-cell__ft'>" + result.data[i].name + "</div></div>");
                }
            }
            else {
                $.toptip(result.errorMessage, 'error');
            }
        },
        error: function () {
            $(".weui_btn").attr("disabled", false);
            $("#loadingToast, .weui_mask").hide();
            $.toptip('网络连接异常，请重试', 'error');
        }
    })
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