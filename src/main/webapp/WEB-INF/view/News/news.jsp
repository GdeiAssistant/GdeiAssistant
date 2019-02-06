<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>新闻通知</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript" src="/js/news/news.js"></script>
</head>
<body>

<div class="weui-tab">

    <div class="weui-tab__panel">

        <div class="weui_cells_title" onclick="history.go(-1)">返回</div>

        <div class="hd">
            <h1 class="page_title">新闻通知</h1>
        </div>

        <div style="margin-top: 0;" class="weui-cells">

        </div>

        <p style="text-align: center;color: lightgrey;margin-top: 1rem;margin-bottom: 2rem" id="loadMore">点击加载更多新闻通知</p>

    </div>

    <div class="weui-tabbar">
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/course.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">教学信息</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/examine.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">考试信息</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/study.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">教务信息</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/admin.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">行政通知</p>
        </a>
        <a href="javascript:" class="weui-tabbar__item">
            <img src="/img/news/school.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">综合信息</p>
        </a>
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

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
