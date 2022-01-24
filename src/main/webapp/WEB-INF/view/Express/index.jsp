<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>表白墙</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link type="text/css" rel="stylesheet" href="/css/common/jquery.mobile.min.css">
    <link type='text/css' rel="stylesheet" href="/css/express/homepage.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-1.12.4.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery.mobile.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/layermobile.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/express/index.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>

<body>

<!-- 页头的信息 -->
<div id="Header" class="Header" data-role="header">
    <h1>广东第二师范学院表白墙</h1>
</div>

<!-- 主体 -->
<div class="main-body" id="main" data-role="content">

    <!-- 错误提示 -->
    <div class="layui-m-layer layui-m-layer0" style="display: none">
        <div class="layui-m-layermain">
            <div class="layui-m-layersection">
                <div class="layui-m-layerchild layui-m-layer-msg  layui-m-anim-up">
                    <div id="warning" class="layui-m-layercont"></div>
                </div>
            </div>
        </div>
    </div>

</div>

<!-- 加载更多 -->
<section id="loadmore" class="loadmore" onclick="loadExpress()">
    <span>点击加载更多</span>
</section>

<!-- 颜色提示信息 -->
<p style="text-align:center;color:#9e9e9e;font-size:12px;">
    蓝色下划线：男生 / 红色下划线：女生 / 黑色下划线：其他或保密
</p>

<!-- 提示信息 -->
<p style="text-align:center;color:#9e9e9e;font-size:12px;">
    交友需谨慎，请注意保护个人隐私。
</p>
<p style="text-align:center;color:#9e9e9e;font-size:12px;">
    抵制粗俗语言，共创文明网络环境。
</p>

<!-- 底部菜单 -->
<div data-role="footer" id="footer" data-position="fixed" data-fullscreen="true" data-tap-toggle="false">
    <div class="ui-navbar" data-role="navbar" role="navigation">
        <ul>
            <li><a href="/express" data-ajax='false' data-role="button" data-icon="articleNow" class="ui-icon-article"
                   data-iconpos="notext">首页</a></li>
            <li><a href="/express/publish" data-ajax='false' data-transition="slide" data-role="button"
                   data-icon="heart"
                   class="ui-icon-heart" data-iconpos="notext">表白</a></li>
            <li><a href="/express/search" data-ajax='false' data-role="button" data-icon="search" class="ui-icon-search"
                   data-iconpos="notext">搜索</a></li>
        </ul>
    </div>
</div>

<!-- 猜名字模块 -->
<div data-role="popup" class="ui-content" data-overlay-theme="b" id="guess-Name-Popup">
    <a href="#" data-rel="back"
       class="ui-btn ui-corner-all ui-shadow ui-btn ui-icon-delete ui-btn-icon-notext ui-btn-right">关闭</a>
    <h4>猜名字</h4>
    <p>
        已猜中 <span id="guess_right"></span> 次, 被猜 <span id="guess_all"></span> 次.
    </p>
    <div class="ui-field-contain">
        <label for="guess-input">猜一猜发起者的名字：</label>
        <input type="search" name="search" id="guess-input" placeholder="名字">
    </div>
    <input id="guess-submit" style="text-align:center;display:block;margin:0 auto;" type="submit" data-inline="true"
           value="猜！">
    <span id="guess-hint"></span>
</div>

<!-- 评论列表和发表评论模块 -->
<div data-role="popup" class="ui-content" data-overlay-theme="b" id="comment-Popup">
    <a href="#" data-rel="back"
       class="ui-btn ui-corner-all ui-shadow ui-btn ui-icon-delete ui-btn-icon-notext ui-btn-right">关闭</a>
    <h4>评论列表</h4>
    <div class="" id="comment-lists">
        <ul id="comment-lists-ul">

        </ul>
    </div>
    <div class="ui-field-contain">
        <label for="guess-input">评论：</label>
        <input type="search" name="search" id="guess-input" placeholder="我想说...">
    </div>
    <input id="comment-submit" style="text-align:center;display:block;margin:0 auto;" type="submit" data-inline="true"
           value="评论">
    <span id="comment-hint"></span>
</div>

</body>

</html>