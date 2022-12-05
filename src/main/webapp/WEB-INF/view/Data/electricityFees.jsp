<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>电费查询</title>
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
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/data/electricityFees.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div>
    <div class="weui-cells__title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">电费查询</h1>
    </div>
</div>

<div id="edit">

    <!-- 用户信息输入表单 -->
    <div class="weui-cells weui-cells_form">
        <form>
            <div class="weui-cell weui-cell_select weui-cell_select-after">
                <div class="weui-cell__hd">
                    <label class="weui-label">年份</label>
                </div>
                <div class="weui-cell__bd weui-cell_primary">
                    <select class="weui-select" id="year" name="year">
                        <option value="2017" selected>2017</option>
                    </select>
                </div>
            </div>
            <div class="weui-cell">
                <div class="weui-cell__hd">
                    <label class="weui-label">姓名</label>
                </div>
                <div class="weui-cell__bd weui-cell_primary">
                    <input id="name" class="weui-input" type="text" maxlength="5" name="name"
                           placeholder="请输入你的姓名" onkeyup="inputLengthCheck(this,10)">
                </div>
            </div>
            <div class="weui-cell">
                <div class="weui-cell__hd">
                    <label class="weui-label">学号</label>
                </div>
                <div class="weui-cell__bd weui-cell_primary">
                    <input id="number" class="weui-input" type="number" maxlength="11" name="number"
                           placeholder="请输入你的学号" onkeyup="inputLengthCheck(this,11)">
                </div>
            </div>
        </form>
    </div>

    <!-- 查询按钮 -->
    <div class="weui-btn_area">
        <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
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

</div>

<div id="result" style="display: none;">

    <div class="weui-cells__title">电费信息</div>
    <div class="weui-cells">
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>年份</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>宿舍</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>入住人数</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>用电数额</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>免费电额</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>计费电数</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>电价</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>总电费</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>平均电费</p>
            </div>
            <div class="weui-cell__ft"></div>
        </div>
    </div>

</div>

</body>
</html>
