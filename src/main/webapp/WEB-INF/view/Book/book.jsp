<%--
  Created by IntelliJ IDEA.
  User: gdeiassistant
  Date: 2017/7/21
  Time: 23:20
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>图书借阅查询</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲�? -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <!-- 不缓存页�? -->
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        //返回主页
        function backToIndex() {
            window.location.href = '/index';
        }

    </script>
</head>
<body>

<%-- 不缓存页�? --%>
<%
    response.setHeader("Pragma", "No-cache");
    response.setHeader("Cache-Control", "no-cache");
    response.setDateHeader("Expires", 0);
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("Cache-Control", "must-revalidate");
%>

<%-- 获取错误信息并提�? --%>
<c:if test="${QueryErrorMessage!=null}">
    <script type="text/javascript">
        $(document).ready(function () {
            $(".weui_warn").text("${QueryErrorMessage}").show().delay(2000).hide(0);
        });
    </script>
</c:if>

<div class="weui_cells_title" onclick="backToIndex()">返回主页</div>

<%-- 如果有返回结�?,显示结果页面;否则显示用户填写信息的页�? --%>
<c:choose>

    <c:when test="${QueryResult!=null}">

        <h1 class="page_title" style="margin-top: 20px;margin-bottom: 10px">当前借阅图书</h1>

        <%-- 判断图书列表是否为空 --%>
        <c:choose>

            <c:when test="${fn:length(QueryResult)>0}">

                <div class="weui_cells">

                    <c:forEach items="${QueryResult}" var="book">

                        <div class="weui_cell">
                            <div class="weui_cell_bd weui_cell_primary">
                                <p>${book.name}</p>
                            </div>
                            <div class="weui_cell_ft" style="margin-left: 15px">
                                借阅时间�?${book.borrowDate}<br>应还时间�?${book.returnDate}
                            </div>
                        </div>

                    </c:forEach>

                </div>

            </c:when>

            <c:otherwise>

                <div class="weui_msg">
                    <div class="weui_text_area">
                        <div class="weui_icon_area"><i class="weui_icon_info weui_icon_msg"></i></div>
                        <h2 class="weui_msg_title">无�?�阅图书</h2>
                        <p class="weui_msg_desc">下一个学霸就是你!</p>
                    </div>
                </div>

                <div class="weui_msg">

                    <div class="weui_extra_area">
                        <p>广东二师助手团队 林冠�? 版权�?�?</p>
                        <p>All rights reserved © 2016 - 2018</p>
                    </div>

                </div>

            </c:otherwise>

        </c:choose>

    </c:when>

    <c:otherwise>

        <!-- 使用JS提交数据 -->
        <script type="text/javascript">
            function postQueryForm() {
                if ($("#number").val() === "" || $("#password").val() === "") {
                    $(".weui_warn").text("请将信息填写完整�?").show().delay(2000).hide(0);
                }
                else {
                    $("#loadingToast, .weui_mask").show();
                    $("form").submit();
                }
            }
        </script>

        <!-- 用户填写图书证号和密码的页面 -->

        <div id="edit">

            <div class="hd">
                <h1 class="page_title">图书借阅查询</h1>
                <p class="page_desc">广东第二师范学院移动图书�?</p>
            </div>

            <!-- 提交的查询信息表�? -->
            <div class="weui_cells weui_cells_form">
                <form action="/bookquery" method="post">
                    <div class="weui_cell">
                        <div class="weui_cell_hd">
                            <label class="weui_label">学号</label>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <input id="number" class="weui_input" type="tel" maxlength="11" name="number"
                                   value="${BookQueryNumber}" placeholder="请输入�?�阅证学�?">
                        </div>
                    </div>
                    <div class="weui_cell">
                        <div class="weui_cell_hd">
                            <label class="weui_label">密码</label>
                        </div>
                        <div class="weui_cell_bd weui_cell_primary">
                            <input id="password" class="weui_input" type="password" maxlength="35" name="password"
                                   value="${BookQueryPassword}" placeholder="请输入�?�阅证密�?">
                        </div>
                    </div>
                </form>
            </div>

            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
            </div>

            <!-- 查询中弹�? -->
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
                    <p class="weui_toast_content">查询�?</p>
                </div>
            </div>

            <!-- 错误提示，显示时�?$.show();隐藏时用$.hide(); -->
            <div class="weui_toptips weui_warn js_tooltips"></div>

            <p class="page_desc" style="margin-top: 25px">初始借阅证密码与学号相同</p>

            <br>

        </div>

    </c:otherwise>

</c:choose>

</body>
</html>
