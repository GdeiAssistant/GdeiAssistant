<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="zh-CN">
<head>
    <title>拍好校园</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link rel="stylesheet" href="/css/common/amazeui.min.css">
    <link rel="stylesheet" href="/css/photograph/index.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/amazeui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery.scrollTo.min.js"></script>
    <script type="text/javascript" src="/js/common/swiper.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/photograph/index.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<!-- 数据统计 -->
<div class="am-g" id="statistics">
    <div class="am-u-sm-4">
        <span id="statistics-photos">0</span>
        <span>照片总数</span>
    </div>
    <div class="am-u-sm-4">
        <span id="statistics-comments">0</span>
        <span>评论总数</span>
    </div>
    <div class="am-u-sm-4">
        <span id="statistics-likes">0</span>
        <span>点赞总数</span>
    </div>
</div>

<!-- 页面属性-->
<input type="hidden" id="type" value="1"/>
<input type="hidden" id="start" value="0"/>

<!-- 信息列表 -->
<div id="card-box">

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

<!-- 加载更多 -->
<p id="loadmore" style="text-align: center; font-size: 14px; color: #777" onclick="loadPhotographs()">点击查看更多</p>

<!-- 提示信息 -->
<p style="text-align: center; font-size: 14px; color: red">平台禁止色情、盗图等侵权违规行为
    <br>若有发现请点击<a href="javascript:" onclick="window.location.href='mailto:report@gdeiassistant.cn'">此处举报</a></p>

<br><br><br>

<!-- 底部菜单 -->
<footer id="toolbar">
    <div class="am-btn-group am-btn-group-justify bottom-btns" style="box-shadow: 0 -1px 5px #989898;">
        <a class="am-btn am-btn-danger" role="button" href="javascript:"
           onclick="switchPhoto(1)">
            <img width="20px" height="20px" src="/img/photograph/life.png"/><br>最美生活照</a>
        <a class="am-btn am-btn-primary" role="button" href="javascript:"
           onclick="switchPhoto(2)">
            <img width="20px" height="20px" src="/img/photograph/school.png"/><br>最美校园照</a>
        <a class="am-btn am-btn-warning" role="button" href="javascript:"
           onclick="switchPhoto(3)">
            <img width="20px" height="20px" src="/img/photograph/graduation.png"/><br>最美毕业照</a>
        <a class="am-btn am-btn-success" role="button" href="javascript:"
           onclick="window.location.href='/photograph/upload'">
            <img width="20px" height="20px" src="/img/photograph/upload.png"/><br>我要晒照</a>
    </div>
</footer>

<!-- 添加评论弹窗 -->
<div class="am-modal am-modal-prompt" tabindex="-1" id="comment-modal">
    <div class="am-modal-dialog">
        <div class="am-modal-hd">评论</div>
        <div class="am-modal-bd">
            <label for="comment-say">说点什么</label>
            <input type="text" class="am-modal-prompt-input" maxlength="50" id="comment-say">
        </div>
        <div class="am-modal-footer">
            <span class="am-modal-btn" data-am-modal-cancel>取消</span>
            <span class="am-modal-btn" data-am-modal-confirm>提交</span>
        </div>
    </div>
</div>

</body>
</html>