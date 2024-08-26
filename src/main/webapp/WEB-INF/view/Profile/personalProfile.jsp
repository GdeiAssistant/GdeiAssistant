<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${Username}的个人资料</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>
        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });
    </script>
    <style>
        .avatar {
            width: 100%;
            height: 100%;
            border-radius: 50px;
        }

        .avatarDiv {
            width: 100px;
            height: 100px;
            margin: 25px auto
        }

        .username {
            text-align: center
        }
    </style>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div class="avatarDiv">
    <c:choose>
        <c:when test="${AvatarURL!=null and AvatarURL!=''}">
            <img class="avatar" src="${AvatarURL}">
        </c:when>
        <c:otherwise>
            <img class="avatar" src="/img/avatar/default.png">
        </c:otherwise>
    </c:choose>
</div>

<div class="username">
    <h2 class="weui-msg__title">${Username}</h2>
</div>

<div class="weui-cells__title">个人资料</div>

<div class="weui-cells">

    <div class="weui-cell">
        <div class="weui-cell__bd">
            <p>昵称</p>
        </div>
        <div class="weui-cell__ft">
            ${NickName}
        </div>
    </div>

    <div class="weui-cell">
        <div class="weui-cell__bd">
            <p>IP属地</p>
        </div>
        <div class="weui-cell__ft">
            ${IPAddressArea}
        </div>
    </div>

    <c:if test="${Faculty!=null}">

        <div class="weui-cell">
            <div class="weui-cell__bd">
                <p>院系</p>
            </div>
            <div class="weui-cell__ft">
                    ${Faculty}
            </div>
        </div>

    </c:if>

    <c:if test="${Major!=null}">

        <div class="weui-cell">
            <div class="weui-cell__bd">
                <p>专业</p>
            </div>
            <div class="weui-cell__ft">
                    ${Major}
            </div>
        </div>

    </c:if>

    <c:if test="${Enrollment!=null}">

        <div class="weui-cell">
            <div class="weui-cell__bd">
                <p>入学年份</p>
            </div>
            <div class="weui-cell__ft">
                    ${Enrollment}
            </div>
        </div>

    </c:if>

    <c:if test="${Age!=null}">

        <div class="weui-cell">
            <div class="weui-cell__bd">
                <p>年龄</p>
            </div>
            <div class="weui-cell__ft">
                    ${Age}
            </div>
        </div>

    </c:if>

    <c:if test="${Location!=null}">

        <div class="weui-cell">
            <div class="weui-cell__bd">
                <p>所在地</p>
            </div>
            <div class="weui-cell__ft">${Location}</div>
        </div>

    </c:if>

    <c:if test="${Hometown!=null}">

        <div class="weui-cell">
            <div class="weui-cell__bd">
                <p>家乡</p>
            </div>
            <div class="weui-cell__ft">${Hometown}</div>
        </div>

    </c:if>
</div>

<br>

<c:if test="${Introduction!=null}">

    <div class="weui-cells__title">个人简介</div>

    <div class="weui-cells">
        <div class="weui-cell">
            <div class="weui-cell__bd">${Introduction}</div>
        </div>
    </div>

</c:if>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/profile/user/${Username}'">
        <div class="weui-cell__bd">
            <p>查看个人资料页访客视角</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<br>

</body>
</html>