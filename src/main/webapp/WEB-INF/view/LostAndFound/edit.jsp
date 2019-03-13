<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport"
          content="width=device-width,initial-scale=1.0,user-scalable=no,minimum-scale=1.0,maximum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="MobileOptimized" content="320">
    <meta name="format-detection" content="telephone=no"/>
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" type="text/css" href="/css/ershou/ershou-base.css">
    <link rel="stylesheet" type="text/css" href="/css/lostandfound/publish.css">
    <title>广东第二师范学院失物招领</title>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
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
                    $('.picture .images').addClass('forp4');
                }

                var ua = window.navigator.userAgent;
                if (!(/\(i[^;]+;( U;)? CPU.+Mac OS X/).test(ua) && ua.indexOf('Android') === -1 && ua.indexOf('Linux') === -1) {
                    $('body').addClass('isPC');
                }

                // 选择商品分类
                $('#selectType').click(function () {
                    $('.sky').show();
                    $('.sky .mark').show();
                    $('.typemw').show();
                    $('html').css({
                        'height': '100%',
                        'overflow': 'hidden'
                    });
                });

                $('.mw .mwclose').click(function (e) {
                    $(e.currentTypt).closest('.mw').hide();
                    $('.sky').hide();
                    $('.sky .mark').hide();
                    $('html').css({
                        'height': 'auto',
                        'overflow': 'auto'
                    });
                });

                $(".which").on("click", "label", function () {
                    if ($(this).index() == 0) {
                        $(".place").text("丢失地点");
                    } else {
                        $(".place").text("捡到地点");
                    }
                });

                // 提交二手交易信息
                $(".submit").click(function () {
                    //检查输入内容合法性
                    if (parseInt($("input[name='lostType']:checked").val()) < 0 || parseInt($("input[name='lostType']:checked").val()) > 1) {
                        showErrorTip("不合法的寻找类型");
                    }
                    else if ($("#name").val() == '' && $("#name").val().length > 25) {
                        showErrorTip("物品名称长度不合法");
                    }
                    else if ($("#description").val() == '' && $("#description").val().length > 100) {
                        showErrorTip("物品描述长度不合法");
                    }
                    else if ($("#location").val() == '' && $("#location").val().length > 30) {
                        showErrorTip("地点长度不合法");
                    }
                    else if (parseInt($("#itemType").val()) < 0 || parseInt($("#itemType").val()) > 11) {
                        showErrorTip("不合法的物品分类")
                    }
                    else if ($("#qq").val() == '' && $("#wechat").val() == '' && $("#phone").val() == '') {
                        showErrorTip("联系方式至少需要填写一项");
                    }
                    else if ($("#qq").val().length > 20) {
                        showErrorTip("不合法的QQ号码");
                    }
                    else if ($("#wechat").val().length > 20) {
                        showErrorTip("不合法的微信号");
                    }
                    else if ($("#phone").val().length > 11) {
                        showErrorTip("不合法的手机号码");
                    }
                    else {

                        var formData = new FormData();

                        formData.append('lostType', $("input[name='lostType']:checked").val());
                        formData.append('name', $("#name").val());
                        formData.append('description', $("#description").val());
                        formData.append('location', $("#location").val());
                        formData.append('itemType', $("#itemType").val());

                        if ($("#qq").val() != '') {
                            formData.append('qq', $("#qq").val());
                        }
                        if ($("#wechat").val() != '') {
                            formData.append('wechat', $("#wechat").val());
                        }
                        if ($("#phone").val() !== '') {
                            formData.append('phone', $("#phone").val());
                        }

                        //防止用户重复点击提交
                        $(".submit").attr("disabled", true);

                        //显示等待动画
                        var loading = weui.loading('提交中');

                        $.ajax({
                            url: '/api/lostandfound/item/id/' + ${LostAndFoundItemID},
                            type: 'post',
                            data: formData,
                            processData: false,
                            contentType: false,
                            success: function (result) {
                                if (result.success === true) {
                                    if (parseInt($("#lostType").val()) == 0) {
                                        window.location.href = '/lostandfound/lost';
                                    }
                                    else {
                                        window.location.href = '/lostandfound/found';
                                    }
                                }
                                else {
                                    $(".submit").attr("disabled", false);
                                    loading.hide();
                                    showErrorTip(result.message);
                                }
                            },
                            error: function () {
                                $(".submit").attr("disabled", false);
                                loading.hide();
                                showErrorTip("网络异常，请检查网络连接");
                            }
                        });
                    }
                });

                // 选择分类后修改隐藏的Input属性值
                $(".mwc ul li a").click(function () {
                    $(".selectvalue").text($(this).text());
                    $("input[name='itemType']").val($(this).closest("li").index());
                    $(this).closest('.mw').hide();
                    $('.sky').hide();
                    $('.sky .mark').hide();
                    $('html').css({
                        'height': 'auto',
                        'overflow': 'auto'
                    });
                });
            }
        );

        // 显示错误提示
        function showErrorTip(message) {
            $(".weui_warn").text(message).show().delay(2000).hide(0);
        }

    </script>
