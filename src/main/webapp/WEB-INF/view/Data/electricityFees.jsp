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
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <script type="application/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/data/electricityFees.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div>
    <div class="weui_cells_title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">电费查询</h1>
    </div>
</div>

<div id="edit">

    <!-- 用户信息输入表单 -->
    <div class="weui_cells weui_cells_form">
        <form>
            <div class="weui_cell weui-cell_select weui-cell_select-after">
                <div class="weui_cell_hd">
                    <label class="weui_label">年份</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <select class="weui-select" id="year" name="year">
                        <option value="2017" selected>2017</option>
                    </select>
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd">
                    <label class="weui_label">姓名</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="name" class="weui_input" type="text" maxlength="5" name="name"
                           placeholder="请输入你的姓名" onkeyup="inputLengthCheck(this,10)">
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd">
                    <label class="weui_label">学号</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="number" class="weui_input" type="number" maxlength="11" name="number"
                           placeholder="请输入你的学号" onkeyup="inputLengthCheck(this,11)">
                </div>
            </div>
        </form>
    </div>

    <!-- 查询按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
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
