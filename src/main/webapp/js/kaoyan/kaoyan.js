let examNumberRegExp = new RegExp("^[0-9]*$");

let idNumberRegExp = new RegExp("^((\\d{18})|([0-9x]{18})|([0-9X]{18}))$");

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//查询成绩信息
function postQueryForm() {
    let name = $("#name").val();
    let idNumber = $("#idNumber").val();
    let examNumber = $("#examNumber").val();
    if (name !== "" && examNumber !== "" && idNumber !== "") {
        if (name.length < 1 || name.length > 15) {
            $(".weui_warn").text("姓名长度不合法").show().delay(2000).hide(0);
        } else if (examNumber.length !== 15 || !examNumberRegExp.test(examNumber)) {
            $(".weui_warn").text("考号格式不合法").show().delay(2000).hide(0);
        } else if (idNumber.length !== 18 || !idNumberRegExp.test(idNumber)) {
            $(".weui_warn").text("证件号格式不合法").show().delay(2000).hide(0);
        } else {
            let loading = weui.loading('查询中');
            $.ajax({
                url: '/rest/kaoyanquery',
                method: 'post',
                data: {
                    name: name,
                    idNumber: idNumber,
                    examNumber: examNumber
                },
                success: function (result) {
                    loading.hide();
                    if (result.success) {
                        $("#input").hide();
                        $("#result").show();
                        $("#result_name").text(result.data.name);
                        $("#result_signUpNumber").text(result.data.signUpNumber);
                        $("#result_examNumber").text(result.data.examNumber);
                        $("#result_totalScore").text(result.data.totalScore);
                        $("#result_firstScore").text(result.data.firstScore);
                        $("#result_secondScore").text(result.data.secondScore);
                        $("#result_thirdScore").text(result.data.thirdScore);
                        $("#result_fourthScore").text(result.data.fourthScore);
                    } else {
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
    } else {
        weui.topTips('请将信息填写完整');
    }
}