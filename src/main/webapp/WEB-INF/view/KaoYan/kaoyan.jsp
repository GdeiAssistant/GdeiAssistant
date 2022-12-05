<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>硕士初试成绩查询</title>
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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/kaoyan/kaoyan.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">考研成绩查询</h1>
</div>

<!-- 用户填写信息页面 -->
<div id="input">

    <!-- 提交的查询信息表单 -->
    <div class="weui-cells weui-cells_form">
        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">姓名</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="name" class="weui-input" type="text" maxlength="15" name="name"
                       placeholder="请输入姓名">
            </div>
        </div>
        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">考号</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="examNumber" class="weui-input" type="number" maxlength="15"
                       name="examNumber" pattern="[0-9]*" placeholder="请输入准考证号">
            </div>
        </div>
        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">证件号</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="idNumber" class="weui-input" type="text" maxlength="18"
                       name="idNumber" placeholder="请输入证件号码">
            </div>
        </div>
    </div>

    <!-- 提交按钮 -->
    <div class="weui-btn_area">
        <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

    <br><br>

    <div class="weui-cells__title">
        备用查询入口
    </div>
    <div class="weui-cells">
        <a class="weui-cell weui-cell_access" href="javascript:"
           onclick="window.location.href = 'https://yz.chsi.com.cn/apply/cjcx/'">
            <div class="weui-cell__bd">
                <p>研招网硕士初试成绩查询</p>
            </div>
            <div class="weui-cell__ft">
            </div>
        </a>
    </div>

</div>

<!-- 查询结果界面 -->
<div id="result" style="display: none">
    <div class="weui-cells__title">考生信息</div>
    <div class="weui-cells">
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>姓名</p>
            </div>
            <div id="result_name" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>报名号</p>
            </div>
            <div id="result_signUpNumber" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>准考证号</p>
            </div>
            <div id="result_examNumber" class="weui-cell__ft"></div>
        </div>
    </div>

    <div class="weui-cells__title">成绩信息</div>
    <div class="weui-cells">
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>总分</p>
            </div>
            <div id="result_totalScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第一门</p>
            </div>
            <div id="result_firstScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第二门</p>
            </div>
            <div id="result_secondScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第三门</p>
            </div>
            <div id="result_thirdScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第四门</p>
            </div>
            <div id="result_fourthScore" class="weui-cell__ft"></div>
        </div>
    </div>

    <br><br>

</body>
</html>
