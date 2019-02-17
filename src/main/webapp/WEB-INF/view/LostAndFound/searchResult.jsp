<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>广东第二师范学院失物招领</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/base.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript">

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        var $first = 10;

        window.checkimg = function (obj) {
            if (obj.width > obj.height) {
                obj.style.width = 'auto';
                obj.style.height = '100%';
            }
        };

        $(function () {
            var ua = window.navigator.userAgent;
            if (!(/\(i[^;]+;( U;)? CPU.+Mac OS X/).test(ua) && ua.indexOf('Android') == -1 && ua.indexOf('Linux') == -1) {
                $('body').addClass('isPC');
            }
            $('body').css('minHeight', window.innerHeight);
        });

        //显示错误提示
        function showErrorTip(message) {
            $(".weui_warn").text(message).show().delay(2000).hide(0);
        }

        //滚动到底部时加载更多数据
        $(function () {
            $(window).scroll(function () {
                if ($(window).scrollTop() + $(window).height() >= $("body").height() - 40 && $(".lis").length === $first) {
                    loadLostAndFoundInfo($(".lis").length);
                }
            });
        });

        //加载更多数据
        function loadLostAndFoundInfo(start) {
            $.ajax({
                url: '/api/lostandfound/lostitem/type/' + $("#type").val() + '/start/' + start,
                type: 'post',
                data: {
                    "keyword": $("#keyword").val()
                },
                success: function (result) {
                    if (result.success === true) {
                        $first = $first + 10;
                        if (result.data !== null) {
                            for (var i = 0; i < result.data.length; i++) {
                                $(".lis").append("<div class='li'>" +
                                    "<a href='/lostandfound/detail/id/" + result.data[i].id + "'>" +
                                    "<i class='img'>" + "<img id='" + result.data[i].id + "' onload='window.checkimg(this)'>" +
                                    "</i>" + "<h5 class='tit'>" + result.data[i].name + "</h5>" +
                                    "<p class='site'><i class='i isite'></i>" + result.data[i].location + "</p>" +
                                    "</a>" + "</div>");
                                $(".lis").append("<script>getPreviewPicture(result.data[i].id)<\/script>");
                            }
                        }
                    }
                    else {
                        showErrorTip(result.message);
                    }
                },
                error: function () {
                    showErrorTip("网络异常，请检查网络连接");
                }
            });
        }

        //获取预览图片
        function getPreviewPicture(id) {
            $.ajax({
                url: '/api/lostandfound/item/id/' + id + '/preview',
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

<input type="hidden" id="keyword" name="keyword" value="${KeyWord}"/>

<input type="hidden" id="type" name="type" value="${LostType}"/>

<body class="searchpage">

<section class="search">
    <i class="i"></i>
    <i class="txt">
        <form onclick="window.location.href='/lostandfound/search/index'">
            <input id="keyword_input" type="text" value="${KeyWord}">
        </form>
    </i>
</section>

<section class="lis">
    <c:if test="${LostAndFoundItemList!=null}">
        <c:forEach var="LostAndFoundItem" items="${LostAndFoundItemList}">
            <div class="li">
                <a href="/lostandfound/detail/id/${LostAndFoundItem.id}">
                    <i class="img">
                        <img id="${LostAndFoundItem.id}" onload="window.checkimg(this)">
                    </i>
                    <h5 class="tit">${LostAndFoundItem.name}</h5>
                    <p class="site"><i class="i isite"></i>${LostAndFoundItem.location}</p>
                </a>
            </div>
            <script>
                getPreviewPicture(${LostAndFoundItem.id});
            </script>
        </c:forEach>
    </c:if>
</section>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>