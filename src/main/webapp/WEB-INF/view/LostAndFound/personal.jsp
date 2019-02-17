<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/base.css">
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/nav.css">
    <title>广东第二师范学院失物招领</title>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="text/javascript">

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        // 这个图片展示规则是宽大于高按高100%截宽，高大于宽按宽100%截高；默认是宽100%截高
        window.checkimg = function (obj) {
            if (obj.width > obj.height) {
                obj.style.width = 'auto';
                obj.style.height = '100%';
            }
        };

        $(function () {
            // 如果屏幕宽度小于375px，按320px宽的页面缩小
            if (window.innerWidth < 375) {
                $('.lists').addClass('forp4');
            }
            var ua = window.navigator.userAgent;
            if (!(/\(i[^;]+;( U;)? CPU.+Mac OS X/).test(ua) && ua.indexOf('Android') === -1 && ua.indexOf('Linux') === -1) {
                $('body').addClass('isPC');
            }
        });

        //设置Tab切换的监听
        $(function () {
            $('.status .tab').click(function (e) {
                var $e = $(e.currentTarget),
                    stat = $e.data('stat');
                $.each($('.status .tab'), function (i, e) {
                    $(e).removeClass('on');
                });
                $e.addClass('on');

                $.each($('.statlists .statlist'), function (i, e) {
                    $(e).addClass('nodis');
                    if ($(e).data('statlist') === stat) {
                        $(e).removeClass('nodis');
                    }

                });
            });
        });

        //加载用户昵称和头像
        $(function () {
            $.ajax({
                url: '/api/avatar',
                async: true,
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        if (result.data !== '') {
                            $("#avatar").attr("src", result.data);
                        }
                    }
                }
            });
            $.ajax({
                url: '/api/profile',
                async: true,
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        $("#kickname").text(result.data.kickname);
                    }
                    else {
                        showErrorTip(result.message);
                    }
                },
                error: function () {
                    showErrorTip("网络异常，请检查网络连接");
                }
            });
            $.ajax({
                url: '/api/introduction',
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        if (result.data === '') {
                            $("#introduction").text('这个人很懒，什么都没写_(:3 」∠)_');
                        }
                        else {
                            $("#introduction").text(result.data);
                        }
                    }
                    else {
                        showErrorTip(result.message);
                    }
                },
                error: function () {
                    showErrorTip("网络异常，请检查网络连接");
                }
            });
        });

        //显示错误提示
        function showErrorTip(message) {
            $(".weui_warn").text(message).show().delay(2000).hide(0);
        }

        //获取预览图片
        function getPreviewPicture(id) {
            $.ajax({
                url: '/api/lostandfound/item/id/' + id + '/preview',
                type: 'get',
                success: function (result) {
                    if (result.success === true) {
                        $("#" + id).attr("src", result.data);
                    }
                }
            });
        }

        //确认寻回物品
        function didFoundItem(id) {
            weui.confirm('确认寻回物品后将不能再次修改信息', {
                title: '是否确认寻回物品？',
                buttons: [{
                    label: '取消',
                    type: 'default'
                }, {
                    label: '确认',
                    type: 'primary',
                    onClick: function () {
                        $.ajax({
                            url: '/api/lostandfound/item/id/' + id + '/didfound',
                            type: 'post',
                            success: function (result) {
                                if (result.success === true) {
                                    window.location.reload();
                                }
                                else {
                                    showErrorTip(result.message);
                                }
                            },
                            error: function () {
                                showErrorTip("网络异常，请检查网络连接");
                            }
                        });
                    }
                }]
            });
        }

    </script>
</head>

<body>

<!-- 个人资料 -->
<section class="profile" onclick="window.location.href='/profile'">
    <i class="avt"><img id="avatar" src="/img/avatar/default.png"></i>
    <span id="kickname" class="nm"></span>
    <span class="introduction">
        <p style="overflow: hidden;text-overflow: ellipsis;" id="introduction"></p>
    </span>
