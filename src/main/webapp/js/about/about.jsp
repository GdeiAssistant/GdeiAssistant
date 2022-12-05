<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="environment" uri="/WEB-INF/tld/environment.tld" %>
<script>

    //检测浏览器窗口大小并显示对应下载页
    $(function () {
        if (document.body.clientWidth > 500) {
            $("#desktop").show();
        } else {
            $("#phone").show();
        }
    });

    //弹出Cookie政策提示
    $(function () {
        if (!localStorage.getItem("cookiePolicy")) {
            $("#rnw_cookies_banner").show();
        }
    });

    //开发模式下检查功能模块启动情况
    $(function () {
        if ('${environment:getSpringProfileName()}' == 'development') {
            $.ajax({
                url: '/api/module/state',
                method: 'GET',
                success: function (result) {
                    let data = result.data.replace(/\r\n/g, '<br/>').replace(/\n/g, '<br/>');
                    $('.module-check-result-text').html(data);
                }
            });
        }
    });

    $(function () {
        if ('${environment:getSpringProfileName()}' == 'development') {
            $.ajax({
                url: '/api/module/core/state',
                method: 'GET',
                success: function (result) {
                    let data = result.data.replace(/\r\n/g, '<br/>').replace(/\n/g, '<br/>');
                    $('.core-module-check-result-text').html(data);
                }
            });
        }
    });

</script>