<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/js/function/function.jsp"/>

<header>
    <h1>广东二师助手</h1>
    <h2>四年时光，广东二师助手陪你一起走过。</h2>
</header>

<div class="links">
    <div id="grade" style="display: none" onclick="window.location.href='/grade'">
        <i style="background: url(/img/function/grade.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>成绩查询</p>
    </div>
    <div id="schedule" style="display: none" onclick="window.location.href='/schedule'">
        <i style="background: url(/img/function/schedule.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>课表查询</p>
    </div>
    <div id="cet" style="display: none" onclick="window.location.href='/cet'">
        <i style="background: url(/img/function/cet.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>四六级查询</p>
    </div>
    <div id="collection" style="display: none" onclick="window.location.href='/collection'">
        <i style="background: url(/img/function/collection.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>馆藏查询</p>
    </div>
    <div id="book" style="display: none" onclick="window.location.href='/book'">
        <i style="background: url(/img/function/library.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>我的图书馆</p>
    </div>
    <div id="bill" style="display: none" onclick="window.location.href='/card'">
        <i style="background: url(/img/function/card.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>消费查询</p>
    </div>
    <div id="card" style="display: none" onclick="window.location.href='/cardinfo'">
        <i style="background: url(/img/function/cardInfo.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>我的饭卡</p>
    </div>
    <div id="evaluate" style="display: none" onclick="window.location.href='/evaluate'">
        <i style="background: url(/img/function/evaluate.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>教学评价</p>
    </div>
    <div id="spare" style="display: none" onclick="window.location.href='/spare'">
        <i style="background: url(/img/function/spare.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>教室查询</p>
    </div>
    <div id="kaoyan" style="display: none" onclick="window.location.href='/kaoyan'">
        <i style="background: url(/img/function/kaoyan.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>考研查询</p>
    </div>
    <div id="tice" style="display: none" onclick="linkToPFTSystem()">
        <i style="background: url(/img/function/sport.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>体测查询</p>
    </div>
    <div id="news" style="display: none" onclick="window.location.href='/news'">
        <i style="background: url(/img/function/news.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>新闻通知</p>
    </div>
    <div id="data" style="display: none" onclick="window.location.href='/data'">
        <i style="background: url(/img/function/data.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>信息查询</p>
    </div>
    <div id="ershou" style="display: none" onclick="window.location.href='/ershou'">
        <i style="background: url(/img/function/ershou.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>二手交易</p>
    </div>
    <div id="lostandfound" style="display: none" onclick="window.location.href='/lostandfound'">
        <i style="background: url(/img/function/lostandfound.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>失物招领</p>
    </div>
    <div id="secret" style="display: none" onclick="window.location.href='/secret'">
        <i style="background: url(/img/function/secret.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>校园树洞</p>
    </div>
    <div id="photograph" style="display: none" onclick="window.location.href='/photograph'">
        <i style="background: url(/img/function/photograph.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>拍好校园</p>
    </div>
    <div id="express" style="display: none" onclick="window.location.href='/express'">
        <i style="background: url(/img/function/express.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>表白墙</p>
    </div>
    <div id="dating" style="display: none" onclick="window.location.href='/dating'">
        <i style="background: url(/img/function/dating.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>卖室友</p>
    </div>
    <div id="topic" style="display: none" onclick="window.location.href='/topic'">
        <i style="background: url(/img/function/topic.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>话题</p>
    </div>
    <div id="delivery" style="display: none" onclick="window.location.href='/delivery'">
        <i style="background: url(/img/function/delivery.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto">
        </i>
        <p>全民快递</p>
    </div>
    <div id="calendar" style="display: none"
         onclick="window.location.href='http://msg.weixiao.qq.com/t/bca2e28bc30ce67907e032f483e82d7f'">
        <i style="background: url(/img/function/calendar.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>学期校历</p>
    </div>
    <div id="government" style="display: none"
         onclick="window.location.href='https://www.gdzwfw.gov.cn/portal/personal/hot'">
        <i style="background: url(/img/function/goverment.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>政务服务</p>
    </div>
    <div id="student" style="display: none"
         onclick="window.location.href='https://www.chsi.com.cn'">
        <i style="background: url(/img/function/student.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>学信网</p>
    </div>
    <div id="volunteer" style="display: none" onclick="window.location.href='https://www.gdzyz.cn/'">
        <i style="background: url(/img/function/volunteer.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>i志愿</p>
    </div>
    <div id="healthcode" style="display: none" onclick="linkToYueKangCode()">
        <i style="background: url(/img/function/healthcode.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>粤康码</p>
    </div>
    <div id="travelcode" style="display: none" onclick="window.location.href='https://xc.caict.ac.cn/'">
        <i style="background: url(/img/function/travelcode.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>通信行程码</p>
    </div>
    <div id="ncov" style="display: none" onclick="window.location.href = 'https://ncov.dxy.cn/ncovh5/view/pneumonia'">
        <i style="background: url(/img/function/ncov.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>疫情动态</p>
    </div>
    <div id="wechat" style="display: none" onclick="window.location.href = '/wechat/attach'">
        <i style="background: url(/img/function/wechat.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>绑定微信</p>
    </div>
    <div id="about" onclick="window.location.href='/about'">
        <i style="background: url(/img/function/about.png) ; background-repeat: no-repeat; background-position: center; background-size: 85% auto"></i>
        <p>关于应用</p>
    </div>
</div>