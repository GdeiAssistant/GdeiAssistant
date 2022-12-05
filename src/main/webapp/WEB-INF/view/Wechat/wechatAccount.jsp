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
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
</head>
<body>

<div class="weui-cells__title" onclick="history.go(-1)">返回</div>

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
                <c:if test="${wechataccount.article!=null and wechataccount.article!=''}">
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

<p class="page_desc">微信公众号由微信公众平台提供服务<br>公众号未被收录？点击此处
    <a class="page_desc" href="javascript:" onclick="window.location.href = 'mailto:admin@gdeiassistant.cn?subject=提交校园公众号&body=请补全此模板邮件中的空缺信息，并将邮件发送到admin@gdeiassistant.cn。公众号至少发送过一篇文章推送，否则不予以收录。微信公众号由微信公众平台提供服务，本站仅进行信息收录、展示和推送。运营者在微信公众号运营和推广过程中需遵守微信公众平台和广东二师助手平台的相关规则。%0d%0a%0d%0a公众号名称：%0d%0a公众号微信号：%0d%0a公众号主体：（个人填写账号管理员姓名，非个人的其他如个体户、政府、媒体、企业以及其他组织等主体类型填写机构组织的名称）%0d%0a公众号类型：（填写订阅号/服务号，不支持企业微信）%0d%0a公众号说明：（填写公众号的功能和介绍）'">进行提交</a>
</p>

<br>

</body>
</html>