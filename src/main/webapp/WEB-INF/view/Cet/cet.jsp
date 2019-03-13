<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>四六级查询</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/cet/cet.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<!-- 用户填写信息页面 -->
<div id="edit">

    <div class="weui_cells_title" onclick="history.go(-1)">返回</div>

    <div class="hd">
        <h1 class="page_title">四六级查询</h1>
    </div>

    <!-- 提交的查询信息表单 -->
    <div class="weui_cells weui_cells_form">
        <div class="weui-cell weui-cell_vcode">
            <div class="weui_cell_hd">
                <label class="weui_label">考号</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="number" class="weui_input" type="number" maxlength="15" name="number" pattern="[0-9]*"
                       placeholder="请输入15位准证号" onkeyup="inputLengthCheck(this,15)">
            </div>
            <div class="weui-cell__ft">
                <a href="javascript:" class="weui-vcode-btn" onclick="importNumber()">导入考号</a>
            </div>
        </div>

        <div class="weui_cell">
            <div class="weui_cell_hd">
                <label class="weui_label">姓名</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="name" class="weui_input" type="text" maxlength="20" name="name"
                       placeholder="姓名超过3个字可只输入前3个">
            </div>
        </div>

        <div class="weui_cell">
            <div class="weui_cell_hd">
                <label class="weui_label">验证码</label>
            </div>
            <div class="weui_cell_bd weui_cell_primary">
                <input id="checkcode" class="weui_input" type="text" maxlength="10" name="checkcode"
                       placeholder="请输入验证码">
            </div>
            <div class="weui-cell__ft" style="width: 120px;height: 50px;">
                <a href="javascript:" class="weui-vcode-btn" onclick="updateCheckCode()">
                    <img style="width: 100%;height: 100%;" id="checkcodeImage"/>
                </a>
            </div>
        </div>
    </div>

    <!-- 提交按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

    <p class="page_desc" style="margin-top: 25px">担心遗忘准考证号？点击
        <a class="page_desc" onclick="window.location.href='/cet/save'">保存考号</a>
    </p>

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

    <br><br>

    <div class="weui-cells__title">
        备用查询入口
    </div>
    <div class="weui-cells">
        <a class="weui-cell weui-cell_access" href="javascript:"
           onclick="window.location.href = 'http://www.chsi.com.cn/cet/'">
            <div class="weui-cell__bd">
                <p>学信网四六级查分</p>
            </div>
            <div class="weui-cell__ft">
            </div>
        </a>
        <a class="weui-cell weui-cell_access" href="javascript:"
           onclick="window.location.href = 'http://cet.neea.edu.cn/cet/'">
            <div class="weui-cell__bd">
                <p>中国教育考试网查询</p>
            </div>
            <div class="weui-cell__ft">
            </div>
        </a>
    </div>

</div>

<!-- 查询结果页面 -->
<div id="result" style="display: none">

    <div class="hd">
        <h1 class="page_title">查询结果</h1>
        <p class="page_desc">成绩仅供参考,请以成绩单为准</p>
    </div>

    <div class="weui_msg">
        <div class="weui_text_area">
            <h2 id="cetName" class="weui_msg_title"></h2>
            <p id="cetType" class="weui_msg_desc"></p>
            <p id="cetSchool" class="weui_msg_desc"></p>
            <br>
            <p id="cetTotalScore" class="weui_msg_desc" style="color: red"></p>
            <p id="cetListeningScore" class="weui_msg_desc"></p>
            <p id="cetReadingScore" class="weui_msg_desc"></p>
            <p id="cetWritingAndTranslatingScore" class="weui_msg_desc"></p>
        </div>
        <br>
        <div class="weui_opr_area">
            <p class="weui_btn_area">
                <a onclick="reQuery()" class="weui_btn weui_btn_primary" href="javascript:">重新查询</a>
                <a onclick="window.location.href='/index'" class="weui_btn weui_btn_default" href="javascript:">返回主页</a>
            </p>
        </div>
    </div>

</div>

</body>
</html>
