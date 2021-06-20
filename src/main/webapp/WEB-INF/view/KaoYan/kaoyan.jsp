<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>硕士初试成绩查询</title>
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
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/kaoyan/kaoyan.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">考研成绩查询</h1>
</div>

<!-- 用户填写信息页面 -->
<div id="input">

    <!-- 提交的查询信息表单 -->
    <div class="weui_cells weui_cells_form">
        <div class="weui_cell">
            <div class="weui_cell_hd">
                <label class="weui_label">姓名</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="name" class="weui_input" type="text" maxlength="15" name="name"
                       placeholder="请输入姓名">
            </div>
        </div>
        <div class="weui-cell">
            <div class="weui_cell_hd">
                <label class="weui_label">考号</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="examNumber" class="weui_input" type="number" maxlength="15"
                       name="examNumber" pattern="[0-9]*" placeholder="请输入准考证号">
            </div>
        </div>
        <div class="weui_cell">
            <div class="weui_cell_hd">
                <label class="weui_label">证件号</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="idNumber" class="weui_input" type="text" maxlength="18"
                       name="idNumber" placeholder="请输入证件号码">
            </div>
        </div>
    </div>

    <!-- 提交按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

    <p class="page_desc" style="margin-top: 25px">祝2019考研er金榜题名</p>

    <br><br>

    <div class="weui-cells__title">
        备用查询入口
    </div>
    <div class="weui-cells">
        <a class="weui-cell weui-cell_access" href="javascript:"
           onclick="window.location.href = 'https://yz.chsi.com.cn/apply/cjcx/'">
            <div class="weui-cell__bd">
                <p>研招网硕士初试成绩查询</p>
            </div>
            <div class="weui-cell__ft">
            </div>
        </a>
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

    <!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
    <div class="weui_toptips weui_warn js_tooltips"></div>

</div>

<!-- 查询结果界面 -->
<div id="result" style="display: none">
    <div class="weui-cells__title">考生信息</div>
    <div class="weui-cells">
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>姓名</p>
            </div>
            <div id="result_name" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>报名号</p>
            </div>
            <div id="result_signUpNumber" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>准考证号</p>
            </div>
            <div id="result_examNumber" class="weui-cell__ft"></div>
        </div>
    </div>

    <div class="weui-cells__title">成绩信息</div>
    <div class="weui-cells">
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>总分</p>
            </div>
            <div id="result_totalScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第一门</p>
            </div>
            <div id="result_firstScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第二门</p>
            </div>
            <div id="result_secondScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第三门</p>
            </div>
            <div id="result_thirdScore" class="weui-cell__ft"></div>
        </div>
        <div class="weui-cell" href="javascript:">
            <div class="weui-cell__bd">
                <p>第四门</p>
            </div>
            <div id="result_fourthScore" class="weui-cell__ft"></div>
        </div>
    </div>

    <br><br>

</body>
</html>
