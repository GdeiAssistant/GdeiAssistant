<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>广东第二师范学院卖室友</title>
    <meta charset="UTF-8">
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
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/dating/global.css">
    <link rel="stylesheet" href="/css/dating/layout.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        //提交撩一下请求
        function submitPickInfo() {
            if ($("#content").val().length > 50) {
                $(".weui_warn").text("撩一下输入的内容太长了").show().delay(2000).hide(0);
            } else if ($("#content").val().length == 0) {
                $(".weui_warn").text("请输入撩一下的留言信息").show().delay(2000).hide(0);
            } else {
                $.ajax({
                    url: '/dating/pick',
                    method: 'post',
                    data: {
                        profileId:${ProfileID},
                        content: $("#content").val()
                    },
                    success: function (result) {
                        if (result.success) {
                            $("#toast").show().delay(1000).hide(0);
                            $("#content").val("");
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function () {
                        $(".weui_warn").text("网络连接异常，请重试").show().delay(2000).hide(0);
                    }
                })
            }
        }

    </script>
</head>
<body>

<div class="panel-overlay"></div>

<div class="warp">

    <div class="head" onclick="window.location.href='/dating'">
        <div class="logo"><span class="t">卖室友</span></div>
    </div>

    <div class="conterInfo">

        <div class="releaseBox">
            <input type="hidden" class="movieid" value="2"/>
            <div class="releaseName">${DatingProfile.kickname}
                <div class="yuan" style="top: -5.5px; left: -5.5px;"></div>
                <div class="yuan" style="top: -5.5px; right: -5.5px;"></div>
                <div class="yuan" style="top: -5.5px; left: 50%; margin-left: -5.5px;"></div>
            </div>
            <div class="Photo">
                <img src="${PictureURL}"/>
            </div>
            <div class="miaoshu">
                ${DatingProfile.content}
            </div>
            <div class="yuepian">
                <ul>
                    <li>
                        <c:choose>
                            <c:when test="${DatingProfile.grade==1}">
                                <span>年级：</span>大一
                            </c:when>
                            <c:when test="${DatingProfile.grade==2}">
                                <span>年级：</span>大二
                            </c:when>
                            <c:when test="${DatingProfile.grade==3}">
                                <span>年级：</span>大三
                            </c:when>
                            <c:otherwise>
                                <span>年级：</span>大四
                            </c:otherwise>
                        </c:choose>
                    </li>
                    <li><span>专业：</span>${DatingProfile.faculty}</li>
                    <li><span>家乡：</span>${DatingProfile.hometown}</li>
                    <c:choose>
                        <c:when test="${isContactVisible!=null}">
                            <li><span>QQ：</span>${DatingProfile.qq}</li>
                            <li><span>微信：</span>${DatingProfile.wechat}</li>
                        </c:when>
                        <c:otherwise>
                            <li><span>QQ：</span>对方接受了撩一下后才可见哦</li>
                            <li><span>微信：</span>对方接受了撩一下后才可见哦</li>
                        </c:otherwise>
                    </c:choose>
                </ul>
            </div>

            <c:if test="${isPickNotAvailable==null}">
                <div class="textBox">
                    <textarea class="textarea" id="content" placeholder="来说点什么吧，不超过50字"></textarea>
                    <input type="button" onclick="submitPickInfo()" class="circleBtn btnclick" value="撩一下"/>
                    <div class="yuan" style="top: -5.5px; left: -5.5px;"></div>
                    <div class="yuan" style="top: -5.5px; right: -5.5px;"></div>
                </div>
            </c:if>
        </div>

    </div>
</div>

<!-- 撩一下信息发送成功弹框 -->
<div id="toast" style="display:none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <i class="weui_icon_toast"></i>
        <p class="weui_toast_content">发送成功，请耐心等待对方回复</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
