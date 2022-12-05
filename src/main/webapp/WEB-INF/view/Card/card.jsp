<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>消费查询</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/card/card.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">饭卡消费查询</h1>
</div>

<div id="input">

    <!-- 当前选择的查询日期 -->
    <p class="page_desc" style="margin-top: 25px;font-size:18px">当前选中日期:未选择</p>

    <!-- 查询的日期参数  -->
    <input type="hidden" name="year" id="year" value=""/>
    <input type="hidden" name="month" id="month" value=""/>
    <input type="hidden" name="date" id="date" value=""/>

    <!-- 确认查询按钮 -->
    <div class="weui-btn_area">
        <a onclick="showDatePicker()" class="weui-btn weui-btn_default" href="javascript:">选择查询日期</a>
        <a onclick="postQueryForm()" class="weui-btn weui-btn_primary" href="javascript:">确认查询</a>
    </div>

</div>

<div id="result" style="display: none;">

    <!-- 当前查询的日期 -->
    <div id="currentDate" class="weui-cells__title">

    </div>

    <!-- 没有消费记录时显示该提示 -->
    <div id="errorTip" class="weui-msg">
        <div class="weui-msg__icon-area"><i class="weui-icon-warn weui-icon_msg"></i></div>
        <div class="weui-msg__text-area">
            <h2 class="weui-msg__title">没有消费记录</h2>
            <p class="weui-msg__desc">饭堂阿姨想念你了</p>
        </div>
    </div>

    <!-- 消费记录列表 -->
    <div id="cardQueryResult" class="weui-cells">

    </div>

    <div class="weui-msg__opr-area">
        <p class="weui-btn-area">
            <a onclick="switchToPickerPage()" href="javascript:" class="weui-btn weui-btn_primary">重新查询</a>
        </p>
    </div>

</div>

</body>
</html>
