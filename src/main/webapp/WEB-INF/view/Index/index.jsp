<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="index" uri="/WEB-INF/tld/index.tld" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>广东二师助手</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/index/index${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <jsp:include page="/js/index/index.jsp"/>
</head>
<body>

<div class="phone">

    <div onclick="window.location.href='/profile'" class="weui-cell__hd" style="margin-top:10px;float: right">
        <p id="right_name" style="margin-left:5px;margin-right:5px;color: #888;display: block;float: right"></p>
        <img id="right_avatar" style="float:right;border-radius: 50%;width: 25px;height: 25px;"
             src="/img/avatar/default.png"/>
        <!-- 小红点提示，中间文字内容 -->
        <span id="messagesBadge" class="weui-badge"
              style="display: none;float:right;margin-right: -5px;margin-top: -5px">1</span>
    </div>

    <header style="margin-top:45px;clear: both">
        <div>
            <h1>${index:getWelcomeTitle()}</h1>
            <h2>${index:getWelcomeContent()}</h2>
        </div>
    </header>

    <div class="links">
        <div onclick="window.location.href='/grade'">
            <i style="background: url(/img/function/grade.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>成绩查询</p>
        </div>
        <div onclick="window.location.href='/schedule'">
            <i style="background: url(/img/function/schedule.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>课表查询</p>
        </div>
        <div onclick="window.location.href='/cet'">
            <i style="background: url(/img/function/cet.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>四六级查询</p>
        </div>
        <div onclick="window.location.href='/collection'">
            <i style="background: url(/img/function/collection.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>馆藏查询</p>
        </div>
        <div onclick="window.location.href='http://www.ytzhihui.com/wap/index.html'">
            <i style="background: url(/img/function/library.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>我的图书馆</p>
        </div>
        <div onclick="window.location.href='/card'">
            <i style="background: url(/img/function/card.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>消费查询</p>
        </div>
        <div onclick="window.location.href='/cardinfo'">
            <i style="background: url(/img/function/cardInfo.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>我的饭卡</p>
        </div>
        <div onclick="window.location.href='/evaluate'">
            <i style="background: url(/img/function/evaluate.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>一键评教</p>
        </div>
        <div onclick="window.location.href='/spare'">
            <i style="background: url(/img/function/spare.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>教室查询</p>
        </div>
        <div onclick="window.location.href='/kaoyan'">
            <i style="background: url(/img/function/kaoyan.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>考研查询</p>
        </div>
        <div onclick="window.location.href='/data'">
            <i style="background: url(/img/function/data.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>信息查询</p>
        </div>
        <div onclick="linkToPFTSystem()">
            <i style="background: url(/img/function/sport.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>体测查询</p>
        </div>
        <div onclick="window.location.href='/news'">
            <i style="background: url(/img/function/news.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>新闻通知</p>
        </div>
        <div onclick="window.location.href='/ershou'">
            <i style="background: url(/img/function/ershou.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
            </i>
            <p>二手交易</p>
        </div>
        <div onclick="window.location.href='/lostandfound'">
            <i style="background: url(/img/function/lostandfound.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
            </i>
            <p>失物招领</p>
        </div>
        <div onclick="window.location.href='/secret'">
            <i style="background: url(/img/function/secret.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
            </i>
            <p>校园树洞</p>
        </div>
        <div onclick="window.location.href='/dating'">
            <i style="background: url(/img/function/dating.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>恋爱交友</p>
        </div>
        <div onclick="window.location.href='http://msg.weixiao.qq.com/t/bca2e28bc30ce67907e032f483e82d7f'">
            <i style="background: url(/img/function/calendar.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>学期校历</p>
        </div>
        <div onclick="window.location.href = '/about/wechat'">
            <i style="background: url(/img/function/wechat.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>绑定微信</p>
        </div>
        <div onclick="showYibanAttachConfirm()">
            <i style="background: url(/img/function/yiban.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>绑定易班</p>
        </div>
        <div onclick="window.location.href='/about'">
            <i style="background: url(/img/function/about.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>关于应用</p>
        </div>
        <div onclick="showLogoutConfirm()">
            <i style="background: url(/img/function/exit.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
            <p>安全退出</p>
        </div>
    </div>
</div>

<br><br>

</body>
</html>
