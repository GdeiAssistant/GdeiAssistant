<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="access" uri="/WEB-INF/tld/access.tld" %>
<script>

    let displaySetting = Object.create(null);

    let access = [];

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
        //加载权限表信息
        loadAccessInfo();
        //显示当前权限组的功能菜单
        loadFunctionMenu();
    });

    //显示当前权限组的功能菜单
    function loadFunctionMenu() {
        $(".weui-cells input").each(function () {
            //检测当前用户组有无权限
            if (access[${sessionScope.group}].has($(this).attr("id"))) {
                $(this).parent().parent().show();
            }
        });
    }

    //加载权限表信息
    function loadAccessInfo() {

        <c:set var="Access" scope="page" value="${access:loadAccessInfo()}" />

        <c:forEach begin="0" end="${Access.size()-1}" step="1" varStatus="state">

        access[${state.index}] = new Set();

        </c:forEach>

        <c:forEach items="${Access}" var="AccessList" varStatus="state">

        <c:forEach items="${AccessList}" var="access">

        access[${state.index}].add("${access}");

        </c:forEach>

        </c:forEach>

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
        $.toptip('保存成功', 'success');
    }

    //重置功能显示设置
    function resetFunctionDisplaySetting() {
        localStorage.removeItem("functionDisplaySetting");
        $(".weui-switch").prop("checked", true);
        $.toptip('重置成功', 'success');
    }

</script>