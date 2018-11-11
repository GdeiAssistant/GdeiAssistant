<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    //消除iOS点击延迟
    $(function () {
        FastClick.attach(document.body);
    });

    //提交表单数据登录
    function postLoginForm() {
        if ($("#username").val() === "" || $("#password").val() === "") {
            $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
        }
        else {
            $("#loadingToast, .weui_mask").show();
            $.ajax({
                url: "/yiban/userattach",
                data: {
                    username: $("#username").val(),
                    password: $("#password").val()
                },
                type: 'post',
                success: function (yibanAttachResult) {
                    //隐藏进度条
                    $("#loadingToast, .weui_mask").hide();
                    if (yibanAttachResult.success === true) {
                        //绑定易班教务系统账号成功
                        <c:choose>
                        <c:when test="${RedirectURL!=null}">
                        window.location.href = '${RedirectURL}';
                        </c:when>
                        <c:otherwise>
                        window.location.href = '/index';
                        </c:otherwise>
                        </c:choose>
                    }
                    else {
                        //显示绑定失败信息提示
                        $(".weui_warn").text(yibanAttachResult.message).show().delay(2000).hide(0);
                    }
                },
                error: function () {
                    //隐藏进度条
                    $("#loadingToast, .weui_mask").hide();
                    $(".weui_warn").text("绑定教务系统账号失败,请检查网络连接！").show().delay(2000).hide(0);
                }
            });
        }
    }

</script>