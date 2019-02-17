<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-base.css">
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-nav.css">
    <title>广东第二师范学院二手交易</title>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript">

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        var hasMore = true;

        // 这个图片展示规则是宽大于高按高100%截宽，高大于宽按宽100%截高；默认是宽100%截高
        window.checkimg = function (obj) {
            if (obj.width > obj.height) {
                obj.style.width = 'auto';
                obj.style.height = '100%';
            }
        };

        //显示错误提示
        function showErrorTip(message) {
            $(".weui_warn").text(message).show().delay(2000).hide(0);
        }

        $(function () {
            // 如果屏幕宽度小于375px，按320px宽的页面缩小
            if (window.innerWidth < 375) {
                $('.lists').addClass('forp4');
            }
            var ua = window.navigator.userAgent;
            if (!(/\(i[^;]+;( U;)? CPU.+Mac OS X/).test(ua) && ua.indexOf('Android') === -1 && ua.indexOf('Linux') === -1) {
                $('body').addClass('isPC');
            }
        });

        //加载更多数据
        function loadMoreErshouInfo() {
            if (hasMore === true) {
                loadErshouInfo($(".list").length);
            }
        }

        $(function () {
            loadErshouInfo(0);
        });

        //加载二手交易信息
        function loadErshouInfo(start) {
            var loading = weui.loading('加载中');
            $.ajax({
                url: '/ershou/item/start/' + start,
                type: 'get',
                success: function (result) {
                    loading.hide();
                    if (result.success === true) {
                        if (result.data !== null) {
                            for (var i = 0; i < result.data.length; i++) {
                                $(".lists").append("<div class='list'>" +
                                    "<a href='/ershou/detail/id/" + result.data[i].id + "'>" +
                                    "<i class='img'>" + "<img id='" + result.data[i].id + "' onload='window.checkimg(this)'>" +
                                    "</i>" + "<h5 class='tit'>" + result.data[i].name + "</h5>" +
                                    "<p class='summary'>" + result.data[i].description + "</p>" +
                                    "<em class='price'>￥" + result.data[i].price + "</em>" + "</a>" + "</div>");
                                $(".lists").append("<script>getPreviewPicture(" + result.data[i].id + ") <\/script>");
                            }
                        }
                        else {
                            $("#loadmore").text("没有更多信息");
                            hasMore = false;
                        }
                    }
                    else {
                        showErrorTip(result.message);
                    }
                },
                error: function () {
                    loading.hide();
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

<body>

<!-- 搜索栏 -->
<section class="search">
    <i class="i"></i>
    <i class="txt">
        <form onkeydown="if(event.keyCode===13){keywordSearch();return false;}">
            <input id="keyword_input" type="text" name="keyword" value="" placeholder="搜搜看" maxlength="25">
            <button type="button" onclick="keywordSearch()">搜索</button>
        </form>
    </i>
</section>

<!-- 常用搜索词汇 -->
<section class="menus">
    <ul>
        <li class="menu"><a href="/ershou/type/0"><i class="i ibicycle"></i><span
                class="t">校园代步</span></a></li>
        <li class="menu"><a href="/ershou/type/1"><i class="i iphone"></i><span class="t">手机</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/2"><i class="i ipc"></i><span class="t">电脑</span></a></li>
        <li class="menu"><a href="/ershou/type/3"><i class="i iparts"></i><span
                class="t">数码配件</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/4"><i class="i idigital"></i><span class="t">数码</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/5"><i class="i iappliances"></i><span
                class="t">电器</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/6"><i class="i isport"></i><span
                class="t">运动健身</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/7"><i class="i iclothes"></i><span
                class="t">衣物伞帽</span></a></li>
        <li class="menu"><a href="/ershou/type/8"><i class="i ibook"></i><span class="t">图书教材</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/9"><i class="i ilease"></i><span class="t">租赁</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/10"><i class="i ilife"></i><span class="t">生活娱乐</span></a>
        </li>
        <li class="menu"><a href="/ershou/type/11"><i class="i iother"></i><span class="t">其他</span></a>
        </li>
    </ul>
</section>

<!-- 二手交易商品列表 -->
<section class="lists">

</section>

<section id="loadmore" class="footer" onclick="loadMoreErshouInfo()">
    <span>点击加载更多</span>
</section>

<div style="height: 4rem;"></div>

<!-- 底部Tab栏 -->
<nav class="main-nav">
    <div><a class="on" href="/ershou"><i class="ibar index"><img src="/img/ershou/home_selected.png"></i>首页</a>
    </div>
    <div><a href="/ershou/publish"><i class="ibar ipublish"><img src="/img/ershou/publish.png"></i>发布</a>
    </div>
    <div><a href="/ershou/personal"><i class="ibar iprofile"><img src="/img/ershou/personal_normal.png"></i>个人中心</a>
    </div>
</nav>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>

</html>
