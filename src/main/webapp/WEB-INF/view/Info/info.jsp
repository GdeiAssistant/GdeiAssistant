<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="info" uri="/WEB-INF/tld/info.tld" %>
<script type="text/javascript" src="/js/info/info.js"></script>

<c:set var="festival" scope="request" value="${info:getFestivalInfo()}"/>

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

<div class="recommendation">
    <div class="hd">
        <h1 class="page_title">专题阅读</h1>
    </div>
    <div class="weui-cells">
        <!-- 专题阅读内容 -->
    </div>
</div>