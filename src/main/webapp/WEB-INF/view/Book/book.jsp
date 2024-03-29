<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>我的图书馆</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/book/book.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

<div id="input">

    <div class="hd">
        <h1 class="page_title">图书借阅查询</h1>
        <p class="page_desc">广东第二师范学院移动图书馆</p>
    </div>

    <!-- 提交的查询信息表单 -->
    <div class="weui-cells weui-cells_form">
        <form>
            <div class="weui-cell">
                <div class="weui-cell__hd">
                    <label class="weui-label">密码</label>
                </div>
                <div class="weui-cell__bd weui-cell_primary">
                    <input id="password" class="weui-input" type="password" maxlength="35" name="password"
                           placeholder="请输入借阅证密码">
                </div>
            </div>
        </form>
    </div>

    <!-- 提交按钮 -->
    <div class="weui-btn_area">
        <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

</div>

<div id="result" style="display: none">

    <h1 class="page_title" style="margin-top: 20px;margin-bottom: 10px">当前借阅图书</h1>

    <div id="booklist" style="display: none">

        <div class="weui-cells">

        </div>

        <div id="bookDetailDialog" class="weui-popup__container">
            <div class="weui-popup__overlay"></div>
            <div class="weui-popup__modal">
                <div class="toolbar">
                    <div class="toolbar-inner">
                        <a href="javascript:" style="left:0" class="picker-button" onclick="$.closePopup()">关闭</a>
                        <h1 class="title">图书详细信息</h1>
                    </div>
                </div>
                <div class="modal-content">
                    <div class='weui-form-preview'>
                        <div class='weui-form-preview__hd'>
                            <label class='weui-form-preview__label'
                                   style='max-width: 12rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'></label>
                            <em class='weui-form-preview__value'></em>
                        </div>
                        <div class='weui-form-preview__bd'>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>条形码</label><span
                                    class='weui-form-preview__value'></span></div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>SN号</label><span
                                    id="sn" class='weui-form-preview__value'></span>
                            </div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>Code号</label><span
                                    id="code" class='weui-form-preview__value'></span>
                            </div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>作者</label><span
                                    class='weui-form-preview__value'></span></div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>借阅时间</label><span
                                    class='weui-form-preview__value'></span></div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>应还时间</label><span
                                    class='weui-form-preview__value'></span>
                            </div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>续借次数</label><span
                                    class='weui-form-preview__value'></span>
                            </div>
                            <!-- 续借按钮 -->
                            <div class="weui-btn_area">
                                <a id="submit" class="weui-btn weui-btn_primary" href="javascript:"
                                   onclick="renewBook()">续借</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div id="empty" style="display: none">

        <div class="weui-msg">
            <div class="weui_text_area">
                <div class="weui_icon_area"><i class="weui_icon_info weui_icon_msg"></i></div>
                <h2 class="weui-msg_title">无借阅图书</h2>
                <p class="weui-msg_desc">下一个学霸就是你!</p>
            </div>
        </div>

    </div>

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


<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui-toptips weui_warn js_tooltips"></div>

</body>
</html>