</section>

<!-- 个人二手交易信息 -->
<section class="status">

    <ul class="tabs">
        <li class="tab on" data-stat="lost">失物<i class="line"></i></li>
        <li class="tab" data-stat="found">招领<i class="line"></i></li>
        <li class="tab" data-stat="didfound">确认寻回<i class="line"></i></li>
    </ul>

    <div class="statlists">

        <div class="statlist" data-statlist="lost">
            <c:if test="${LostItemList!=null}">
                <c:forEach var="LostItem" items="${LostItemList}">
                    <div class="stat">
                        <div class="info">
                            <i class="img">
                                <img id="${LostItem.id}" onload="window.checkimg(this)">
                            </i>
                            <h5 class="tit">${LostItem.name}</h5>
                            <p class="tm">${LostItem.publishTime}</p>
                        </div>
                        <p class="btns">
                            <a class="btn" href="/lostandfound/edit/id/${LostItem.id}"><b>编辑</b></a>
                            <a class="btn saled" href="javascript:didFoundItem(${LostItem.id})"><b>确认寻回</b></a>
                        </p>
                    </div>
                    <script>
                        getPreviewPicture(${LostItem.id});
                    </script>
                </c:forEach>
            </c:if>
        </div>

        <div class="statlist nodis" data-statlist="found">
            <c:if test="${FoundItemList!=null}">
                <c:forEach var="FoundItem" items="${FoundItemList}">
                    <div class="stat">
                        <div class="info">
                            <i class="img">
                                <img id="${FoundItem.id}" onload="window.checkimg(this)">
                            </i>
                            <h5 class="tit">${FoundItem.name}</h5>
                            <p class="tm">${FoundItem.publishTime}</p>
                        </div>
                        <p class="btns">
                            <a class="btn" href="/lostandfound/edit/id/${FoundItem.id}"><b>编辑</b></a>
                            <a class="btn saled" href="javascript:didFoundItem(${FoundItem.id})"><b>确认寻回</b></a>
                        </p>
                    </div>
                    <script>
                        getPreviewPicture(${FoundItem.id});
                    </script>
                </c:forEach>
            </c:if>
        </div>

        <div class="statlist nodis" data-statlist="didfound">
            <c:if test="${DidFoundItemList!=null}">
                <c:forEach var="DidFoundItem" items="${DidFoundItemList}">
                    <div class="stat">
                        <div class="info">
                            <i class="img">
                                <img id="${DidFoundItem.id}" onload="window.checkimg(this)">
                            </i>
                            <h5 class="tit">${DidFoundItem.name}</h5>
                            <p class="tm">${DidFoundItem.publishTime}</p>
                        </div>
                    </div>
                    <script>
                        getPreviewPicture(${DidFoundItem.id});
                    </script>
                </c:forEach>
            </c:if>
        </div>

    </div>
</section>

<div style="height: 4rem;"></div>

<!-- 底部Tab栏 -->
<nav class="main-nav">
    <div>
        <a href="/lostandfound/lost"><i class="ibar index">
            <img src="/img/lostandfound/lost_normal.png"></i>失物</a>
    </div>
    <div>
        <a class="on" href="/lostandfound/found"><i class="ibar ipublish">
            <img src="/img/lostandfound/found_normal.png"></i>招领</a>
    </div>
    <div class="publish">
        <div class="pubbtn">
            <a href="/lostandfound/publish">
                <div class="inner">
                </div>
            </a>
        </div>
    </div>
    <div>
        <a href="/lostandfound/search/index"><i class="ibar ipublish">
            <img src="/img/lostandfound/search.png"></i>搜索</a>
    </div>
    <div>
        <a href="/lostandfound/personal"><i class="ibar iprofile">
            <img src="/img/lostandfound/personal_selected.png"></i>个人中心</a>
    </div>
</nav>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>