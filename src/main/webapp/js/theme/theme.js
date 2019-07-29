$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //加载当前选中的主题
    let theme = localStorage.getItem("theme");
    if (theme) {
        $("#" + theme).prop("checked", true);
    }
    //配置个人资料页功能管理小红点图标显示配置
    if (!localStorage.getItem("themeBadge")) {
        localStorage.setItem("themeBadge", 1);
    }
    //设置单选按钮点击事件
    $(".weui-check").change(function () {
        changeTheme($(this).attr("id"));
    });
});

function changeTheme(theme) {
    localStorage.setItem("theme", theme);
    let links = document.querySelectorAll('link[title]');
    [].slice.call(links).forEach(function (link) {
        link.disabled = false;
    });
    [].slice.call(links).forEach(function (link) {
        link.disabled = true;
    });
    [].slice.call(links).forEach(function (link) {
        if (link.getAttribute("title") == theme) {
            link.disabled = false;
        }
    });
}