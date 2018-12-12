<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/8
  Time: 14:52
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
<head>
    <title>广东第二师范学院二手交易</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-base.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/yxMobileSlider.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript">

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        $(function () {
            var ua = window.navigator.userAgent;
            if (!(/\(i[^;]+;( U;)? CPU.+Mac OS X/).test(ua) && ua.indexOf('Android') == -1 && ua.indexOf('Linux') == -1) {
                $('body').addClass('isPC');
            }
        });

    </script>
</head>

<body>

<!-- 顶部 -->
<section class="head">
    <a href="javascript:history.go(-1)" class="logo">
        <i class="i ihead"></i>
        <span class="t">广东第二师范学院二手交易</span>
    </a>
</section>

<!-- 二手交易商品详细信息 -->
<section class="detail">

    <!-- 滚动轮播图片 -->
    <div class="slider">
        <ul>
            <c:forEach var="ImageURL" items="${ErshouInfo.ershouItem.pictureURL}">
                <li class="img">
                    <img src="${ImageURL}" onload="window.checkimg(this)">
                </li>
            </c:forEach>
        </ul>
    </div>

    <script>
        var ua = window.navigator.userAgent;
        if (!(/\(i[^;]+;( U;)? CPU.+Mac OS X/).test(ua) && ua.indexOf('Android') === -1 && ua.indexOf('Linux') === -1) {
            $(".slider").yxMobileSlider({
                width: 480,
                height: 480,
                during: 3000
            });
        } else {
            $(".slider").yxMobileSlider({
                width: window.innerWidth,
                height: window.innerWidth,
                during: 3000
            });
        }
    </script>

    <!-- 二手交易商品基本信息 -->
    <div class="info">
        <em class="price"><b>￥</b>${ErshouInfo.ershouItem.price}</em>
        <h5 class="tit">${ErshouInfo.ershouItem.name}</h5>
        <p class="tm">发布时间：<b> <fmt:formatDate value="${ErshouInfo.ershouItem.publishTime}" pattern="yyyy-MM-dd HH:mm:ss"/></b></p>
    </div>

    <!-- 二手交易商品交易地点 -->
    <p class="site"><span><i class="i isite"></i>交易地点：</span>${ErshouInfo.ershouItem.location}</p>

</section>

<section class="userinfo">

    <!-- 用户信息 -->
    <a class="user" onclick="window.location.href='/profile/user/${ErshouInfo.profile.username}'">
        <i class="avt">
            <c:choose>
                <c:when test="${ErshouInfo.profile.avatarURL!=''}">
                    <img src="${ErshouInfo.profile.avatarURL}">
                </c:when>
                <c:otherwise>
                    <img src="/img/avatar/default.png">
                </c:otherwise>
            </c:choose>
        </i>
        <span class="nm">发布者：${ErshouInfo.profile.kickname}</span>
    </a>

    <!-- 二手交易商品交易描述 -->
    <div class="info">
        <i class="i iinfo"></i>
        <p class="w">商品描述：${ErshouInfo.ershouItem.description}</p>
    </div>

    <!-- 用户联系方式 -->
    <div class="contact">
        <i class="i icontact"></i>
        <p class="qq">qq：<b>${ErshouInfo.ershouItem.qq}</b></p>
        <c:if test="${ErshouInfo.ershouItem.phone!=null}">
            <p class="cont">
            <span class="phone">
                手机号：<a>${ErshouInfo.ershouItem.phone}</a>
            </span>
                <a href="tel:${ErshouInfo.ershouItem.phone}">打电话</a><a href="sms:${ErshouInfo.ershouItem.phone}">发短信</a>
            </p>
        </c:if>
    </div>

</section>

<section class="footer">
    <span>网络交易有风险，交易时请自行核实</span>
</section>

</body>
</html>
