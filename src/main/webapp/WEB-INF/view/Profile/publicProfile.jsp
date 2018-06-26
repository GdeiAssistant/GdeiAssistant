<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/22
  Time: 02:43
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>${KickName}的个人资料</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/common.css">
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

        <c:if test="${Gender!=null and Gender>=1 and Gender<=3}">
            <div class="weui-cell">
                <div class="weui-cell__bd">
                    <p>性别</p>
                </div>
                <div class="weui-cell__ft">
                    <c:choose>
                        <c:when test="${Gender==1}">
                            男
                        </c:when>
                        <c:when test="${Gender==2}">
                            女
                        </c:when>
                        <c:otherwise>
                            其他
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <c:if test="${GenderOrientation!=null && GenderOrientation>=2 && GenderOrientation<=4}">

                <div class="weui-cell">
                    <div class="weui-cell__bd">
                        <p>性取向</p>
                    </div>
                    <div class="weui-cell__ft">
                        <c:choose>
                            <c:when test="${GenderOrientation==2}">
                                <c:choose>
                                    <c:when test="${Gender==1}">
                                        同性恋
                                    </c:when>
                                    <c:when test="${Gender==2}">
                                        异性恋
                                    </c:when>
                                    <c:otherwise>
                                        其他
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:when test="${GenderOrientation==3}">
                                <c:choose>
                                    <c:when test="${Gender==1}">
                                        异性恋
                                    </c:when>
                                    <c:when test="${Gender==2}">
                                        同性恋
                                    </c:when>
                                    <c:otherwise>
                                        其他
                                    </c:otherwise>
                                </c:choose>
                            </c:when>
                            <c:when test="${GenderOrientation==4}">
                                双性恋
                            </c:when>
                        </c:choose>
                    </div>
                </div>
            </c:if>
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
