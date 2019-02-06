<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>${NewDetail.title}</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript" src="/js/common/yiban.js"></script>
    <jsp:include page="/js/news/newDetail.jsp"/>
</head>
<body>

<div>
    <div class="weui_cells_title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">${NewDetail.title}</h1>
    </div>
</div>

${NewDetail.content}

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
