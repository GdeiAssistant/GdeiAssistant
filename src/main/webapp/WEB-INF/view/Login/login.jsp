<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>广东二师助手—广东第二师范学院必备校园服务应用</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <meta name="keywords" content="校园,四六级,查分,课程表,二师助手,广二师助手,广东二师助手,广东第二师范学院">
    <meta name="description"
          content="广东二师助手是为广东第二师范学院专属打造的校园服务应用，不仅提供了课表查询、成绩查询、四六级考试成绩查询、空课室查询、图书借阅查询、馆藏图书查询、校园卡充值、校园卡挂失、消费查询等综合性的教务功能，还提供了二手交易、失物招领、校园树洞、恋爱交友、表白墙、全民快递、话题等社区交流平台。为广东第二师范学院的校友们带来便携的教务、社交服务，给学生们提供最快最便捷获取校园生活、社团、信息的方式。四年时光，广东二师助手陪你一起走过。">
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/login/login.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript">

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        //提交表单数据登录
        function postLoginForm() {
            if ($("#username").val() === "" || $("#password").val() === "") {
                $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
            } else {
                $("#loadingToast, .weui_mask").show();
                $.ajax({
                    url: '/api/userlogin',
                    method: 'POST',
                    data: {
                        username: $("#username").val(),
                        password: $("#password").val()
                    },
                    success: function (result) {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.success) {
                            if ($("#redirect").val() != '') {
                                window.location.href = $("#redirect").val();
                            } else {
                                window.location.href = '/index';
                            }
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function (result) {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.status) {
                            $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                        } else {
                            $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                        }
                    }
                });
            }
        }

        //弹出Cookie政策提示
        $(function () {
            if (!localStorage.getItem("cookiePolicy")) {
                $("#rnw_cookies_banner").show();
            }
        });

    </script>
</head>

<body>

<div class="hd">
    <h1 class="page_title">广东二师助手</h1>
    <p class="page_desc">请登录教务系统</p>
</div>

<!-- 提交的用户信息表单 -->
<div class="weui_cells weui_cells_form">
    <form>
        <input type="hidden" id="redirect" name="redirect" value="${RedirectURL}">
        <div class="weui_cell">
            <div class="weui_cell_hd">
                <label class="weui_label">账号</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="username" class="weui_input" type="text" maxlength="20" name="username"
                       placeholder="请输入你的教务系统账号">
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_hd">
                <label class="weui_label">密码</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="password" class="weui_input" type="password" maxlength="35" name="password"
                       placeholder="请输入你的教务系统密码">
            </div>
        </div>
    </form>
</div>

<!-- 登录按钮 -->
<div class="weui_btn_area">
    <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postLoginForm()">登录</a>
</div>

<p class="page_desc" style="margin-top: 25px">关于登录账户请阅读
    <a class="page_desc"
       onclick="window.location.href = '/about/account'"
       style="color: #3cc51f;">《教务系统账号说明》
    </a>
</p>

<p class="page_desc">点击登录按钮表示你已阅读并同意
    <br>
    <a class="page_desc"
       onclick="window.location.href = '/agreement'"
       style="color: #3cc51f;">《用户协议》
    </a>和
    <a class="page_desc"
       onclick="window.location.href = '/policy/privacy'"
       style="color: #3cc51f;">《隐私政策》
    </a>
</p>

<!-- 登录中弹框 -->
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
        <p class="weui_toast_content">登录中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<div class="weui_msg">

    <div class="weui_extra_area">
        <p>广东二师助手团队 版权所有</p>
        <p>All rights reserved © 2016 - 2019</p>
    </div>

</div>

<div id="rnw_cookies_banner" style="display: none">
    <a class="spritefont-close" id="rnw_cookies_banner_button_close" title="关闭"
       onclick="$('#rnw_cookies_banner').hide();localStorage.setItem('cookiePolicy',1);" href="javascript:">×</a>
    <div id="rnw_cookies_banner_text">
        本网站使用Cookie的目的是为您提供更加简捷和个性化的上网体验。Cookie将有用的信息存储在您的电脑上，可帮助我们改善您浏览我们网站的效率以及对您的实用性。在某些情况下，它们是网站正常运行所必不可少的。如果您访问本网站，即表示您同意我们使用Cookie。
        请点击<a href="/policy/cookie" id="rnw_cookies_banner_button_more">Cookie政策</a>了解更多信息。
    </div>
</div>

</body>

</html>
