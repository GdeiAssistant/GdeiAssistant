<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<html>
<head>
    <title>我的图书馆</title>
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
    <link rel="stylesheet" href="/css/common/common${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min${themecolor}.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min${themecolor}.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        let list = [];

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        function postQueryForm() {
            if ($("#password").val() === "") {
                $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
            } else {
                $("#loadingToast, .weui_mask").show();
                list = [];
                $.ajax({
                    url: '/api/bookquery',
                    method: 'POST',
                    data: {
                        'password': $("#password").val()
                    },
                    success: function (result) {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.success) {
                            if (result.data.length > 0) {
                                $("#result").show();
                                $("#input").hide();
                                $("#booklist").show();
                                for (var i = 0; i < result.data.length; i++) {
                                    list.push(result.data[i]);
                                    $("#booklist .weui_cells").append("<a class='weui-cell weui-cell_access' href='javascript:' onclick='showBookDetail(" + i + ")'>" +
                                        "<div class='weui-cell__bd'><p>" + result.data[i].name + "</p>" +
                                        "<p style='font-size:13px;color:#999'>借阅时间：" + result.data[i].borrowDate + "</p>" +
                                        "<p style='font-size:13px;color:#999'>应还时间：" + result.data[i].returnDate + "</p>" +
                                        "</div><div class='weui-cell__ft'></div></a>");
                                }
                            } else {
                                $("#result").show();
                                $("#input").hide();
                                $("#empty").show();
                            }
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                    },
                    error: function () {
                        $("#loadingToast, .weui_mask").hide();
                        if (result.status == 503) {
                            //网络连接超时
                            $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                        } else {
                            $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                        }
                    }
                })
            }
        }

        //显示图书详细信息
        function showBookDetail(index) {
            $("#bookDetailDialog .weui-form-preview__value:eq(0)").text(list[index].name);
            $("#bookDetailDialog .weui-form-preview__value:eq(1)").text(list[index].id);
            $("#bookDetailDialog .weui-form-preview__value:eq(2)").text(list[index].sn);
            $("#bookDetailDialog .weui-form-preview__value:eq(3)").text(list[index].code);
            $("#bookDetailDialog .weui-form-preview__value:eq(4)").text(list[index].author);
            $("#bookDetailDialog .weui-form-preview__value:eq(5)").text(list[index].borrowDate);
            $("#bookDetailDialog .weui-form-preview__value:eq(6)").text(list[index].returnDate);
            $("#bookDetailDialog .weui-form-preview__value:eq(7)").text(list[index].renewTime);
            $("#bookDetailDialog").popup();
        }

        //续借图书
        function renewBook() {
            let sn = $("#bookDetailDialog .weui-form-preview__value:eq(2)").text();
            let code = $("#bookDetailDialog .weui-form-preview__value:eq(3)").text();
            let loading = weui.loading('续借图书中');
            $.ajax({
                url: '/api/bookrenew',
                method: 'POST',
                data: {
                    sn: sn,
                    code: code
                },
                success: function (result) {
                    loading.hide();
                    if (result.success) {
                        $.toptip('续借图书成功', 'success');
                        $.closePopup();
                    } else {
                        $.toptip(result.message, 'error');
                    }
                },
                error: function () {
                    loading.hide();
                    if (result.status == 503) {
                        //网络连接超时
                        $.toptip(result.responseJSON.message, 'error');
                    } else {
                        $.toptip("网络连接异常，请检查网络连接", 'error');
                    }
                }
            })
        }

    </script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div id="input">

    <div class="hd">
        <h1 class="page_title">图书借阅查询</h1>
        <p class="page_desc">广东第二师范学院移动图书馆</p>
    </div>

    <!-- 提交的查询信息表单 -->
    <div class="weui_cells weui_cells_form">
        <form>
            <div class="weui_cell">
                <div class="weui_cell_hd">
                    <label class="weui_label">密码</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="password" class="weui_input" type="password" maxlength="35" name="password"
                           placeholder="请输入借阅证密码">
                </div>
            </div>
        </form>
    </div>

    <!-- 提交按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

</div>

<div id="result" style="display: none">

    <h1 class="page_title" style="margin-top: 20px;margin-bottom: 10px">当前借阅图书</h1>

    <div id="booklist" style="display: none">

        <div class="weui_cells">

        </div>

        <div id="bookDetailDialog" class="weui-popup__container">
            <div class="weui-popup__overlay"></div>
            <div class="weui-popup__modal">
                <div class="toolbar">
                    <div class="toolbar-inner">
                        <a href="javascript:" style="left:0" class="picker-button" onclick="$.closePopup()">关闭</a>
                        <h1 class="title">图书详细信息</h1>
                    </div>
                </div>
                <div class="modal-content">
                    <div class='weui-form-preview'>
                        <div class='weui-form-preview__hd'>
                            <label class='weui-form-preview__label'
                                   style='max-width: 12rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'></label>
                            <em class='weui-form-preview__value'></em>
                        </div>
                        <div class='weui-form-preview__bd'>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>条形码</label><span
                                    class='weui-form-preview__value'></span></div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>SN号</label><span
                                    id="sn" class='weui-form-preview__value'></span>
                            </div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>Code号</label><span
                                    id="code" class='weui-form-preview__value'></span>
                            </div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>作者</label><span
                                    class='weui-form-preview__value'></span></div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>借阅时间</label><span
                                    class='weui-form-preview__value'></span></div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>应还时间</label><span
                                    class='weui-form-preview__value'></span>
                            </div>
                            <div class='weui-form-preview__item'><label
                                    class='weui-form-preview__label'>续借次数</label><span
                                    class='weui-form-preview__value'></span>
                            </div>
                            <!-- 续借按钮 -->
                            <div class="weui_btn_area">
                                <a id="submit" class="weui_btn weui_btn_primary" href="javascript:"
                                   onclick="renewBook()">续借</a>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <div id="empty" style="display: none">

        <div class="weui_msg">
            <div class="weui_text_area">
                <div class="weui_icon_area"><i class="weui_icon_info weui_icon_msg"></i></div>
                <h2 class="weui_msg_title">无借阅图书</h2>
                <p class="weui_msg_desc">下一个学霸就是你!</p>
            </div>
        </div>

    </div>

</div>

<!-- 查询中弹框 -->
<div class="weui_mask" style="display: none"></div>
<div id="loadingToast" class="weui_loading_toast" style="display: none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <div class="weui_loading">
            <div class="weui_loading_leaf weui_loading_leaf_0"></div>
            <div class="weui_loading_leaf weui_loading_leaf_1"></div>
            <div class="weui_loading_leaf weui_loading_leaf_2"></div>
            <div class="weui_loading_leaf weui_loading_leaf_3"></div>
            <div class="weui_loading_leaf weui_loading_leaf_4"></div>
            <div class="weui_loading_leaf weui_loading_leaf_5"></div>
            <div class="weui_loading_leaf weui_loading_leaf_6"></div>
            <div class="weui_loading_leaf weui_loading_leaf_7"></div>
            <div class="weui_loading_leaf weui_loading_leaf_8"></div>
            <div class="weui_loading_leaf weui_loading_leaf_9"></div>
            <div class="weui_loading_leaf weui_loading_leaf_10"></div>
            <div class="weui_loading_leaf weui_loading_leaf_11"></div>
        </div>
        <p class="weui_toast_content">查询中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
