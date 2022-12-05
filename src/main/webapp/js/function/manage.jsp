<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    let displaySetting = Object.create(null);

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
        //显示功能菜单
        loadFunctionMenu();
    });

    //显示功能菜单
    function loadFunctionMenu() {
        $(".weui-cells input").each(function () {
            $(this).parent().parent().show();
        });
    }

    //加载功能显示设置
    function loadFunctionDisplaySetting() {
        if (localStorage.getItem("functionDisplaySetting")) {
            displaySetting = JSON.parse(localStorage.getItem("functionDisplaySetting"));
            for (let index = 0; index < $(".weui-switch").length; index++) {
                if (Object.keys(displaySetting).indexOf($(".weui-switch:eq(" + index + ")").attr("id")) == -1) {
                    $(".weui-switch:eq(" + index + ")").prop("checked", true);
                } else {
                    $(".weui-switch:eq(" + index + ")").prop("checked", displaySetting[$(".weui-switch:eq(" + index + ")").attr("id")]);
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
        weui.toast('保存成功');
    }

    //重置功能显示设置
    function resetFunctionDisplaySetting() {
        localStorage.removeItem("functionDisplaySetting");
        $(".weui-switch").prop("checked", true);
        weui.toast('重置成功');
    }

</script>