<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>成绩查询</title>
    <meta charset="UTF-8">
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
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/grade/grade.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/grade/grade_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/grade/grade_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/grade/grade.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div>

    <div class="weui_cells_title" style="float: left" onclick="history.go(-1)">返回</div>
    <div class="weui_cells_title" style="float: right" onclick="showOptionMenu()">更多</div>
    <div class="hd">
        <h1 class="page_title" style="clear:both;margin-top: 35px">我的成绩单</h1>
    </div>

    <div class="outter">
        <div class="navbar">
            <div id="one" onclick="postQueryForm(0)">大一</div>
            <div id="two" onclick="postQueryForm(1)">大二</div>
            <div id="three" onclick="postQueryForm(2)">大三</div>
            <div id="four" onclick="postQueryForm(3)">大四</div>
        </div>
    </div>

    <div class="grades">
        <div class="term" style="padding-top: 15px">第一学期</div>
        <p class="page_desc"></p>
        <div class="table">
            <table>
                <thead>
                <tr>
                    <th width="65%">课程</th>
                    <th>学分</th>
                    <th>成绩</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
        <div class="term">第二学期</div>
        <p class="page_desc"></p>
        <div class="table">
            <table>
                <thead>
                <tr>
                    <th width="65%">课程</th>
                    <th>学分</th>
                    <th>成绩</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>

</div>

<!-- 更新弹框 -->
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
        <p class="weui_toast_content">数据加载中</p>
    </div>
</div>

<!-- 更新成功弹框 -->
<div id="toast" style="display:none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <i class="weui_icon_toast"></i>
        <p class="weui_toast_content">加载成绩成功</p>
    </div>
</div>

</body>
</html>
