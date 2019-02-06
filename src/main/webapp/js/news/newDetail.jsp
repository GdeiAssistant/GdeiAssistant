<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    //消除iOS点击延迟
    $(function () {
        FastClick.attach(document.body);
    });

    //下载文件
    function downloadFile(url) {
        if (${sessionScope.yiBanUserID!=null}) {
            download_fun(url, function () {
                if (errorInfo == '该终端类型暂不支持使用') {
                    $(".weui_warn").text("当前设备不支持文件下载功能").show().delay(2000).hide(0);
                }
            });
        } else {
            window.location.href = url;
        }
    }
</script>