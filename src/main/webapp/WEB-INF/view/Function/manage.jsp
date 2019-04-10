<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>功能管理</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/function/manage.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">功能管理</h1>
    <p class="page_desc">注意：功能入口配置将被保存至浏览器本地</p>
</div>

<div class="weui-cells__title">打开/关闭首页的功能入口</div>
<div class="weui-cells weui-cells_form">
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">成绩查询</div>
        <div class="weui-cell__ft">
            <input id="grade" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">课表查询</div>
        <div class="weui-cell__ft">
            <input id="schedule" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">四六级查询</div>
        <div class="weui-cell__ft">
            <input id="cet" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">馆藏查询</div>
        <div class="weui-cell__ft">
            <input id="collection" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">我的图书馆</div>
        <div class="weui-cell__ft">
            <input id="book" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">消费查询</div>
        <div class="weui-cell__ft">
            <input id="card" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">我的饭卡</div>
        <div class="weui-cell__ft">
            <input id="cardinfo" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">教学评价</div>
        <div class="weui-cell__ft">
            <input id="evaluate" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">教室查询</div>
        <div class="weui-cell__ft">
            <input id="spare" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">考研查询</div>
        <div class="weui-cell__ft">
            <input id="kaoyan" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">信息查询</div>
        <div class="weui-cell__ft">
            <input id="data" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">体测查询</div>
        <div class="weui-cell__ft">
            <input id="tice" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">新闻通知</div>
        <div class="weui-cell__ft">
            <input id="news" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">二手交易</div>
        <div class="weui-cell__ft">
            <input id="ershou" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">失物招领</div>
        <div class="weui-cell__ft">
            <input id="lostandfound" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">校园树洞</div>
        <div class="weui-cell__ft">
            <input id="secret" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">全民快递</div>
        <div class="weui-cell__ft">
            <input id="delivery" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">学期校历</div>
        <div class="weui-cell__ft">
            <input id="calendar" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">政务服务</div>
        <div class="weui-cell__ft">
            <input id="government" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">志愿活动</div>
        <div class="weui-cell__ft">
            <input id="volunteer" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">绑定微信</div>
        <div class="weui-cell__ft">
            <input id="wechat" class="weui-switch" type="checkbox">
        </div>
    </div>
    <div class="weui-cell weui-cell_switch">
        <div class="weui-cell__bd">绑定易班</div>
        <div class="weui-cell__ft">
            <input id="yiban" class="weui-switch" type="checkbox">
        </div>
    </div>
</div>

<!-- 重置按钮 -->
<div class="weui_btn_area">
    <a id="submit" class="weui_btn weui_btn_warn" href="javascript:" onclick="resetFunctionDisplaySetting()">重置</a>
</div>

<br>

</body>
</html>
