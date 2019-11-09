<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="info" uri="/WEB-INF/tld/info.tld" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>校园公众号</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.min.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.min.css">
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
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">校园公众号</h1>
    <p class="page_desc">热门校园资讯信息、优质社团组织推送，全在这里了</p>
</div>

<div>
    <!-- 所有热门公众号列表 -->
    <c:forEach var="wechataccount" items="${info:getSchoolWechatAccountData()}">
        <div class="weui-form-preview" style="margin-bottom: 10px">
            <div class="weui-form-preview__bd">
                <div class="weui-form-preview__item" style="margin-bottom: 0.3rem">
                    <label class="weui-form-preview__label">头像</label>
                    <img src="${wechataccount.avatar}" style="width: 50px; height: auto">
                </div>
                <div class="weui-form-preview__item">
                    <label class="weui-form-preview__label">公众号</label>
                    <span class="weui-form-preview__value">${wechataccount.name}</span>
                </div>
                <div class="weui-form-preview__item">
                    <label class="weui-form-preview__label">功能介绍</label>
                    <span class="weui-form-preview__value">${wechataccount.description}</span>
                </div>
                <c:if test="${wechataccount.article==null or wechataccount.article!=''}">
                    <div class="weui-form-preview__item">
                        <label class="weui-form-preview__label">最近文章</label>
                        <span class="weui-form-preview__value">${wechataccount.article}</span>
                    </div>
                </c:if>
            </div>
            <div class="weui-form-preview__ft">
                <a class="weui-form-preview__btn weui-form-preview__btn_primary"
                   href="https://mp.weixin.qq.com/mp/profile_ext?action=home&__biz=${wechataccount.biz}&scene=123#wechat_redirect">查看</a>
            </div>
        </div>
    </c:forEach>
</div>

</body>
</html>