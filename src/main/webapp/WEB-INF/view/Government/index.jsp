<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>政务服务</title>
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
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <link title="pink" rel="alternate stylesheet" href="/css/common/jquery-weui.min_pink.css">
    <link title="blue" rel="alternate stylesheet" href="/css/common/jquery-weui.min_blue.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

    </script>
</head>
<body>

<div>
    <div class="weui_cells_title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">政务服务</h1>
    </div>
</div>

<div class="weui-cells__title">政务服务</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://www.gjzwfw.gov.cn/fwmh/item/gr_index.do'">
        <div class="weui-cell__bd">
            <p>中国政务服务</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://www.gdzwfw.gov.cn/portal/personal/hot'">
        <div class="weui-cell__bd">
            <p>广东政务服务</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://www.gz.gov.cn/gzgov/zwfw/zwfw.shtml'">
        <div class="weui-cell__bd">
            <p>广州政务服务</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">治安户政</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://gdzaj.gd.gov.cn/gdza'">
        <div class="weui-cell__bd">
            <p>广东治安户政</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://gzhzyw.gzjd.gov.cn/pub/index'">
        <div class="weui-cell__bd">
            <p>广州治安户政</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">网上信访</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://www.gjxfj.gov.cn/gjxfj/wsxf/A0903index_1.htm'">
        <div class="weui-cell__bd">
            <p>国家网络信访</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://gdwsxf.gd.gov.cn/wsxf/index.html'">
        <div class="weui-cell__bd">
            <p>广东网络信访</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://www.gz12345.gov.cn'">
        <div class="weui-cell__bd">
            <p>广州网络信访</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

<div class="weui-cells__title">投诉申诉</div>
<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'https://dxss.miit.gov.cn'">
        <div class="weui-cell__bd">
            <p>工信部电信用户申诉</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://sswz.spb.gov.cn'">
        <div class="weui-cell__bd">
            <p>邮政快递消费者申诉</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'https://jubao.nifa.org.cn/ipnifa/index.html'">
        <div class="weui-cell__bd">
            <p>非法校园借贷款举报</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'http://www.moe.gov.cn/jyb_hygq/hygq_tsjb/201505/t20150520_184529.html'">
        <div class="weui-cell__bd">
            <p>教育部统一监督举报</p>
        </div>
        <div class="weui-cell__ft"></div>
    </a>
</div>

</body>
</html>