</head>

<body class="body1">

<form id="form">
    <section class="head">
        <p class="opt"><a href="#" class="submit">完成</a></p>
        <a href="javascript:history.go(-1);" class="back"><i class="i iarr"></i></a>
        <span class="t2">修改失物招领信息</span>
    </section>

    <section class="form">
        <div class="frm">
            <p class="frmt">寻找类型</p>
            <div class="which">
                <label>
                    <input type="radio" name="lostType" value="0"
                           <c:if test="${LostAndFoundItemLostType==0}">checked</c:if>>寻物
                </label>
                <label>
                    <input type="radio" name="lostType" value="1"
                           <c:if test="${LostAndFoundItemLostType==1}">checked</c:if>>寻主
                </label>
            </div>
        </div>
        <div class="frm">
            <p class="frmt">物品名称</p>
            <div class="frmc">
                <input id="name" type="text" placeholder="最多25个字" value="${LostAndFoundItemName}" name="name"
                       maxlength="25">
            </div>
        </div>
        <div class="frm">
            <p class="frmt">物品描述</p>
            <div class="frmc">
                <input id="description" type="text" name="description" value="${LostAndFoundItemDescription}"
                       maxlength="=100">
            </div>
        </div>
        <div class="frm">
            <p class="frmt place">丢失地点</p>
            <div class="frmc">
                <input id="location" type="text" name="location" value="${LostAndFoundItemLocation}" maxlength="30">
            </div>
        </div>
        <div class="frm">
            <p class="frmt select">选择分类</p>
            <div class="frmc"><b id="selectType"><span class="selectvalue">${LostAndFoundItemItemType}</span>
                <i class="i iarrow"></i></b></div>
            <input id="itemType" type="hidden" name="itemType" value="${LostAndFoundItemItemTypeValue}">
        </div>

        <!-- 联系方式 -->
        <div class="contact_tip">QQ号/微信/手机号任填其中一项即可</div>
        <div class="frm">
            <p class="frmt">QQ号</p>
            <div class="frmc">
                <input id="qq" type="text" name="qq" value="${LostAndFoundItemQQ}" maxlength="20">
            </div>
        </div>
        <div class="frm">
            <p class="frmt">微信号</p>
            <div class="frmc">
                <input id="wechat" type="text" name="wechat" value="${LostAndFoundItemWechat}" maxlength="20">
            </div>
        </div>
        <div class="frm">
            <p class="frmt">手机号</p>
            <div class="frmc">
                <input id="phone" type="text" name="phone" value="${LostAndFoundItemPhone}" maxlength="11">
            </div>
        </div>

        <br><br>
    </section>
</form>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<div class="sky">
    <div class="mark"></div>
    <div class="mw typemw">

        <div class="mwt">
            <a href="javascript:" class="mwclose"><i class="i imwclose"></i></a>
            <p>选择分类</p>
        </div>

        <div class="mwc">
            <ul>
                <li><a href="javascript:">校园代步</a></li>
                <li><a href="javascript:">手机</a></li>
                <li><a href="javascript:">电脑</a></li>
                <li><a href="javascript:">数码配件</a></li>
                <li><a href="javascript:">数码</a></li>
                <li><a href="javascript:">电器</a></li>
                <li><a href="javascript:">运动健身</a></li>
                <li><a href="javascript:">衣物伞帽</a></li>
                <li><a href="javascript:">图书教材</a></li>
                <li><a href="javascript:">租赁</a></li>
                <li><a href="javascript:">生活娱乐</a></li>
                <li><a href="javascript:">其他</a></li>
            </ul>
        </div>

    </div>
</div>

</body>

</html>