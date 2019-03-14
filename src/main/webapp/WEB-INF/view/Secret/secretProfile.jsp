<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>广东第二师范学院树洞</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0,  minimum-scale=1.0, maximum-scale=1.0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/secret/secret-profile.css">
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>
        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });
    </script>
</head>
<body>

<div class="notice"><i class="inotice"></i>我的树洞</div>

<!-- 发布的树洞消息列表 -->
<c:forEach var="Secret" items="${SecretList}">
    <div class="msg">
        <a href="/secret/detail/id/${Secret.id}">
            <p>${Secret.content}</p>
        </a>
        <i class="toggle"></i>
    </div>
</c:forEach>

<script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
<script type="text/javascript">

    $(".msg p").each(function () {
        $(this).css("margin-top", ("80" - $(this).height()) / 2);
    });
</script>

</body>
</html>