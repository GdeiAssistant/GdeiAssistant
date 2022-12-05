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
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/grade/grade.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/grade/grade.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div>

    <div class="weui-cells__title" style="float: left" onclick="history.go(-1)">返回</div>
    <div class="weui-cells__title" style="float: right" onclick="showOptionMenu()">更多</div>
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

</body>
</html>
