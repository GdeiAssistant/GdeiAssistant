<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>广东第二师范学院二手交易</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-base.css">
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
                    loadErshouInfo($(".lis").length);
                }
            });
        });

        //加载更多数据
        function loadErshouInfo(start) {
            $.ajax({
                url: '/ershou/keyword/' + $("#keyword").val() + '/start' + start,
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        $first = $first + 10;
                        if (result.data !== null) {
                            for (var i = 0; i < result.data.length; i++) {
                                $(".lis").append("<div class='li'>" +
                                    "<a href='/ershou/detail/id/" + result.data[i].id + "'>" +
                                    "<i class='img'>" + "<img id='" + result.data[i].id + "' onload='window.checkimg(this)'>" +
                                    "</i>" + "<h5 class='tit'>" + result.data[i].name + "</h5>" +
                                    "<p class='site'><i class='i isite'></i>" + result.data[i].location + "</p>" +
                                    "<em class='price'>￥" + result.data[i].price + "</em>" + "</a>" + "</div>");
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
                url: '/ershou/item/id/' + id + '/preview',
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        $("#" + id).attr("src", result.data);
                    }
                }
            });
        }

        //关键字搜索
        function keywordSearch() {
            if ($("#keyword_input").val() === '') {
                showErrorTip("搜索关键字不能为空");
            }
            else {
                window.location.href = '/ershou/search/keyword/' + $("#keyword_input").val();
            }
        }

    </script>
</head>

<input type="hidden" id="keyword" name="keyword" value="${KeyWord}"/>

<%-- 如果首次查询失败，显示错误提示 --%>
<c:if test="${ErrorMessage!=null}">
    <script>
        showErrorTip("${ErrorMessage}");
    </script>
</c:if>

<body class="searchpage">

<section class="search">
    <i class="i"></i>
    <i class="txt">
        <form onkeydown="if(event.keyCode===13){keywordSearch();return false;}">
            <input id="keyword_input" type="text" value="${KeyWord}">
            <button type="button" onclick="keywordSearch()">搜索</button>
        </form>
    </i>
</section>

<section class="lis">
    <c:if test="${ErshouItemList!=null}">
        <c:forEach var="ErshouItem" items="${ErshouItemList}">
            <div class="li">
                <a href="/ershou/detail/id/${ErshouItem.id}">
                    <i class="img">
                        <img id="${ErshouItem.id}" onload="window.checkimg(this)">
                    </i>
                    <h5 class="tit">${ErshouItem.name}</h5>
                    <p class="site"><i class="i isite"></i>${ErshouItem.location}</p>
                    <em class="price">￥${ErshouItem.price}</em>
                </a>
            </div>
            <script>
                getPreviewPicture(${ErshouItem.id});
            </script>
        </c:forEach>
    </c:if>
</section>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>