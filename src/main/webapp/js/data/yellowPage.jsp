<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    let list;

    $(function () {
        //消除iOS点击延迟
        FastClick.attach(document.body);
        //加载黄页信息
        queryYellowPageInfo();
    });

    //显示黄页详细信息
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

    //显示复制地址确认ActionSheet
    function showCopyConfirmActionSheet(content) {
        if (content != '无') {
            $.actions({
                actions: [{
                    text: "复制地址",
                    onClick: function () {
                        copyDetailInfo(content);
                    }
                }]
            });
        }
    }

    //显示电话号码确认ActionSheet
    function showPhoneActionSheet(content) {
        if (content != '无') {
            $.actions({
                actions: [{
                    text: "拨打电话",
                    onClick: function () {
                        phoneCall(content);
                    }
                }, {
                    text: "发送短信",
                    onClick: function () {
                        sendSMS(content);
                    }
                }, {
                    text: "复制号码",
                    onClick: function () {
                        copyDetailInfo(content);
                    }
                }]
            });
        }
    }

    //显示发送邮件确认ActionSheet
    function showEmailActionSheet(content) {
        if (content != '无') {
            $.actions({
                actions: [{
                    text: "发送电子邮件",
                    onClick: function () {
                        sendEmail(content);
                    }
                }, {
                    text: "复制邮箱地址",
                    onClick: function () {
                        copyDetailInfo(content);
                    }
                }]
            });
        }
    }

    //显示网址确认ActionSheet
    function showWebsiteActionSheet(content) {
        if (content != '无') {
            $.actions({
                actions: [{
                    text: "跳转网站",
                    onClick: function () {
                        switchWebSite(content);
                    }
                }, {
                    text: "复制网址",
                    onClick: function () {
                        copyDetailInfo(content);
                    }
                }]
            });
        }
    }

    //跳转到网站地址
    function switchWebSite(content) {
        window.location.href = content;
    }

    //发送短信
    function sendSMS(content) {
        if (${sessionScope.yiBanUserID!=null}) {
            $.toptip('易班客户端暂不支持直接发送短信，你可选择复制电话号码', 'warning');
        } else {
            window.location.href = 'sms:' + content;
        }
    }

    //拨打电话
    function phoneCall(content) {
        if (${sessionScope.yiBanUserID!=null}) {
            phone_fun(content, function (errorInfo) {
                if (errorInfo == '手机号格式错误') {
                    $.toptip('该电话号码不支持直接拨打，你可选择复制电话号码', 'warning');
                } else if (errorInfo == '该终端类型暂不支持使用') {
                    $.toptip('当前设备不支持电话通话功能', 'error');
                }
            });
        } else {
            window.location.href = 'tel:' + content;
        }
    }

    //发送邮件
    function sendEmail(content) {
        if (${sessionScope.yiBanUserID!=null}) {
            mail_fun(content, function (errorInfo) {
                if (errorInfo == '邮箱地址格式错误') {
                    $.toptip('该邮箱地址不支持直接发送，你可选择复制该邮箱地址', 'warning');
                } else if (errorInfo == '该终端类型暂不支持使用') {
                    $.toptip('当前设备不支持电子邮箱功能', 'error');
                }
            });
        } else {
            window.location.href = 'mailto:' + content;
        }
    }

    //复制信息
    function copyDetailInfo(content) {
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

    //加载黄页信息
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