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
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
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

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<input type="hidden" id="currentPage" value="1"/>
<input type="hidden" id="sumPage" value="1"/>
<input type="hidden" id="queriedBookName"/>

<div class="hd">
    <h1 class="page_title">馆藏图书查询</h1>
    <p class="page_desc">广东第二师范学院移动图书馆</p>
</div>

<!-- 查询中弹框 -->
<div class="weui_mask" style="display: none"></div>
<div id="loadingToast" class="weui_loading_toast" style="display: none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <div class="weui_loading">
            <div class="weui_loading_leaf weui_loading_leaf_0"></div>
            <div class="weui_loading_leaf weui_loading_leaf_1"></div>
            <div class="weui_loading_leaf weui_loading_leaf_2"></div>
            <div class="weui_loading_leaf weui_loading_leaf_3"></div>
            <div class="weui_loading_leaf weui_loading_leaf_4"></div>
            <div class="weui_loading_leaf weui_loading_leaf_5"></div>
            <div class="weui_loading_leaf weui_loading_leaf_6"></div>
            <div class="weui_loading_leaf weui_loading_leaf_7"></div>
            <div class="weui_loading_leaf weui_loading_leaf_8"></div>
            <div class="weui_loading_leaf weui_loading_leaf_9"></div>
            <div class="weui_loading_leaf weui_loading_leaf_10"></div>
            <div class="weui_loading_leaf weui_loading_leaf_11"></div>
        </div>
        <p class="weui_toast_content">查询中</p>
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
            <div class="weui_cells weui_cells_form">
                <form>
                    <div class="weui_cell">
                        <div class="weui_cell_hd">
                            <label class="weui_label">书名</label>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <input id="bookname" class="weui_input" name="bookname" placeholder="请输入图书信息"
                                   value="${BookName}">
                        </div>
                    </div>
                </form>
            </div>

            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
            </div>

            <p class="page_desc" href="javascript:"
               style="margin-top: 25px" onclick="openAdvancedSearchPage()">高级检索</p>

            <!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
            <div class="weui_toptips weui_warn js_tooltips"></div>

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
