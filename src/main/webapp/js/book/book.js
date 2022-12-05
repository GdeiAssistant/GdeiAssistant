let list = [];

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

function postQueryForm() {
    if ($("#password").val() === "") {
        $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
    } else {
        $("#loadingToast").show();
        list = [];
        $.ajax({
            url: '/api/bookquery',
            method: 'POST',
            data: {
                'password': $("#password").val()
            },
            success: function (result) {
                $("#loadingToast").hide();
                if (result.success) {
                    if (result.data.length > 0) {
                        $("#result").show();
                        $("#input").hide();
                        $("#booklist").show();
                        for (var i = 0; i < result.data.length; i++) {
                            list.push(result.data[i]);
                            $("#booklist .weui_cells").append("<a class='weui-cell weui-cell_access' href='javascript:' onclick='showBookDetail(" + i + ")'>" +
                                "<div class='weui-cell__bd'><p>" + result.data[i].name + "</p>" +
                                "<p style='font-size:13px;color:#999'>借阅时间：" + result.data[i].borrowDate + "</p>" +
                                "<p style='font-size:13px;color:#999'>应还时间：" + result.data[i].returnDate + "</p>" +
                                "</div><div class='weui-cell__ft'></div></a>");
                        }
                    } else {
                        $("#result").show();
                        $("#input").hide();
                        $("#empty").show();
                    }
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function () {
                $("#loadingToast").hide();
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

//显示图书详细信息
function showBookDetail(index) {
    $("#bookDetailDialog .weui-form-preview__value:eq(0)").text(list[index].name);
    $("#bookDetailDialog .weui-form-preview__value:eq(1)").text(list[index].id);
    $("#bookDetailDialog .weui-form-preview__value:eq(2)").text(list[index].sn);
    $("#bookDetailDialog .weui-form-preview__value:eq(3)").text(list[index].code);
    $("#bookDetailDialog .weui-form-preview__value:eq(4)").text(list[index].author);
    $("#bookDetailDialog .weui-form-preview__value:eq(5)").text(list[index].borrowDate);
    $("#bookDetailDialog .weui-form-preview__value:eq(6)").text(list[index].returnDate);
    $("#bookDetailDialog .weui-form-preview__value:eq(7)").text(list[index].renewTime);
    $("#bookDetailDialog").popup();
}

//续借图书
function renewBook() {
    let sn = $("#bookDetailDialog .weui-form-preview__value:eq(2)").text();
    let code = $("#bookDetailDialog .weui-form-preview__value:eq(3)").text();
    let loading = weui.loading('续借图书中');
    $.ajax({
        url: '/api/bookrenew',
        method: 'POST',
        data: {
            sn: sn,
            code: code
        },
        success: function (result) {
            loading.hide();
            if (result.success) {
                weui.toast('续借图书成功')
                $.closePopup();
            } else {
                weui.topTips(result.message);
            }
        },
        error: function () {
            loading.hide();
            if (result.status == 503) {
                //网络连接超时
                weui.topTips(result.responseJSON.message);
            } else {
                weui.topTips('网络连接异常，请检查网络连接');
            }
        }
    })
}