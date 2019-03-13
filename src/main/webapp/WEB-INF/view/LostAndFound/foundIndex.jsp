<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/base.css">
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/nav.css">
    <title>广东第二师范学院失物招领</title>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript" src="/js/lostandfound/found.js"></script>
</head>
<body>

<!-- 搜索栏 -->
<section class="search" onclick="window.location.href='/lostandfound/search/index'">
    <i class="i"></i>
    <i class="txt">
        <form>
            <input id="keyword_input" type="text" placeholder="搜搜看" disabled>
        </form>
    </i>
</section>

<!-- 常用搜索词汇 -->
<section class="menus">
    <ul>
        <li class="menu"><a href="/lostandfound/foundinfo/type/0"><i class="i iphone"></i><span
                class="t">手机</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/1"><i class="i icard"></i><span
                class="t">校园卡</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/2"><i class="i iidcard"></i><span
                class="t">身份证</span></a>
        </li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/3"><i class="i ibank"></i><span
                class="t">银行卡</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/4"><i class="i ibook"></i><span class="t">书</span></a>
        </li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/5"><i class="i ikey"></i><span class="t">钥匙</span></a>
        </li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/6"><i class="i ibag"></i><span class="t">包包</span></a>
        </li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/7"><i class="i icloth"></i><span
                class="t">衣帽</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/8"><i class="i ibicycle"></i><span
                class="t">校园代步</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/9"><i class="i isport"></i><span
                class="t">运动健身</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/10"><i class="i iparts"></i><span
                class="t">数码配件</span></a></li>
        <li class="menu"><a href="/lostandfound/foundinfo/type/11"><i class="i iother"></i><span
                class="t">其他</span></a></li>
    </ul>
</section>

<!-- 失物招领物品列表 -->
<section class="lists">

</section>

<!-- 加载更多 -->
<section id="loadmore" class="footer" onclick="loadMoreInfo()">
    <span>点击加载更多</span>
</section>

<div style="height: 4rem;"></div>

<!-- 底部Tab栏 -->
<nav class="main-nav">
    <div>
        <a href="/lostandfound/lost"><i class="ibar index">
            <img src="/img/lostandfound/lost_normal.png"></i>失物</a>
    </div>
    <div>
        <a class="on" href="/lostandfound/found"><i class="ibar ipublish">
            <img src="/img/lostandfound/found_selected.png"></i>招领</a>
    </div>
    <div class="publish">
        <div class="pubbtn">
            <a href="/lostandfound/publish">
                <div class="inner">
                </div>
            </a>
        </div>
    </div>
    <div>
        <a href="/lostandfound/search/index"><i class="ibar ipublish"><img
                src="/img/lostandfound/search.png"></i>搜索</a>
    </div>
    <div>
        <a href="/lostandfound/personal"><i class="ibar iprofile"><img src="/img/lostandfound/personal_normal.png"></i>个人中心</a>
    </div>
</nav>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
