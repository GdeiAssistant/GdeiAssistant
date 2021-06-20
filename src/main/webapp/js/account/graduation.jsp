<%@ page import="cn.gdeiassistant.Enum.UserGroup.UserGroupEnum" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    <c:set value="<%= UserGroupEnum.STUDENT.getValue()%>" var="student"/>
    <c:set value="<%= UserGroupEnum.TEST.getValue()%>" var="test"/>

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
        <c:choose>
        <c:when test="${sessionScope.group==student || sessionScope.group==test}">
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
        </c:when>
        <c:otherwise>
        $(".weui_warn").text("当前用户组不支持毕业账号处理方案功能").show().delay(2000).hide(0);
        </c:otherwise>
        </c:choose>
    }

</script>