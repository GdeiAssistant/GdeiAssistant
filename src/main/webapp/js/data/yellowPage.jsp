<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    let list;

    //消除iOS点击延迟
    $(function () {
        FastClick.attach(document.body);
    });

    $(function () {
        queryYellowPageInfo();
    });

    function showDetailInfo(id) {
        $("#detail .page_title").text(list[id].section);
        $("#detail .weui-cell__ft:eq(0)").text(list[id].typeName);
        $("#detail .weui-cell__ft:eq(1)").text(list[id].campus);
        $("#detail .weui-cell__ft:eq(2)").text(list[id].majorPhone ? list[id].majorPhone : "无");
        $("#detail .weui-cell__ft:eq(3)").text(list[id].minorPhone ? list[id].minorPhone : "无");
        $("#detail .weui-cell__ft:eq(4)").text(list[id].address ? list[id].address : "无");
        $("#detail .weui-cell__ft:eq(5)").text(list[id].email ? list[id].email : "无");
        $("#detail .weui-cell__ft:eq(6)").text(list[id].website ? list[id].website : "无");
        $("#detail").popup();
    }

    function switchWebSite(content) {
        if (content != '无') {
            window.location.href = content;
        }
    }

    function phoneCall(content) {
        if (content != '无') {
            if (${sessionScope.yiBanUserID!=null}) {
                phone_fun(content, function (errorInfo) {
                    if (errorInfo == '手机号格式错误') {
                        copyDetailInfo(content);
                    } else if (errorInfo == '该终端类型暂不支持使用') {
                        $.toptip('当前设备不支持电话通话功能', 'error');
                    }
                });
            } else {
                window.location.href = 'sms:' + content;
            }
        }
    }

    function sendEmail(content) {
        if (content != '无') {
            if (${sessionScope.yiBanUserID!=null}) {
                mail_fun(content, function (errorInfo) {
                    if (errorInfo == '邮箱地址格式错误') {
                        copyDetailInfo(content);
                    } else if (errorInfo == '该终端类型暂不支持使用') {
                        $.toptip('当前设备不支持电子邮箱功能', 'error');
                    }
                });
            } else {
                window.location.href = 'mailto:' + content;
            }
        }
    }

    function copyDetailInfo(content) {
        if (content != '无') {
            const input = document.createElement('input');
            input.setAttribute('readonly', 'readonly');
            input.setAttribute('value', content);
            document.body.appendChild(input);
            input.select();
            if (document.execCommand('copy')) {
                document.execCommand('copy');
                $.toptip('复制成功', 'success');
            } else {
                $.toptip('当前浏览器不支持复制操作', 'error');
            }
            document.body.removeChild(input);
        }
    }


    function queryYellowPageInfo() {
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: '/api/data/yellowpage',
            method: 'GET',
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.success) {
                    result.data.type.forEach(function (element) {
                        $("#result").append("<div class='weui-cell-container'><div class='weui-cells__title'>" + element.typeName + "</div><div class='weui-cells'></div></div>")
                    });
                    result.data.data.forEach(function (element, index) {
                        $(".weui-cells:eq(" + (parseInt(element.typeCode) - 1) + ")").append("<div class='weui-cell weui-cell_access' href='javascript:' onclick='showDetailInfo(" + index + ")'><div class='weui-cell__bd'>" +
                            "<p>" + element.section + "</p></div><div class='weui-cell__ft'></div></div>")
                    });
                    list = result.data.data;
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.status) {
                    $(".weui_warn").text("服务暂不可用，请稍后再试").show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查并重试").show().delay(2000).hide(0);
                }
            }
        })
    }

</script>