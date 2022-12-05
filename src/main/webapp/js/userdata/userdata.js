//检查用户数据导出状态
function exportUserData() {
    $.confirm({
        title: '导出用户数据',
        text: '是否确认提交用户数据导出请求？',
        onOK: function () {
            $("#loadingToast").show();
            $.ajax({
                url: '/api/userdata/state',
                method: 'get',
                success: function (result) {
                    if (result.success) {
                        switch (result.data) {
                            case 0:
                                //未导出
                                $.ajax({
                                    url: '/api/userdata/export',
                                    method: 'post',
                                    success: function (result) {
                                        $("#loadingToast").hide();
                                        if (result.success) {
                                            $.alert({
                                                title: '已提交用户数据导出请求',
                                                text: '系统正在准备你的用户数据，请稍候再返回到该页面，并重新点击下载用户数据按钮进行下载'
                                            });
                                        } else {
                                            $.toptip(result.message, 'error');
                                        }
                                    },
                                    error: function () {
                                        $("#loadingToast").hide();
                                        $.toptip('网络连接失败，请检查网络连接！', 'error');
                                    }
                                });
                                break;

                            case 1:
                                //正在导出
                                $.alert({
                                    title: '正在导出用户数据，这可能需要一定的时间',
                                    text: '系统正在准备你的用户数据，请稍候再返回到该页面，并重新点击下载用户数据按钮进行下载'
                                });
                                break;

                            case 2:
                                //已导出
                                $.ajax({
                                    url: '/api/userdata/download',
                                    method: 'post',
                                    success: function (result) {
                                        $("#loadingToast").hide();
                                        if (result.success) {
                                            window.open(result.data);
                                        } else {
                                            $.toptip(result.message, 'error');
                                        }
                                    },
                                    error: function () {
                                        $("#loadingToast").hide();
                                        $.toptip('网络连接失败，请检查网络连接！', 'error');
                                    }
                                });
                                break;
                        }
                    } else {
                        $("#loadingToast").hide();
                        $.toptip(result.message, 'error');
                    }
                },
                error: function () {
                    $.toptip('网络连接失败，请检查网络连接！', 'error');
                }
            })
        }
    });
}