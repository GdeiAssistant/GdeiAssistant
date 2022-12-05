<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>馆藏图书查询</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <!-- 不缓存页面 -->
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
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
    <script type="text/javascript" src="/js/book/collection.js"></script>
</head>
<body>

<%-- 不缓存页面 --%>
<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Cache-Control", "must-revalidate");
%>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<input type="hidden" id="currentPage" value="1"/>
<input type="hidden" id="sumPage" value="1"/>
<input type="hidden" id="queriedBookName"/>

<div class="hd">
    <h1 class="page_title">馆藏图书查询</h1>
    <p class="page_desc">广东第二师范学院移动图书馆</p>
</div>

<!-- 查询中弹框 -->
<div role="alert" id="loadingToast" style="display: none;">
    <div class="weui-mask_transparent"></div>
    <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast">
              <span class="weui-primary-loading__dot"></span>
            </span>
        <p class="weui-toast__content">查询中</p>
    </div>
</div>

<%-- 如果查询失败,显示错误信息,否则显示馆藏信息 --%>
<c:choose>

    <%-- 出现查询错误或者用户初次进入页面显示编辑界面 --%>
    <c:when test="${CollectionList==null}">

        <div id="edit">

            <script>

                $(document).ready(function () {
                    sessionStorage.removeItem("collectionList");
                    sessionStorage.removeItem("currentPage");
                    sessionStorage.removeItem("sumPage");
                    sessionStorage.removeItem("anchor");
                });

            </script>

            <!-- 提交的查询信息表单 -->
            <div class="weui-cells weui-cells_form">
                <form>
                    <div class="weui-cell">
                        <div class="weui-cell__hd">
                            <label class="weui-label">书名</label>
                        </div>
                        <div class="weui-cell__bd weui-cell_primary">
                            <input id="bookname" class="weui-input" name="bookname" placeholder="请输入图书信息"
                                   value="${BookName}">
                        </div>
                    </div>
                </form>
            </div>

            <!-- 提交按钮 -->
            <div class="weui-btn_area">
                <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
            </div>

            <p class="page_desc" href="javascript:"
               style="margin-top: 25px" onclick="openAdvancedSearchPage()">高级检索</p>

            <!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
            <div class="weui-toptips weui_warn js_tooltips"></div>

        </div>

        <%-- 有错误信息时进行显示 --%>
        <c:if test="${QueryErrorMessage!=null}">

            <script type="text/javascript">
                $(document).ready(function () {
                    $(".weui_warn").text("${QueryErrorMessage}").show().delay(2000).hide(0);
                });
            </script>

        </c:if>

    </c:when>

    <%-- 查询成功后显示馆藏信息 --%>
    <c:otherwise>

        <div id="cetQueryResultEnum">

            <script>

                $(document).ready(function () {
                    $("#queriedBookName").val("${BookName}");
                    $("#sumPage").val("${SumPage}");
                    if ($("#currentPage").val() === $("#sumPage").val()) {
                        //已经是最后一页
                        $("#bottomLine").text("别拉了,已经到底了").show();
                        $("#bottomLine").removeAttribute("onclick");
                    } else {
                        $("#bottomLine").text("点击加载更多信息").show();
                        $("#bottomLine").attr("onclick", "queryMore();");
                    }
                });

            </script>

            <div class="weui-cells__title">
                馆藏查询结果
            </div>

            <div class="weui-cells">

                    <%-- 显示馆藏信息 --%>
                <c:forEach var="collection" items="${CollectionList}">

                    <a id='${collection.detailURL}' class='weui-cell'
                       href='javascript:'
                       style='color: black'>
                        <div class='weui-cell__bd'>
                            <p>${collection.bookname}</p>
                            <c:if test="${collection.author!=null}">
                                <p style='font-size: 13px;color: #999'>${collection.author}</p>
                            </c:if>
                            <c:if test="${collection.publishingHouse!=null}">
                                <p style='font-size: 13px;color: #999'>${collection.publishingHouse}</p>
                            </c:if>
                        </div>
                    </a>

                    <script>
                        setDetailURL("${collection.detailURL}");
                    </script>

                </c:forEach>

            </div>

            <div id='bottomLine' style='display:none;text-align: center;color: #888;font-size: 14px;margin: 15px'>

            </div>

        </div>

    </c:otherwise>

</c:choose>

</body>
</html>
