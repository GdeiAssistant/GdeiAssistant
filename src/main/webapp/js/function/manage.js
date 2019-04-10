$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //设置点击事件
    $(".weui-switch").each(function () {
        $(this).click(function () {
            changeFunctionDisplaySetting($(this).attr("id")
                , $(this).prop("checked"));
        });
    });
    //加载功能显示设置并调整开关组件状态
    loadFunctionDisplaySetting();
    //配置个人资料页功能管理小红点图标显示配置
    if (!localStorage.getItem("functionBadge")) {
        localStorage.setItem("functionBadge", 1);
    }
});

//缓存的显示设置表
let displaySetting = Object.create(null);

//加载功能显示设置
function loadFunctionDisplaySetting() {
    if (localStorage.getItem("functionDisplaySetting")) {
        let setting = JSON.parse(localStorage.getItem("functionDisplaySetting"));
        for (let index = 0; index < $(".weui-switch").length; index++) {
            if (Object.keys(setting).indexOf($(".weui-switch:eq(" + index + ")").attr("id")) == -1) {
                $(".weui-switch:eq(" + index + ")").prop("checked", true);
            } else {
                $(".weui-switch:eq(" + index + ")").prop("checked", setting[$(".weui-switch:eq(" + index + ")").attr("id")]);
            }
        }
    } else {
        $(".weui-switch").prop("checked", true);
    }
}

//设置功能显示设置
function changeFunctionDisplaySetting(id, state) {
    displaySetting[id] = state;
    localStorage.setItem("functionDisplaySetting", JSON.stringify(displaySetting));
    $.toptip('保存成功', 'success');
}

//重置功能显示设置
function resetFunctionDisplaySetting() {
    localStorage.removeItem("functionDisplaySetting");
    $(".weui-switch").prop("checked", true);
    $.toptip('重置成功', 'success');
}