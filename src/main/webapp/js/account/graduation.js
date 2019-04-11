$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //加载毕业用户账号处理方案
    loadGraduationProgram();
});

//加载毕业用户账号处理方案
function loadGraduationProgram() {
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/api/graduation',
        method: 'GET',
        success: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                $("#program").val(result.data.program);
            } else {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            }
        },
        error: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.status) {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
            }
        }
    });
}

//保存毕业用户账号处理方案
function saveGraduationProgram() {
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/api/graduation',
        method: 'POST',
        data: {
            program: $("#program").val()
        },
        success: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                $.toptip('保存成功', 'success');
            } else {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            }
        },
        error: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.status) {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
            }
        }
    });
}