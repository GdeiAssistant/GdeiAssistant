<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/18
  Time: 16:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>广东第二师范学院树洞</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0,  minimum-scale=1.0, maximum-scale=1.0">
    <link rel="stylesheet" href="/css/secret/secret-index.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
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
    <c:if test="${SecretList!=null}">
        <c:forEach var="Secret" items="${SecretList}">
            <div class="secret theme${Secret.theme}" id="${Secret.id}">
                <a href="/secret/detail/id/${Secret.id}">
                    <section>${Secret.content}</section>
                </a>
                <footer>
                    <div>
                        <c:choose>
                            <c:when test="${Secret.liked==1}">
                                <i class="good"></i>
                            </c:when>
                            <c:otherwise>
                                <i class="pregood"></i>
                            </c:otherwise>
                        </c:choose>
                        <span>${Secret.likeCount}</span>
                    </div>
                    <a href="/secret/detail/id/${Secret.id}">
                        <div><i class="comment"></i><span>${Secret.commentCount}</span></div>
                    </a>
                </footer>
            </div>
        </c:forEach>
    </c:if>
</div>

<footer id="loadmore" style="text-align: center; line-height: 3rem; color:#999;margin-top:-1rem; font-size:1rem;"
        onclick="loadMoreSecretInfo()">点击加载更多
</footer>

<script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
<script type="text/javascript">

    var hasMore = true;

    //适配屏幕大小
    var height = $(".secret").width() * 0.8;
    var width = $(".secret").width();
    var style = "<style>.secret{height:" + height + "px;}" +
        ".secret section{width: " + width + "px; height: " + (height - 42) + "px;}" +
        "</style>";
    $("body").append(style);

    //更改点赞状态
    $("body").on("click", ".pregood", function () {
        $e = $(this);
        $.post("/rest/secret/id/" + $(this).closest(".secret").attr("id") + "/like", {"like": "1"}, function (data) {
            if (data.success === true) {
                $e.removeClass("pregood").addClass("good").next("span").text(parseInt($e.next("span").text()) + 1);
            }
        }, "json");
    }).on("click", ".good", function () {
        $e = $(this);
        $.post("/rest/secret/id/" + $(this).closest(".secret").attr("id") + "/like", {"like": "0"}, function (data) {
            if (data.success === true) {
                $e.removeClass("good").addClass("pregood").next("span").text(parseInt($e.next("span").text()) - 1);
            }
        }, "json");
    });

    //加载更多树洞信息
    function loadMoreSecretInfo() {

        if (hasMore === true) {

            var loading = weui.loading('加载中');

            $.get("/rest/secret/info/start/" + $("#list").children("div").length, function (result) {

                    loading.hide();

                    if (result.success === true) {

                        if (result.data !== null) {

                            for (var i = 0; i < result.data.length; i++) {

                                var liked = 'pregood';
                                if (result.data[i].liked === 1) {
                                    liked = 'good';
                                }

                                $("#list").append("<div class='secret theme" + result.data[i].theme + "' id='" + result.data[i].id + "'>" +
                                    "<a href='/secret/detail/id/" + result.data[i].id + "'>" +
                                    "<section>" + result.data[i].content + "</section>" + "</a>" +
                                    "<footer><div><i class='" + liked + "'></i>" + "<span>"
                                    + result.data[i].likeCount + "</span>" + "</div>"
                                    + "<a href='/secret/detail/id" + result.data[i].id + "'>" +
                                    "<div><i class='comment'></i>" + "<span>" + result.data[i].commentCount + "</span></div></a>" + "</footer>" +
                                    "</div>");
                            }
                        }
                        else {
                            $("#loadmore").text("没有更多信息");
                            hasMore = false;
                        }
                    }
                }
            );
        }
    }

</script>

</body>
</html>
