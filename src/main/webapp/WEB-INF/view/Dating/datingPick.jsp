<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/dating/global.css">
    <link rel="stylesheet" href="/css/dating/layout.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        //接受撩一下请求
        function acceptPick() {
            $.ajax({
                url: '/dating/pick/id/${DatingPick.pickId}',
                method: 'post',
                data: {
                    state: 1
                },
                success: function (result) {
                    if (result.success) {
                        window.location.reload();
                    }
                    else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function () {
                    $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                }
            });
        }

        //拒绝撩一下请求
        function rejectPick() {
            $.ajax({
                url: '/dating/pick/id/${DatingPick.pickId}',
                method: 'post',
                data: {
                    state: -1
                },
                success: function (result) {
                    if (result.success) {
                        window.location.reload();
                    }
                    else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function () {
                    $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                }
            });
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
            <p class="record-t"><i class="iconfont"></i>撩一下记录</p>
            <div class='MessageBox'>
                <div class='yuan' style='top: -5.5px; left: -5.5px;'></div>
                <div class='yuan' style='top: -5.5px; right: -5.5px;'></div>
                <div class='movieName'>
                    用户${DatingPick.username}撩了你一下
                </div>
                <div class='Message'>对方留言：${DatingPick.content}</div>
            </div>
        </div>

        <div class="action">
            <div style="background-color:white;color:black"
                 onclick="window.location.href='/profile/user/${DatingPick.username}'">
                查看对方个人资料
            </div>
            <c:choose>
                <c:when test="${DatingPick.state==0}">
                    <br>
                    <div style="background-color:limegreen;" onclick="acceptPick()">接受</div>
                    <br>
                    <div style="background-color:red;" onclick="rejectPick()">拒绝</div>
                </c:when>
                <c:otherwise>
                    <div style="color:black">你已处理该撩一下请求</div>
                </c:otherwise>
            </c:choose>
        </div>

    </div>

</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
