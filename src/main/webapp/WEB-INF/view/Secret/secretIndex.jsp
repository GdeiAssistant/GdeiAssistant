<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>广东第二师范学院树洞</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0,  minimum-scale=1.0, maximum-scale=1.0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link rel="stylesheet" href="/css/secret/secret-index.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>
        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });
    </script>
</head>
<body>
<header>
    <a href="/secret/publish" class="pub"><i class="publish"></i>说个小秘密</a>
    <a href="/secret/profile" class="msg">
    </a>
</header>

<!-- 树洞信息列表 -->
<div id="list">

</div>

<footer id="loadmore" style="text-align: center; line-height: 3rem; color:#999;margin-top:-1rem; font-size:1rem;"
        onclick="loadSecretInfo()">点击加载更多
</footer>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
<script type="text/javascript">

    $(function () {
        loadSecretInfo();
    });

    var hasMore = true;

    $(".secret").height($(".secret").width() * 0.8);

    //更改点赞状态
    $("body").on("click", ".pregood", function () {
        $e = $(this);
        $.post("/api/secret/id/" + $(this).closest(".secret").attr("id") + "/like", {"like": "1"}, function (data) {
            if (data.success === true) {
                $e.removeClass("pregood").addClass("good").next("span").text(parseInt($e.next("span").text()) + 1);
            }
        }, "json");
    }).on("click", ".good", function () {
        $e = $(this);
        $.post("/api/secret/id/" + $(this).closest(".secret").attr("id") + "/like", {"like": "0"}, function (data) {
            if (data.success === true) {
                $e.removeClass("good").addClass("pregood").next("span").text(parseInt($e.next("span").text()) - 1);
            }
        }, "json");
    });

    //查看树洞详细信息
    function showSecretDetailInfo(id) {
        sessionStorage.setItem('size', $("#list").children("div").length);
        sessionStorage.setItem('anchor', id);
        window.location.href = '/secret/detail/id/' + id;
    }

    //加载树洞信息
    function loadSecretInfo() {

        if (hasMore === true) {

            var loading = weui.loading('加载中');

            var url;

            var size = sessionStorage.getItem('size');

            if (size) {
                url = "/api/secret/info/start/" + $("#list").children("div").length + "/size/" + size;
                sessionStorage.removeItem('size');
            } else {
                url = "/api/secret/info/start/" + $("#list").children("div").length + "/size/10";
            }

            $.get(url, function (result) {

                    loading.hide();

                    if (result.success === true) {

                        if (size) {
                            sessionStorage.removeItem('size');
                        }

                        if (result.data !== null || result.data.length === 0) {

                            for (var i = 0; i < result.data.length; i++) {

                                var liked = 'pregood';
                                if (result.data[i].liked === 1) {
                                    liked = 'good';
                                }

                                if (result.data[i].type === 0) {
                                    $("#list").append("<div class='secret theme" + result.data[i].theme + "' id='" + result.data[i].id + "'>" +
                                        "<a id='" + result.data[i].id + "' href='javascript:;' onclick='showSecretDetailInfo(" + result.data[i].id + ")'>" +
                                        "<section>" + result.data[i].content + "</section>" + "</a>" +
                                        "<footer><div><i class='" + liked + "'></i>" + "<span>"
                                        + result.data[i].likeCount + "</span>" + "</div>"
                                        + "<a href='/secret/detail/id/" + result.data[i].id + "'>" +
                                        "<div><i class='comment'></i>" + "<span>" + result.data[i].commentCount + "</span></div></a>" + "</footer>" +
                                        "</div>");
                                } else if (result.data[i].type === 1 || result.data[i].type === 2) {
                                    if (result.data[i].theme === 1) {
                                        $("#list").append("<div class='secret theme" + result.data[i].theme + "' id='" + result.data[i].id + "'>" +
                                            "<a id='" + result.data[i].id + "' href='javascript:;' onclick='showSecretDetailInfo(" + result.data[i].id + ")'>" +
                                            "<section><img id='record' width='50px' height='50px' src='/img/secret/voice_normal_white.png'></section>" + "</a>" +
                                            "<footer><div><i class='" + liked + "'></i>" + "<span>"
                                            + result.data[i].likeCount + "</span>" + "</div>"
                                            + "<a href='/secret/detail/id/" + result.data[i].id + "'>" +
                                            "<div><i class='comment'></i>" + "<span>" + result.data[i].commentCount + "</span></div></a>" + "</footer>" +
                                            "</div>");
                                    } else {
                                        $("#list").append("<div class='secret theme" + result.data[i].theme + "' id='" + result.data[i].id + "'>" +
                                            "<a id='" + result.data[i].id + "' href='javascript:;' onclick='showSecretDetailInfo(" + result.data[i].id + ")'>" +
                                            "<section><img id='record' width='50px' height='50px' src='/img/secret/voice_normal.png'></section>" + "</a>" +
                                            "<footer><div><i class='" + liked + "'></i>" + "<span>"
                                            + result.data[i].likeCount + "</span>" + "</div>"
                                            + "<a href='/secret/detail/id/" + result.data[i].id + "'>" +
                                            "<div><i class='comment'></i>" + "<span>" + result.data[i].commentCount + "</span></div></a>" + "</footer>" +
                                            "</div>");
                                    }
                                }
                            }
                        } else {
                            $("#loadmore").text("没有更多信息");
                            hasMore = false;
                        }
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }

                    //若用户查看过树洞详细消息并返回，跳转到瞄点的位置
                    var anchor = sessionStorage.getItem('anchor');
                    if (anchor) {
                        window.location.href = "#" + anchor;
                        sessionStorage.removeItem('anchor');
                    }
                }
            );
        }
    }

</script>

</body>
</html>
