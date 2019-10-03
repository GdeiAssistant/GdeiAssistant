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
    <title>广东第二师范学院失物招领</title>
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/search.css">
    <script type="text/javascript" src="/js/lostandfound/search.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<form action="/lostandfound/search" method="post" onsubmit="return checkLostTypeCheckedState()">
    <div class="input">
        <input type="text" name="keywords" placeholder="搜索时请选择寻物或寻主" autofocus>
    </div>
    <div class="radios">
        <div class="radio">
            <label class="radio1" onclick="selectLost()">
                <input type="radio" name="type" value="1" hidden><img src="/img/lostandfound/unlost.png">寻物
            </label>
        </div>
        <div class="radio">
            <label class="radio2" onclick="selectFound()">
                <input type="radio" name="type" value="0" hidden><img src="/img/lostandfound/unfound.png">寻主
            </label>
        </div>
    </div>
    <div class="buttons">
        <div class="button cancel">
            <button type="button" onclick="history.back()">取消</button>
        </div>
        <div class="button ok">
            <button type="submit">确定</button>
        </div>
    </div>
</form>

</body>
</html>
