<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="access" uri="/WEB-INF/tld/access.tld" %>
<script>

    //权限表信息
    var access = [];

    //用户是否为微信浏览器
    var wechatUser = false;

    //用户是否为易班浏览器
    var yibanUser = false;

    //消除iOS点击延迟
    $(function () {
        FastClick.attach(document.body);
    });

    //加载权限表信息
    $(function () {

        <c:set var="Access" scope="page" value="${access:loadAccessInfo()}" />

        <c:forEach begin="0" end="${Access.size()}" step="1" varStatus="state">

        access[${state.index}] = new Set();

        </c:forEach>

        <c:forEach items="${Access}" var="AccessList" varStatus="state">

        <c:forEach items="${AccessList}" var="access">

        access[${state.index}].add("${access}");

        </c:forEach>

        </c:forEach>

    });

    //检查用户浏览器属性
    $(function () {

        <c:if test="${sessionScope.yiBanUserID!=null}">
        //易班登录
        yibanUser = true;
        </c:if>
        if (navigator.userAgent.toLowerCase().match(/MicroMessenger/i) == "micromessenger") {
            //微信登录
            wechatUser = true;
        }
    });

    //加载个人姓名信息和头像地址
    $(function () {
        $.ajax({
            url: '/api/profile',
            async: true,
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    $("#right_name").text(result.data.kickname);
                }
            }
        });
        $.ajax({
            url: '/api/avatar',
            async: true,
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data !== '') {
                        $("#right_avatar").attr("src", result.data);
                    }
                }
            }
        });
    });

    //加载显示设置，并重新调整单元格边框属性
    $(function () {
        if (localStorage.getItem("functionDisplaySetting")) {
            let setting = JSON.parse(localStorage.getItem("functionDisplaySetting"));
            for (let index = 0; index < $(".links div").length; index++) {
                //检测当前用户组有无权限
                if (access[${sessionScope.group}].has($(".links div:eq(" + index + ")").attr("id"))) {
                    //检测功能管理是否设置为显示
                    if (Object.keys(setting).indexOf($(".links div:eq(" + index + ")").attr("id")) == -1) {
                        $(".links div:eq(" + index + ")").show();
                    } else {
                        if (setting[$(".links div:eq(" + index + ")").attr("id")] == true) {
                            $(".links div:eq(" + index + ")").show();
                        }
                    }
                }
            }
        } else {
            for (let index = 0; index < $(".links div").length; index++) {
                //检测当前用户组有无权限
                if (access[${sessionScope.group}].has($(".links div:eq(" + index + ")").attr("id"))) {
                    //检测功能管理是否设置为显示
                    $(".links div:eq(" + index + ")").show();
                }
            }
        }
        let functionSize = $("[class='links']").find("div").length;
        let j = 0;
        for (let i = 0; i < functionSize; i++) {
            if (!$("[class='links']").find("div").eq(i).is(":hidden")) {
                if ((j + 1) % 1 == 0) {
                    $("[class='links']").find("div").eq(i).css("border-right", "1px solid #E2E0E3")
                } else if ((j + 1) % 2 == 0) {
                    $("[class='links']").find("div").eq(i).css("border-right", "1px solid #E2E0E3")
                }
                if ((j + 1) % 3 == 0) {
                    $("[class='links']").find("div").eq(i).css("border-right", "0px");
                }
                j++;
            }
        }
        //如果显示的功能菜单不足三个，则隐藏第一行对应的上边框
        if ($(".links div").filter(":visible").length < 3) {
            $(".phone .links").css("border-top", "0px");
            for (let index = 0; index < $(".links div").filter(":visible").length; index++) {
                $(".links").find("div:visible").eq(index).css("border-top", "1px solid #E2E0E3");
            }
        }
    });

    //进行体测查询系统
    function linkToPFTSystem() {
        if (wechatUser) {
            window.location.href = 'https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxa2d196aa4b8a7600&redirect_uri=http%3A%2F%2F5itsn.com%2FWeixin%2FOAuth2%2FUserInfoCallback&response_type=code&scope=snsapi_userinfo&state=TestUrlTestResult&connect_redirect=1#wechat_redirect';
        } else {
            $.alert("请关注微信公众号易小助使用体测查询功能", "请使用微信客户端进行登录");
        }
    }

    //弹出重新绑定易班确认框
    function showYibanAttachConfirm() {
        if (yibanUser) {
            $.confirm({
                title: '重新绑定易班',
                text: '重新绑定易班账号将退出当前账号，你确定吗？',
                onOK: function () {
                    window.location.href = '/yiban/attach';
                }
            });
        } else {
            $.alert("请使用易班客户端进行登录", "错误提示");
        }
    }

    //弹出退出确认框
    function showLogoutConfirm() {
        if (!yibanUser) {
            $.confirm({
                title: '退出当前账号',
                text: '你确定退出当前账号并清除账号缓存吗？',
                onOK: function () {
                    //清空本地缓存
                    localStorage.clear();
                    window.location.href = '/logout';
                }
            });
        } else {
            $.alert("易班客户端不支持账号退出，你可以重新绑定易班账号", "错误提示");
        }
    }

    //切换TabPanel
    function switchTabPanel(index) {
        for (let i = 0; i < $(".weui-tabbar a").length; i++) {
            $(".weui-tabbar a:eq(" + i + ")").removeClass("weui-bar__item_on");
            $(".tabPanelItem:eq(" + i + ")").hide();
        }
        $(".weui-tabbar a:eq(" + index + ")").addClass("weui-bar__item_on");
        $(".tabPanelItem:eq(" + index + ")").show();
    }

</script>