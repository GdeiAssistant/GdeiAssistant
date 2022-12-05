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
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/cet/cet.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<!-- 用户填写信息页面 -->
<div id="edit">

    <div class="weui-cells__title" onclick="history.go(-1)">返回</div>

    <div class="hd">
        <h1 class="page_title">四六级查询</h1>
    </div>

    <!-- 提交的查询信息表单 -->
    <div class="weui-cells weui-cells_form">
        <div class="weui-cell weui-cell_vcode">
            <div class="weui-cell__hd">
                <label class="weui-label">考号</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="number" class="weui-input" type="number" maxlength="15" name="number" pattern="[0-9]*"
                       placeholder="请输入15位准证号" onkeyup="inputLengthCheck(this,15)">
            </div>
            <div class="weui-cell__ft">
                <a href="javascript:" class="weui-vcode-btn" onclick="importNumber()">导入考号</a>
            </div>
        </div>

        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">姓名</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="name" class="weui-input" type="text" maxlength="20" name="name"
                       placeholder="姓名超过3个字可只输入前3个">
            </div>
        </div>

        <div class="weui-cell">
            <div class="weui-cell__hd">
                <label class="weui-label">验证码</label>
            </div>
            <div class="weui-cell__bd weui-cell_primary">
                <input id="checkcode" class="weui-input" type="text" maxlength="10" name="checkcode"
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
    <div class="weui-btn_area">
        <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

    <p class="page_desc" style="margin-top: 25px">担心遗忘准考证号？点击
        <a class="page_desc" onclick="window.location.href='/cet/save'">保存考号</a>
    </p>

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

    <div class="weui-msg">
        <div class="weui_text_area">
            <h2 id="cetName" class="weui-msg_title"></h2>
            <p id="cetType" class="weui-msg_desc"></p>
            <p id="cetSchool" class="weui-msg_desc"></p>
            <br>
            <p id="cetTotalScore" class="weui-msg_desc" style="color: red"></p>
            <p id="cetListeningScore" class="weui-msg_desc"></p>
            <p id="cetReadingScore" class="weui-msg_desc"></p>
            <p id="cetWritingAndTranslatingScore" class="weui-msg_desc"></p>
        </div>
        <br>
        <div class="weui_opr_area">
            <p class="weui-btn_area">
                <a onclick="reQuery()" class="weui-btn weui-btn_primary" href="javascript:">重新查询</a>
                <a onclick="window.location.href='/index'" class="weui-btn weui-btn_default" href="javascript:">返回主页</a>
            </p>
        </div>
    </div>

</div>

</body>
</html>
