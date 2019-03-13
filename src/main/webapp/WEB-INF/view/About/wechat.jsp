<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <meta content="telephone=no" name="format-detection">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <title>微信消息对话查询使用说明</title>
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
</head>
<body>

<div style="max-width: 677px;margin-left: auto;margin-right: auto">
    <p style="text-align:center">《广东二师助手微信消息对话查询使用说明》</p>
    <p style="text-align:center">更新日期：2019年2月14日</p>
    <p>&nbsp; &nbsp; &nbsp; &nbsp; 易小助微信公众号提供了用户消息对话进行教务查询的功能。用户在微信公众号绑定教务系统账号后，可以通过发送消息快捷查询成绩信息、课表信息和校园卡信息等。</p>
    <p style="text-align: center;">
        <img style="width: 100% !important; height: auto !important; visibility: visible !important;"
             src="/img/about/wechat_message.jpg" onclick="window.location.href='/img/about/wechat_message.jpg'">
    </p>
    <p><span style="color: rgb(255, 0, 0);">【绑定账号/更改绑定账号】</span></p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用户使用微信公众号消息对话查询功能，首先需要进行教务系统账号绑定。账号绑定后，用户可以随时进行绑定账号更改。一个微信账号仅可以绑定一个教务系统账号，但一个教务系统账号可以被多个微信账号进行绑定。若你有多个微信账号，你可以都将其绑定到同一教务系统账号上，方便你的查询和使用。</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;账号绑定/更改绑定账号方法：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用户向微信公众号发送如下格式的文本消息：绑定账号-用户名-密码。例：绑定账号-lisiyi-123456</p>
    <p><span style="color: rgb(255, 0, 0);">【成绩信息查询】</span></p>
    <p><span style="color: rgb(255, 0, 0);">&nbsp;&nbsp;&nbsp;&nbsp;</span>&nbsp;&nbsp;&nbsp;&nbsp;成绩查询功能允许用户查询当前学年学期的成绩信息。若当前学年学期没有成绩信息，则会查询上一学年或学期的成绩信息。
    </p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;成绩信息查询方法：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用户绑定账号后，向微信公众号发送“成绩”，即可查询成绩信息。</p>
    <p><span style="color: rgb(255, 0, 0);">【课表信息查询】</span></p>
    <p>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;课表查询功能允许用户查询今天的课表信息。系统将智能识别今天的周数、所属星期数，剔除、筛选出教务系统上有效的课表信息进行显示。<br>
    </p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;课表信息查询方法：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用户绑定账号后，向微信公众号发送“课表”，即可查询课表信息。</p>
    <p><span style="color: rgb(255, 0, 0);">【校园卡信息查询】</span></p>
    <p>
        &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;校园卡信息查询功能允许用户查询当前校园卡的姓名、学号、卡号等基本信息，余额、过渡余额等余额信息以及冻结状态、挂失状态等状态信息。</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;校园卡信息查询方法：</p>
    <p>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;用户绑定账号后，向微信公众号发送“校园卡”，即可查询校园卡信息。</p>
</div>

</body>
</html>
