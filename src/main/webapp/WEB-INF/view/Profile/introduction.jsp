<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>个人简介</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <!-- 引入CSS样式 -->
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript" src="/js/profile/introduction.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">详细资料</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<input id="introduction_cache" type="hidden" value="">

<div class="weui-cells__title">个人简介</div>

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="showIntroductionDialog()">
        <div class="weui-cell__bd">
            <p id="introduction_content"></p>
        </div>
    </div>
</div>

<!-- 修改个人简介弹窗 -->
<div id="changeIntroduction" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">请输入个人简介</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells weui-cells_form">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">
                            个人简介
                        </label>
                    </div>
                    <div class="weui-cell__bd">
                        <textarea id="introduction_input" onkeyup="inputLengthCheck(this,80)"
                                  placeholder="请输入个人简介信息" type="text"
                                  class="weui-textarea" placeholder="请输入文本"></textarea>
                        <div class="weui-textarea-counter">
                            <span id="introductionLength">0</span>/80
                        </div>
                    </div>
                </div>
            </div>
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:"
                   onclick="changeIntroduction()">确定</a>
            </div>
        </div>
    </div>
</div>

</body>

</body>
</html>
