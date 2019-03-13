<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>广东第二师范学院卖室友</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/dating/global.css">
    <link rel="stylesheet" href="/css/dating/layout.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        var messageIndex = 0;

        var hasMore = true;

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        $(function () {
            loadMore();
        });

        function showProfile(profileId, messageId) {
            if (typeof (messageId) == "undefined") {
                window.location.href = '/dating/profile/id/' + profileId;
            } else {
                $.ajax({
                    url: '/dating/message/id/' + messageId + '/read',
                    method: 'post',
                    success: function (result) {
                        if (result.success) {
                            window.location.href = '/dating/profile/id/' + profileId;
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function () {
                        $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                    }
                })
            }
        }

        function showPick(pickId, messageId) {
            if (typeof (messageId) == "undefined") {
                window.location.href = '/dating/pick/id/' + pickId;
            } else {
                $.ajax({
                    url: '/dating/message/id/' + messageId + '/read',
                    method: 'post',
                    success: function (result) {
                        if (result.success) {
                            window.location.href = '/dating/pick/id/' + pickId;
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function () {
                        $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                    }
                })
            }
        }

        function loadMore() {
            if (hasMore) {
                var loading = weui.loading('加载中');
                $.ajax({
                    url: '/dating/message/start/' + messageIndex,
                    method: 'get',
                    success: function (result) {
                        loading.hide();
                        if (result.success) {
                            if (result.data.length == 0) {
                                hasMore = false;
                                $("#loadMore").text("没有更多消息了");
                            } else {
                                messageIndex = messageIndex + result.data.length;
                                for (var i = 0; i < result.data.length; i++) {
                                    if (result.data[i].type == 0) {
                                        //撩一下通知
                                        if (result.data[i].state == 0) {
                                            $(".record").eq(0).append("<div onclick='showPick(" + result.data[i].datingPick.pickId + "," + result.data[i].messageId + ")' class='MessageBox'><div class='yuan' style='top: -5.5px; left: -5.5px;'></div>" +
                                                "<div class='yuan' style='top: -5.5px; right: -5.5px;'></div><div class='movieName'>[未读]被撩通知" + "</div>" +
                                                "<div class='Message'>你被用户" + result.data[i].datingPick.username + "撩了一下，点击查看</div></div>");
                                        } else {
                                            $(".record").eq(0).append("<div onclick='showPick(" + result.data[i].datingPick.pickId + ")' class='MessageBox'><div class='yuan' style='top: -5.5px; left: -5.5px;'></div>" +
                                                "<div class='yuan' style='top: -5.5px; right: -5.5px;'></div><div class='movieName'>被撩通知" + "</div>" +
                                                "<div class='Message'>你被用户" + result.data[i].datingPick.username + "撩了一下，点击查看</div></div>");
                                        }
                                    } else {
                                        //撩一下处理通知
                                        if (result.data[i].datingPick.state == -1) {
                                            //被拒绝通知
                                            if (result.data[i].state == 0) {
                                                $(".record").eq(0).append("<div onclick='showProfile(" + result.data[i].datingPick.datingProfile.profileId + "," + result.data[i].messageId + ")' class='MessageBox'><div class='yuan' style='top: -5.5px; left: -5.5px;'></div>" +
                                                    "<div class='yuan' style='top: -5.5px; right: -5.5px;'></div><div class='movieName'>[未读]撩一下请求被拒绝</div>" +
                                                    "<div class='Message'>" + result.data[i].datingPick.datingProfile.kickname + "拒绝了你的撩一下请求</div></div>");
                                            } else {
                                                $(".record").eq(0).append("<div onclick='showProfile(" + result.data[i].datingPick.datingProfile.profileId + ")' class='MessageBox'><div class='yuan' style='top: -5.5px; left: -5.5px;'></div>" +
                                                    "<div class='yuan' style='top: -5.5px; right: -5.5px;'></div><div class='movieName'>撩一下请求被拒绝</div>"
                                                    + "<div class='Message'>" + result.data[i].datingPick.datingProfile.kickname + "拒绝了你的撩一下请求</div></div>");
                                            }
                                        } else {
                                            //被接受通知
                                            if (result.data[i].state == 0) {
                                                $(".record").eq(0).append("<div onclick='showProfile(" + result.data[i].datingPick.datingProfile.profileId + ")' class='MessageBox'><div class='yuan' style='top: -5.5px; left: -5.5px;'></div>" +
                                                    "<div class='yuan' style='top: -5.5px; right: -5.5px;'></div><div onclick='showProfile(" + result.data[i].datingPick.datingProfile.profileId + "," + result.data[i].messageId + ")' class='movieName'>[未读]撩一下请求被接受</div>" +
                                                    "<div class='Message'>" + result.data[i].datingPick.datingProfile.kickname + "接受了你的撩一下请求</div></div>");
                                            } else {
                                                $(".record").eq(0).append("<div onclick='showProfile(" + result.data[i].datingPick.datingProfile.profileId + ")' class='MessageBox'><div class='yuan' style='top: -5.5px; left: -5.5px;'></div>" +
                                                    "<div class='yuan' style='top: -5.5px; right: -5.5px;'></div><div onclick='showProfile(" + result.data[i].datingPick.datingProfile.profileId + ")' class='movieName'>撩一下请求被接受</div>" +
                                                    "<div class='Message'>" + result.data[i].datingPick.datingProfile.kickname + "接受了你的撩一下请求</div></div>");
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function () {
                        loading.hide();
                        $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                    }
                })
            }
        }
    </script>
</head>
<body>

<div class="panel-overlay"></div>

<div class="warp">

    <div class="head" onclick="window.location.href='/dating'">
        <div class="logo"><span class="t">卖室友</span></div>
    </div>

    <div class="content">

        <div class="record">
            <p class="record-t"><i class="iconfont"></i>卖室友消息</p>
        </div>

        <div style="text-align: center">
            <span id="loadMore" onclick="loadMore()">点击加载更多</span>
        </div>

    </div>

</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
