<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="info" uri="/WEB-INF/tld/info.tld" %>
<script type="text/javascript" src="/js/info/info.js"></script>

<c:set var="festival" scope="request" value="${info:getFestivalInfo()}"/>

<div class="announcement" style="display: none">
    <div class="hd">
        <h1 class="page_title">通知公告</h1>
    </div>
    <article class="weui-article">
        <h2></h2>
        <h2></h2>
        <section>
            <!-- 通知公告内容 -->
        </section>
    </article>
</div>

<div class="wechataccount">
    <div class="hd">
        <h1 class="page_title">校园公众号</h1>
    </div>
    <div>
        <!-- 校园热门公众号列表 -->
        <c:forEach var="wechataccount" items="${info:getSchoolWechatAccountData()}" begin="0" end="2">
            <div class="weui-form-preview" style="margin-bottom: 10px">
                <div class="weui-form-preview__bd">
                    <div class="weui-form-preview__item" style="margin-bottom: 0.3rem">
                        <label class="weui-form-preview__label">头像</label>
                        <img src="${wechataccount.avatar}" style="width: 50px; height: auto">
                    </div>
                    <div class="weui-form-preview__item">
                        <label class="weui-form-preview__label">公众号</label>
                        <span class="weui-form-preview__value">${wechataccount.name}</span>
                    </div>
                    <div class="weui-form-preview__item">
                        <label class="weui-form-preview__label">功能介绍</label>
                        <span class="weui-form-preview__value">${wechataccount.description}</span>
                    </div>
                    <c:if test="${wechataccount.article==null or wechataccount.article!=''}">
                        <div class="weui-form-preview__item">
                            <label class="weui-form-preview__label">最近文章</label>
                            <span class="weui-form-preview__value">${wechataccount.article}</span>
                        </div>
                    </c:if>
                </div>
                <div class="weui-form-preview__ft">
                    <a class="weui-form-preview__btn weui-form-preview__btn_primary"
                       href="https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=${wechataccount.biz}&scene=123#wechat_redirect">查看</a>
                </div>
            </div>
        </c:forEach>
        <p style="text-align:center;margin-top:0.5rem;font-size:13px;color:#999"
           onclick="window.location.href='/wechataccount'">
            点击查看所有热门校园公众号
        </p>
        <br>
    </div>
</div>

<div class="recommendation" style="display: none">
    <div class="hd">
        <h1 class="page_title">专题阅读</h1>
    </div>
    <div class="weui-cells">
        <!-- 专题阅读内容 -->
    </div>
    <p style="text-align:center;margin-top:0.5rem;font-size:13px;color:#999" onclick="window.location.href='/reading'">
        点击查看所有专题阅读内容
    </p>
    <br>
</div>

<c:if test="${festival!=null}">

    <div class="today">
        <div class="hd">
            <h1 class="page_title">世界上的今日</h1>
        </div>
        <article class="weui-article">
            <jsp:useBean id="now" class="java.util.Date" scope="request"/>
            <h2>你知道吗？每年的<fmt:formatDate value="${now}" pattern="MM月dd日"/>，是${festival.name}</h2>
            <section>
                <c:forEach var="description" items="${festival.description}">
                    <p>${fn:trim(description)}</p>
                </c:forEach>
            </section>
        </article>
    </div>

</c:if>