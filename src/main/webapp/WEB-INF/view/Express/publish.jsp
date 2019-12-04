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
    <script>document.write("<script type='text/javascript' src='/js/express/publish.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>

<body>

<div id="Header" class="Header" data-role="header">
    <h1>广东第二师范学院表白墙</h1>
</div>

<!-- 信息填写框 -->
<div class="main-body" id="saylove-box" data-role="content">

    <img id="loading" style="display:none;position: fixed;top: 50%;left: 45%" src="/img/express/ajax-loader.gif"
         width="50px" height="auto"/>

    <!-- 表白者信息 -->
    <fieldset>
        <legend>你的信息</legend>
        <label for="nickname">你的昵称</label>
        <input type="text" name="nickname" id="nickname" value="" maxlength="10">
        <label for="realname">你的真名</label>
        <input type="text" name="realname" id="realname" value="" maxlength="10">
        <p class="guess-hint">
            注：真实姓名为可选填写项，默认保密不显示！填写即可参加紧张刺激的猜名字游戏！
        </p>
        <select class="gender-Type" id="self_gender" name="type">
            <option value="0" selected>男</option>
            <option value="1">女</option>
            <option value="2">其他或保密</option>
        </select>
    </fieldset>

    <!-- 被表白者信息 -->
    <fieldset>
        <legend>TA的信息</legend>
        <label for="name">TA的名字、选择性别</label>
        <input type="text" name="name" id="name" value="" maxlength="10">
        <select class="itsGender-Type" id="person_gender" name="type">
            <option value="0" selected>男</option>
            <option value="1">女</option>
            <option value="2">其他或保密</option>
        </select>

    </fieldset>

    <!-- 表白内容 -->
    <label for="content" style=" text-align: left; clear: both ">表白的内容</label>
    <textarea style="height: 250px" name="content" id="content" onkeyup="inputLengthCheck(this,250)"></textarea>
    <p id="textCountBox">
        剩余<span id="textCount">250</span>字
    </p>

    <!-- 提交按钮 -->
    <a href="javascript:" data-rel="popup" id="submit" class="ui-btn ui-btn-inline ui-corner-all"
       data-position-to="window" data-transition="pop" onclick="publishExpress()">提交</a>

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

<!-- 底部按钮 -->
<div data-role="footer" id="footer" data-position="fixed" data-fullscreen="true" data-tap-toggle="false">
    <div class="" data-role="navbar">
        <ul>
            <li><a href="/express" data-ajax='false' data-role="button" data-icon="article" class="ui-icon-article"
                   data-iconpos="notext">首页</a></li>
            <li><a href="/express/publish" data-ajax='false' data-transition="slide" data-role="button"
                   data-icon="heartNow"
                   class="ui-icon-heart" data-iconpos="notext">表白</a></li>
            <li><a href="/express/search" data-ajax='false' data-role="button" data-icon="search" class="ui-icon-search"
                   data-iconpos="notext">搜索</a></li>
        </ul>
    </div>

</div>

</body>
</html>
