<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/6
  Time: 16:59
  To change this template use File | Settings | File Templates.
--%>
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
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-base.css">
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-nav.css">
    <title>广东第二师范学院二手交易</title>
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

        //跳转到个人资料页面
        function changeToProfilePage() {
            window.location.href = '/profile';
        }

        //显示错误提示
        function showErrorTip(message) {
            $(".weui_warn").text(message).show().delay(2000).hide(0);
        }

        //上架商品
        function onSaleItem(id) {
            $.ajax({
                url: '/ershou/item/state/id/' + id,
                data: {
                    state: 1
                },
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

        //下架商品
        function offSaleItem(id) {
            $.ajax({
                url: '/ershou/item/state/id/' + id,
                data: {
                    state: 0
                },
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

        //确认出售商品
        function saledItem(id) {
            weui.confirm('确定出售后将不能再次编辑，你确定吗？', {
                title: '已出售商品',
                buttons: [{
                    label: '取消',
                    type: 'default'
                }, {
                    label: '确定',
                    type: 'primary',
                    onClick: function () {
                        $.ajax({
                            url: '/ershou/item/state/id/' + id,
                            data: {
                                state: 2
                            },
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

<%-- 如果首次查询失败，显示错误提示 --%>
<c:if test="${ErrorMessage!=null}">
    <script>
        showErrorTip("${ErrorMessage}");
    </script>
</c:if>

<!-- 个人资料 -->
<section class="profile" onclick="changeToProfilePage()">
    <i class="avt"><img id="avatar" src="/img/avatar/default.png"></i>
    <span id="kickname" class="nm"></span>
    <span class="introduction"><p id="introduction"></p></span>
</section>

<!-- 个人二手交易信息 -->
<section class="status">

    <ul class="tabs">
        <li class="tab on" data-stat="doing">正在出售<i class="line"></i></li>
        <li class="tab" data-stat="sold">已下架<i class="line"></i></li>
        <li class="tab" data-stat="off">已出售<i class="line"></i></li>
    </ul>

    <div class="statlists">

        <!-- 待出售的商品 -->
        <div class="statlist" data-statlist="doing">
            <c:if test="${AvailableErshouItemList!=null}">
                <c:forEach var="ErshouItem" items="${AvailableErshouItemList}">
                    <div class="stat">
                        <div class="info">
                            <i class="img">
                                <img id="${ErshouItem.id}" onload="window.checkimg(this)">
                            </i>
                            <h5 class="tit">${ErshouItem.name}</h5>
                            <em class="price">￥${ErshouItem.price}</em>
                            <p class="tm">${ErshouItem.publishTime}</p>
                        </div>
                        <p class="btns">
                            <a class="btn" href="/ershou/edit/id/${ErshouItem.id}"><b>编辑</b></a>
                            <a class="btn offsale"
                               href="javascript:offSaleItem(${ErshouItem.id})"><b>下架</b></a>
                            <a class="btn saled" href="javascript:saledItem(${ErshouItem.id})"><b>确认售出</b></a>
                        </p>
                    </div>
                    <script>
                        $.ajax({
                            url: '/ershou/item/id/${ErshouItem.id}/preview',
                            type: 'get',
                            success: function (result) {
                                if (result.success === true) {
                                    $("#${ErshouItem.id}").attr("src", result.data);
                                }
                            }
                        });
                    </script>
                </c:forEach>
            </c:if>
        </div>

        <!-- 已下架的商品 -->
        <div class="statlist nodis" data-statlist="sold">
            <c:if test="${NotAvailableErshouItemList!=null}">
                <c:forEach var="ErshouItem" items="${NotAvailableErshouItemList}">
                    <div class="stat">
                        <div class="info">
                            <i class="img">
                                <img id="${ErshouItem.id}" onload="window.checkimg(this)">
                            </i>
                            <h5 class="tit">${ErshouItem.name}</h5>
                            <em class="price">￥${ErshouItem.price}</em>
                            <p class="tm">${ErshouItem.publishTime}</p>
                        </div>
                        <p class="btns">
                            <a class="btn" href="/ershou/edit/id/${ErshouItem.id}"><b>编辑</b></a>
                            <a class="btn onsale" href="javascript:onSaleItem(${ErshouItem.id})"><b>上架</b></a>
                        </p>
                    </div>
                    <script>
                        $.ajax({
                            url: '/ershou/item/id/${ErshouItem.id}/preview',
                            type: 'get',
                            success: function (result) {
                                if (result.success === true) {
                                    $("#${ErshouItem.id}").attr("src", result.data);
                                }
                            }
                        });
                    </script>
                </c:forEach>
            </c:if>
        </div>

        <!-- 已出售的商品 -->
        <div class="statlist nodis" data-statlist="off">
            <c:if test="${SoldedErshouItemList!=null}">
                <c:forEach var="ErshouItem" items="${SoldedErshouItemList}">
                    <div class="stat">
                        <div class="info">
                            <i class="img">
                                <img id="${ErshouItem.id}" onload="window.checkimg(this)">
                            </i>
                            <h5 class="tit">${ErshouItem.name}</h5>
                            <em class="price">￥${ErshouItem.price}</em>
                            <p class="tm">${ErshouItem.publishTime}</p>
                        </div>
                    </div>
                    <script>
                        $.ajax({
                            url: '/ershou/item/id/${ErshouItem.id}/preview',
                            type: 'get',
                            success: function (result) {
                                if (result.success === true) {
                                    $("#${ErshouItem.id}").attr("src", result.data);
                                }
                            }
                        });
                    </script>
                </c:forEach>
            </c:if>
        </div>

    </div>
</section>

<div style="height: 4rem;"></div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<!-- 底部Tab栏 -->
<nav class="main-nav">
    <div><a class="on" href="/ershou"><i class="ibar index"><img src="/img/ershou/home_normal.png"></i>首页</a>
    </div>
    <div><a href="/ershou/publish"><i class="ibar ipublish"><img src="/img/ershou/publish.png"></i>发布</a>
    </div>
    <div><a href="/ershou/personal"><i class="ibar iprofile"><img
            src="/img/ershou/personal_selected.png"></i>个人中心</a>
    </div>
</nav>

</body>
</html>