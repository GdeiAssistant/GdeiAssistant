$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //获取管理员选项设置
    LoadAdminSetting();
});

//更改管理员选项设置
function changeAdminSetting(index) {
    $.ajax({
        url: '/api/admin',
        type: 'post',
        data: {
            index: index,
            state: state
        },
        success: function (result) {
            LoadAdminSetting();
            if (result.success === true) {
                $.toptip("更新成功", 'success');
            } else {
                $.toptip(result.message, 'error');
            }
        },
        error: function () {
            LoadAdminSetting();
            $.toptip('网络连接失败，请检查网络连接', 'error');
        }
    });
}