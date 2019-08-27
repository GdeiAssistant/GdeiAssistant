<%@ page import="edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="access" uri="/WEB-INF/tld/access.tld" %>
<script>

    var userGroupMap = [];

    <c:set value="<%= UserGroupEnum.STUDENT.getValue()%>" var="student"/>
    <c:set value="<%= UserGroupEnum.TEST.getValue()%>" var="test"/>

    $(function () {
        FastClick.attach(document.body);
        loadAuthenticationData();
        loadUserGroupMap();
    });

    //加载用户组映射表
    function loadUserGroupMap() {

        <c:forEach items="${access:loadUserGroupInfo()}" var="userGroup" varStatus="status">

        userGroupMap[${status.index}] = "${userGroup}";

        </c:forEach>
    }

    //加载实名认证信息
    function loadAuthenticationData() {
        $.ajax({
            url: '/api/authentication',
            type: 'GET',
            success: function (result) {
                if (result.success) {
                    if (result.data) {
                        $("#name").text(result.data.realname);
                        $("#authenticateState").text("已认证");
                        $("#nameCell").show();
                        $("#removeAuthentication").show();
                    } else {
                        $("#name").text("");
                        $("#authenticateState").text("未认证");
                        $("#nameCell").hide();
                        $("#removeAuthentication").hide();
                        //弹出提示进行实名认证的弹窗
                        $.alert({
                            text: '根据《中华人民共和国网络安全法》第二十四条的规定要求，为保障你使用广东二师助手不受限制，' +
                                '请尽快完成实名认证',
                            title: '未进行实名认证'
                        });
                    }
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                if (result.status) {
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        })
    }

    //与易班校方认证信息进行同步
    function authenticateWithYiBan() {
        <c:choose>
        <c:when test="${sessionScope.yiBanUserID!=null}">
        //易班登录
        $.confirm({
            text: '我们将依照《隐私政策》保护你的个人信息，若你点击确定按钮，将视为你已阅读并同意《隐私政策》',
            title: '授权广东二师助手获取你的易班用户信息',
            onOK: function () {
                $("#loadingToast, .weui_mask").show();
                $.ajax({
                    url: '/api/authentication',
                    type: 'POST',
                    data: {
                        method: 2
                    },
                    success: function (result) {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.success) {
                            $.alert({
                                text: '已完成实名认证，你现在可以使用广东二师助手所有功能了',
                                title: '实名认证成功',
                                onOK: function () {
                                    loadAuthenticationData();
                                }
                            });
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
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
        });
        </c:when>
        <c:otherwise>
        $.alert({
            text: '请使用易班客户端登录并完成认证',
            title: '错误提示'
        });
        </c:otherwise>
        </c:choose>
    }

    //与教务系统身份认证信息进行同步
    function authenticateBySystem() {
        <c:choose>
        <c:when test="${sessionScope.group==student || sessionScope.group==test}">
        $.confirm({
            text: '我们将依照《隐私政策》保护你的个人信息，若你点击确定按钮，将视为你已阅读并同意《隐私政策》',
            title: '授权广东二师助手登录你的教务系统并获取你的个人信息',
            onOK: function () {
                $("#loadingToast, .weui_mask").show();
                $.ajax({
                    url: '/api/authentication',
                    type: 'POST',
                    data: {
                        method: 0
                    },
                    success: function (result) {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.success) {
                            $.alert({
                                text: '已完成实名认证，你现在可以使用广东二师助手所有功能了',
                                title: '实名认证成功',
                                onOK: function () {
                                    loadAuthenticationData();
                                }
                            });
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
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
        });
        </c:when>
        <c:otherwise>
        $.alert({
            text: '当前用户组不支持使用教务系统进行实名认证',
            title: '错误提示'
        });
        </c:otherwise>
        </c:choose>
    }

    //清除实名认证信息
    function removeAuthenticationData() {
        $.confirm({
            text: '即将清除你的实名认证信息，你的账号将恢复为未实名认证状态，这可能导致你使用本应用功能受到限制',
            title: '注销实名认证',
            onOK: function () {
                $("#loadingToast, .weui_mask").show();
                $.ajax({
                    url: '/api/authentication/remove',
                    type: 'POST',
                    success: function (result) {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.success) {
                            $.alert({
                                text: '已清除实名认证信息',
                                title: '注销实名认证成功',
                                onOK: function () {
                                    loadAuthenticationData();
                                }
                            });
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
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
        });
    }

    //使用大陆居民身份证进行认证，确认后弹出调用摄像头拍照的提示
    function authenticateWithMainlandIDCard() {
        $.alert({
            text: '请使用摄像头拍摄身份证个人信息页，并上传供广东二师助手进行实名认证。注意，该方式仅支持使用中华人民共和国第二代居民身份证进行认证。' +
                '证件的信息需完整且清晰可辨，无反光、遮挡、水印、证件套、LOGO等，不缺边角。此外，证件必须真实拍摄，不能使用复印件，不能对镜。',
            title: '申请访问摄像头权限',
            onOK: function () {
                getVideoMedia();
            }
        });
    }

    //使用大陆其他类型的证件联系客服认证
    function authenticateWithMainlandDocuments() {
        $.alert({
            text: '当前认证方式仅限于中国大陆护照、军官证、士兵证、外国人永久居留证和港澳台居民证等非中华人民共和国第二代居民身份证件进行认证，否则你的认证申请可能会遭到拒绝。',
            title: '注意事项',
            onOK: function () {
                window.location.href = "mailto:support@gdeiassistant.cn?subject=中国大陆证件实名认证&body=请补全此模板邮件中的空缺信息，并通过附件上传军官证、士兵证、外国人永久居留证等中国大陆证件基本身份信息面的扫描件或照片，一并发送到support@gdeiassistant.cn。" +
                    "你提供的信息将被严格保密，并在完成实名认证后的48小时内进行删除。%0d%0a%0d%0a用户名：${sessionScope.username}%0d%0a用户组：" + userGroupMap[${sessionScope.group-1}] + "%0d%0a姓名：%0d%0a证件类型：%0d%0a证件号/编号：";
            }
        });
    }

    //使用港澳身份证件联系客服认证
    function authenticateWithHongKongAndMacaoIDCard() {
        window.location.href = "mailto:support@gdeiassistant.cn?subject=香港澳门身份证件实名认证&body=请补全此模板邮件中的空缺信息，并通过附件上传香港澳门身份证基本身份信息面的扫描件或照片，一并发送到support@gdeiassistant.cn。此认证方式同时支持旧版和新版身份证。" +
            "你提供的信息将被严格保密，并在完成实名认证后的48小时内进行删除。%0d%0a%0d%0a用户名：${sessionScope.username}%0d%0a用户组：" + userGroupMap[${sessionScope.group-1}] + "%0d%0a姓名：%0d%0a地区：（请填写香港或澳门）%0d%0a证件号：";
    }

    //使用港澳居民来往内地通行证件联系客服认证
    function authenticateWithHongKongAndMacaoExitAndEntryPermit() {
        window.location.href = "mailto:support@gdeiassistant.cn?subject=港澳居民来往内地通行证件实名认证&body=请补全此模板邮件中的空缺信息，并通过附件上传港澳居民来往内地通行证基本身份信息面的扫描件或照片，一并发送到support@gdeiassistant.cn。" +
            "你提供的信息将被严格保密，并在完成实名认证后的48小时内进行删除。%0d%0a%0d%0a用户名：${sessionScope.username}%0d%0a用户组：" + userGroupMap[${sessionScope.group-1}] + "%0d%0a姓名：%0d%0a地区：（请填写香港或澳门）%0d%0a证件号：";
    }

    //使用台湾居民来往大陆通行证认证件联系客服认证
    function authenticateWithTaiwanExitAndEntryPermit() {
        window.location.href = "mailto:support@gdeiassistant.cn?subject=台湾居民来往大陆通行证件实名认证&body=请补全此模板邮件中的空缺信息，并通过附件上传台湾居民来往大陆通行证基本身份信息面的扫描件或照片，一并发送到support@gdeiassistant.cn。" +
            "你提供的信息将被严格保密，并在完成实名认证后的48小时内进行删除。%0d%0a%0d%0a用户名：${sessionScope.username}%0d%0a用户组：" + userGroupMap[${sessionScope.group-1}] + "%0d%0a姓名：%0d%0a证件号：";
    }

    //使用护照证件联系客服认证
    function authenticateWithPassport() {
        $.alert({
            text: '当前认证方式仅限于非中国大陆护照证件进行认证，否则你的认证申请可能会遭到拒绝。中国大陆护照证件请通过大陆居民认证的其他大陆证件类型认证方式进行认证。',
            title: '注意事项',
            onOK: function () {
                window.location.href = "mailto:support@gdeiassistant.cn?subject=海外居民护照证件实名认证&body=请补全此模板邮件中的空缺信息，并通过附件上传海外护照基本身份信息页和入境盖章页的扫描件或照片，一并发送到support@gdeiassistant.cn。" +
                    "你提供的信息将被严格保密，并在完成实名认证后的48小时内进行删除。%0d%0a%0d%0a用户名：${sessionScope.username}%0d%0a用户组：" + userGroupMap[${sessionScope.group-1}] + "%0d%0a姓名：%0d%0a国籍（地区）：%0d%0a证件号：";
            }
        });
    }

    //申请摄像头权限并获取视频流
    function getVideoMedia(reload) {
        if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
            navigator.mediaDevices.getUserMedia({video: {facingMode: "environment"}}).then(function (stream) {
                //添加playsinline和controls标记以自动播放视频
                $("#camera")[0].srcObject = stream;
                $("#camera")[0].setAttribute("playsinline", true);
                $("#camera")[0].setAttribute("controls", true);
                setTimeout(() => {
                    //自动播放视频后移除播放控件
                    $("#camera")[0].removeAttribute("controls");
                });
                if (!reload) {
                    $("#cameraPopup").popup();
                }
            }, function () {
                $.toptip('用户拒绝了摄像头权限或你的浏览器不支持相关API', 'error');
            });
            return true;
        } else {
            $.toptip('用户拒绝了摄像头权限或你的浏览器不支持相关API', 'error');
        }
        return false;
    }

    //用户点击拍照按钮，从视频流中截取图片
    function capturePhoto() {
        //从视频流截取图片
        let canvas = document.createElement('canvas');
        let context = canvas.getContext('2d');
        context.canvas.width = $("#camera").width();
        context.canvas.height = $("#camera").height();
        if (!$("#camera")[0].paused) {
            $("#camera")[0].pause();
        }
        context.drawImage($("#camera")[0], 0, 0, $("#camera").width(), $("#camera").height());
        let base64 = canvas.toDataURL('image/jpeg', 0.8);
        authenticateWithIdentityImage(base64);
    }

    //上传身份证照片进行认证
    function authenticateWithIdentityImage(base64) {
        base64 = base64.split(',')[1];
        base64 = window.atob(base64);
        let ia = new Uint8Array(base64.length);
        for (let i = 0; i < base64.length; i++) {
            ia[i] = base64.charCodeAt(i);
        }
        let blob = new Blob([ia], {type: "image/jpg"});
        let formData = new FormData();
        formData.append('image', blob);
        formData.append('method', 1);
        $("#popupLoadingToast, .weui_mask").show();
        $(".weui_btn").attr("disabled", true);
        //上传身份证图片到服务器
        $.ajax({
            url: "/api/authentication",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            success: function (result) {
                $("#popupLoadingToast, .weui_mask").hide();
                if (result.success) {
                    $.closePopup();
                    $.toptip('实名认证成功', 'success');
                    loadAuthenticationData();
                } else {
                    $.toptip(result.message, 'error');
                    $(".weui_btn").attr("disabled", false);
                    getVideoMedia(true);
                }
            },
            error: function () {
                $("#popupLoadingToast, .weui_mask").hide();
                $(".weui_btn").attr("disabled", false);
                if (result.status) {
                    $.toptip(result.responseJSON.message, 'error');
                } else {
                    $.toptip('网络连接超时，请重试', 'error');
                }
                getVideoMedia(true);
            }
        });
    }
</script>
