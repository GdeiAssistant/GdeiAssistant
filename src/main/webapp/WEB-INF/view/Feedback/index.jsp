<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>帮助与反馈</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="default" rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">帮助与反馈</h1>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/feedback/faq'">
        <div class="weui-cell__bd">
            <p>常见问题解答</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/feedback/function'">
        <div class="weui-cell__bd">
            <p>功能意见反馈</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/feedback/ticket'">
        <div class="weui-cell__bd">
            <p>故障工单提交</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'mailto:copyright@gdeiassistant.cn?subject=侵权申诉&body=请补全此模板邮件中的空缺信息，并将邮件发送到copyright@gdeiassistant.cn。。%0d%0a%0d%0a资质身份：（自然人/法人/非法人组织）%0d%0a名称：（真实姓名/企业/机构/单位/团体全称）%0d%0a联系方式：（手机号/座机电话/传真号码/邮箱）%0d%0a证件号码：（身份证号码/护照号码/营业执照编号/统一社会信用代码证号）%0d%0a是否为代理人身份：（权利人/代理人）%0d%0a代理人名称：（选填，非代理人可以不填写）%0d%0a权益类型：（著作权/商标权/商誉权/人格权）%0d%0a申诉链接：（请确保输入的链接格式正确，最多可输入100条，用回车键分开，如有错误将不予受理）%0d%0a申诉理由：（该申诉理由将会被转送至被申诉人，请注意在提交前做好个人信息保护措施）%0d%0a处理要求：（指申诉人要求我站对申诉内容采取的措施）%0d%0a%0d%0a请以附件形式上传权利人有效证件扫描件及能证明申诉属实的相关资料或照片，如为代理申诉，须同时填写代理人信息并上传授权材料。证明材料一般提供原件的彩色扫描件，只能提供复印件扫描件的，需申诉人在复印件上签章。如证明材料涉外的，应按照法律规定，进行公证转递，并同时提供相应的公证转递材料。'">
        <div class="weui-cell__bd">
            <p>网络侵权申诉</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = 'mailto:report@gdeiassistant.cn'">
        <div class="weui-cell__bd">
            <p>社区违规举报</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

</body>
</html>
