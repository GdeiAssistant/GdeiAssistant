//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//提交一键评教请求
function postEvaluateForm() {
    let loading = weui.loading('提交中');
    let checked = $("#directlySubmit").prop("checked");
    $.ajax({
        url: '/api/evaluate',
        type: 'post',
        data: {
            directlySubmit: checked
        },
        success: function (result) {
            loading.hide();
            if (result.success === true) {
                if (checked) {
                    weui.alert('已提交教学质量评价信息', {
                        title: '教学质量评价成功',
                        buttons: [{
                            label: '好',
                            type: 'primary'
                        }]
                    });
                } else {
                    weui.alert('请登录教务系统进行最终确认', {
                        title: '教学质量评价成功',
                        buttons: [{
                            label: '好',
                            type: 'primary'
                        }]
                    });
                }
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
    });
}