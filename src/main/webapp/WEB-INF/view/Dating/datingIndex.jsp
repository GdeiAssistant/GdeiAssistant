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

        var female_index = 0;
        var male_index = 0;

        var female_hasMore = true;
        var male_hasMore = true;

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        $(function () {
            loadMore(0);
            loadMore(1);
        });

        function switchArea(index) {
            switch (index) {
                case 0:
                    $("li.female").attr("class", "female selected");
                    $("li.male").attr("class", "male");
                    $("div.cards.female").attr("class", "cards female active");
                    $("div.cards.male").attr("class", "cards male");
                    break;

                case 1:
                    $("li.male").attr("class", "male selected");
                    $("li.female").attr("class", "female");
                    $("div.cards.male").attr("class", "cards male active");
                    $("div.cards.female").attr("class", "cards female");
                    break;
            }
        }

        function showDetailInfo(id) {
            window.location.href = '/dating/profile/id/' + id;
        }

        function loadMore(index) {
            var hasMore;
            switch (index) {
                case 0:
                    hasMore = female_hasMore;
                    break;

                case 1:
                    hasMore = male_hasMore;
                    break;
            }
            if (hasMore) {
                var url;
                switch (index) {
                    case 0:
                        url = '/dating/profile/area/' + 0 + '/start/' + female_index;
                        break;

                    case 1:
                        url = '/dating/profile/area/' + 1 + '/start/' + male_index;
                        break;
                }
                var loading = weui.loading('加载中');
                $.ajax({
                    url: url,
                    method: 'get',
                    success: function (result) {
                        loading.hide();
                        if (result.success) {
                            if (result.data.length != 0) {
                                for (var i = 0; i < result.data.length; i++) {
                                    switch (result.data[i].grade) {
                                        case 1:
                                            $(".cardsBox").eq(index).append("<li onclick='showDetailInfo(" + result.data[i].profileId + ")'><div class='Peopleimg'>" +
                                                "<a class='click' href='javascript:'><img id='" + result.data[i].profileId + "' /></a></div>" +
                                                "<h1 class='P-title'><a class='click' href='javascript:'></a></h1>" +
                                                "<div class='p-info'>" + result.data[i].faculty
                                                + " 大一学生</div>" + "<div class='p-info'>来自" + result.data[i].hometown + "</div></li>");
                                            $(".cardsBox").append("<script>getPicture(" + result.data[i].profileId + ") <\/script>");
                                            break;

                                        case 2:
                                            $(".cardsBox").eq(index).append("<li onclick='showDetailInfo(" + result.data[i].profileId + ")'><div class='Peopleimg'>" +
                                            "<a class='click' href='javascript:'><img id='" + result.data[i].profileId + "' /></a></div>" +
                                            "<h1 class='P-title'><a class='click' href='javascript:'></a></h1>" +
                                            "<div class='p-info'>" + result.data[i].faculty
                                            + " 大二学生</div>" + "<div class='p-info'>来自" + result.data[i].hometown + "</div></li>");
                                            $(".cardsBox").append("<script>getPicture(" + result.data[i].profileId + ") <\/script>");
                                            break;

                                        case 3:
                                            $(".cardsBox").eq(index).append("<li onclick='showDetailInfo(" + result.data[i].profileId + ")'><div class='Peopleimg'>" +
                                                "<a class='click' href='javascript:'><img id='" + result.data[i].profileId + "' /></a></div>" +
                                                "<h1 class='P-title'><a class='click' href='javascript:'></a></h1>" +
                                                "<div class='p-info'>" + result.data[i].faculty
                                                + " 大三学生</div>" + "<div class='p-info'>来自" + result.data[i].hometown + "</div></li>");
                                            $(".cardsBox").append("<script>getPicture(" + result.data[i].profileId + ") <\/script>");
                                            break;

                                        case 4:
                                            $(".cardsBox").eq(index).append("<li onclick='showDetailInfo(" + result.data[i].profileId + ")'><div class='Peopleimg'>" +
                                                "<a class='click' href='javascript:'><img id='" + result.data[i].profileId + "' /></a></div>" +
                                                "<h1 class='P-title'><a class='click' href='javascript:'></a></h1>" +
                                                "<div class='p-info'>" + result.data[i].faculty
                                                + " 大四学生</div>" + "<div class='p-info'>来自" + result.data[i].hometown + "</div></li>");
                                            $(".cardsBox").append("<script>getPicture(" + result.data[i].profileId + ") <\/script>");
                                            break;
                                    }
                                }
                                switch (index) {
                                    case 0:
                                        female_index = female_index + result.data.length;
                                        break;

                                    case 1:
                                        male_index = male_index + result.data.length;
                                        break;
                                }
                            }
                            else {
                                switch (index) {
                                    case 0:
                                        female_hasMore = false;
                                        $("#loadMore_female").text("已经到底了");
                                        break;

                                    case 1:
                                        male_hasMore = false;
                                        $("#loadMore_male").text("已经到底了");
                                        break;
                                }
                            }
                        }
                        else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function () {
                        loading.hide();
                        $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                    }
                });
            }
        }

        function getPicture(id) {
            $.ajax({
                url: '/dating/profile/id/' + id + '/picture',
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        $("#" + id).attr("src", result.data);
                    }
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
        <div class="account">
            <div class="loginWin">
                <a href="/dating/message">
                    <c:choose>
                        <c:when test="${hasUnReadMessage==null}">
                            <img src="/img/dating/message.png" style="width: 25px;height: 25px;margin-top: 1rem">
                        </c:when>
                        <c:otherwise>
                            <img src="/img/dating/unread_message.png" style="width: 30px;height: 30px;margin-top: 1rem">
                        </c:otherwise>
                    </c:choose>
                </a>
            </div>
        </div>
    </div>

    <div class="content">
        <div id="banner">
            <div class="pagination"></div>
        </div>
        <div class="sexTab">
            <ul>
                <li class="female selected"><a href="javascript:" onclick="switchArea(0)">小姐姐</a></li>
                <li class="male"><a href="javascript:" onclick="switchArea(1)">小哥哥</a></li>
            </ul>
        </div>
        <div class="sexWrapper">
            <div class="cards male">
                <ul class="cardsBox"></ul>
                <section style="clear: both;text-align: center">
                    <span id="loadMore_female" onclick="loadMore(0)">点击加载更多</span>
                </section>
            </div>
            <div class="cards female active">
                <ul class="cardsBox"></ul>
                <section style="clear: both;text-align: center">
                    <span id="loadMore_male" onclick="loadMore(1)">点击加载更多</span>
                </section>
            </div>
        </div>
    </div>
</div>

<div class="skypub" onclick="window.location.href='/dating/publish'">
    <i></i>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
