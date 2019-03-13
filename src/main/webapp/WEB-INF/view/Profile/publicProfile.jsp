<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${KickName}的个人资料</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
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

        .kickname {
            text-align: center
        }
    </style>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

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

<div class="kickname">
    <h2 class="weui-msg__title">${KickName}</h2>
</div>

<c:if test="${ContainProfile!=null and ContainProfile==true}">

    <div class="weui-cells__title">个人资料</div>

    <div class="weui-cells">

        <c:if test="${Gender!=null}">

            <div class="weui-cell">
                <div class="weui-cell__bd">
                    <p>性别</p>
                </div>
                <div class="weui-cell__ft">
                        ${Gender}
                </div>
            </div>

        </c:if>

        <c:if test="${GenderOrientation!=null}">

            <div class="weui-cell">
                <div class="weui-cell__bd">
                    <p>性取向</p>
                </div>
                <div class="weui-cell__ft">
                        ${GenderOrientation}
                </div>
            </div>

        </c:if>

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

        <c:if test="${Location!=null}">

            <div class="weui-cell">
                <div class="weui-cell__bd">
                    <p>所在地</p>
                </div>
                <div class="weui-cell__ft">${Location}</div>
            </div>

        </c:if>

    </div>

    <br>

</c:if>

<c:if test="${Introduction!=null}">

    <div class="weui-cells__title">个人简介</div>

    <div class="weui-cells">
        <div class="weui-cell">
            <div class="weui-cell__bd">${Introduction}</div>
        </div>
    </div>

</c:if>

</body>
</html>
