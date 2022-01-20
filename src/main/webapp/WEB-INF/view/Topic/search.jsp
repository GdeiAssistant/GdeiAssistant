<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>话题</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link rel="stylesheet" href="/css/topic/base.css">
    <link rel="stylesheet" href="/css/topic/cards.css">
    <link rel="stylesheet" href="/css/topic/app.css">
    <link rel="stylesheet" href="/css/topic/profile.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/swiper.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script type="text/javascript" src="/js/topic/search.js"></script>
</head>
<body>

<div class="weui-tab">

    <div class="weui-tab__panel">

        <div class="hd">
            <h1 class="page_title">搜索话题</h1>
        </div>

        <div id="search">

            <div class="weui_cells weui_cells_form">
                <form>
                    <div class="weui_cell">
                        <div class="weui_cell_bd weui_cell_primary">
                            <input id="keyword" class="weui_input" type="text" maxlength="10" name="keyword"
                                   placeholder="请输入话题关键词">
                        </div>
                    </div>
                </form>
            </div>

            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:"
                   onclick="queryTopicKeyword()">查询</a>
            </div>

        </div>

        <div id="result" style="display: none">

            <div id="data">

            </div>

            <p id="loadmore"
               style="text-align: center; font-size: 14px; color: #777;margin-top: 15px;margin-bottom: 35px"
               onclick="loadTopicData()">点击查看更多</p>

        </div>

    </div>

    <!-- 加载中弹框 -->
    <div class="weui_mask" style="display: none"></div>
    <div id="loadingToast" class="weui_loading_toast" style="display: none">
        <div class="weui_mask_transparent"></div>
        <div class="weui_toast">
            <div class="weui_loading">
                <div class="weui_loading_leaf weui_loading_leaf_0"></div>
                <div class="weui_loading_leaf weui_loading_leaf_1"></div>
                <div class="weui_loading_leaf weui_loading_leaf_2"></div>
                <div class="weui_loading_leaf weui_loading_leaf_3"></div>
                <div class="weui_loading_leaf weui_loading_leaf_4"></div>
                <div class="weui_loading_leaf weui_loading_leaf_5"></div>
                <div class="weui_loading_leaf weui_loading_leaf_6"></div>
                <div class="weui_loading_leaf weui_loading_leaf_7"></div>
                <div class="weui_loading_leaf weui_loading_leaf_8"></div>
                <div class="weui_loading_leaf weui_loading_leaf_9"></div>
                <div class="weui_loading_leaf weui_loading_leaf_10"></div>
                <div class="weui_loading_leaf weui_loading_leaf_11"></div>
            </div>
            <p class="weui_toast_content">加载中</p>
        </div>
    </div>

    <div class="weui-tabbar">
        <a href="javascript:" onclick="window.location.href='/topic'" class="weui-tabbar__item weui-bar__item">
            <img src="/img/topic/home.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">首页</p>
        </a>
        <a href="javascript:" onclick="window.location.href='/topic/publish'" class="weui-tabbar__item">
            <img src="/img/topic/publish.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">发布</p>
        </a>
        <a href="javascript:" onclick="window.location.href='/topic/search'"
           class="weui-tabbar__item weui-bar__item_on">
            <img src="/img/topic/search.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">搜索</p>
        </a>
    </div>
</div>

</body>
</html>