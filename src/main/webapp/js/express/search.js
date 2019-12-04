//关键词
let keyword;

//表白信息数量
let start = 0;

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

$(document).ready(function () {

    //点赞
    $("#main").on('click', ".ui-icon-like", function (event) {
        let postID = $(this).attr('post');
        // 判断是否已点赞
        if (!$("a[post='" + postID + "']").hasClass("ui-icon-liked")) {
            //提交点赞请求
            $.ajax({
                url: '/api/express/id/' + postID + '/like',
                type: 'post',
                success: function (result) {
                    if (result.success === true) {
                        //添加已点赞图标，点赞数+1
                        $(event.target).addClass('ui-icon-liked').fadeIn(300);
                        $(event.target).text(parseInt($(event.target).text()) + 1);
                    } else {
                        showWarningMessage(result.message);
                    }
                },
                error: function () {
                    showWarningMessage("网络异常，请检查网络连接");
                }
            });
        }
    });

    //弹出猜名字窗口
    $("#main").on('click', ".ui-icon-guess", function () {
        var proportion = $(this).text();
        var num = proportion.split("/");
        $("#guess_right").text(num[0]);
        $("#guess_all").text(num[1]);
        $("#guess-submit").attr("post", $(this).attr('post'));
        $("#guess-hint").text("");
    });

    //提交猜名字信息
    $("#guess-submit").click(function () {
        let postID = $("#guess-submit").attr("post");
        let guessName = $("#guess-input").val();
        if (guessName != "") {
            $.ajax({
                url: '/api/express/id/' + postID + '/guess',
                type: 'post',
                data: {
                    name: guessName
                },
                success: function (result) {
                    if (result.success === true) {
                        if (result.data === true) {
                            $("#guess-hint").text("恭喜你，猜中了！");
                            $("#guess_right").text((parseInt($("#guess_right").text()) + 1));
                            $("#guess_all").text((parseInt($("#guess_all").text()) + 1));
                            $("a[data-icon='guess'][post='" + postID + "']").text($("#guess_right").text() + "/" + $("#guess_all").text())
                        } else {
                            $("#guess-hint").text("很遗憾，没有猜中，再试试吧！");
                            $("#guess_all").text((parseInt($("#guess_all").text()) + 1));
                            $("a[data-icon='guess'][post='" + postID + "']").text($("#guess_right").text() + "/" + $("#guess_all").text())
                        }
                    } else {
                        showWarningMessage(result.message);
                    }
                },
                error: function () {
                    showWarningMessage("网络异常，请检查网络连接");
                }
            });
        } else {
            showWarningMessage("真实姓名不能为空");
        }
    });

    //加载评论信息
    $("#main").on('click', ".ui-icon-comment", function () {
        let postID = $(this).attr('post');
        $("#comment-submit").attr('post', postID);
        $("#comment-lists-ul").html("");
        commentOutput(postID);
    });

    //提交评论
    $("#comment-submit").click(function () {
        var comment = $("#comment-Popup input").val();
        var postID = $(this).attr('post');
        if (comment != "") {
            $.ajax({
                url: '/api/express/id/' + postID + '/comment',
                type: 'post',
                data: {
                    comment: comment
                },
                success: function (result) {
                    if (result.success === true) {
                        //清空评论输入表单
                        $("#comment-Popup input").val("");
                        //表白信息评论数+1
                        $("a[data-icon='comment'][post='" + postID + "']").text(parseInt($("a[data-icon='comment'][post='" + postID + "']").text()) + 1);
                        //重新渲染评论
                        commentOutput(postID);
                    } else {
                        showWarningMessage(result.message);
                    }
                },
                error: function () {
                    showWarningMessage("网络异常，请检查网络连接");
                }
            });
        } else {
            showWarningMessage("评论信息不能为空");
        }
    });
});

//渲染表白评论
function commentOutput(id) {
    $("#comment-lists-ul").html("");
    $.ajax({
        url: '/api/express/id/' + id + '/comment',
        type: 'get',
        success: function (result) {
            if (result.success === true) {
                if (result.data.length == 0) {
                    $("#comment-lists-ul").text("暂无评论，快来抢沙发吧！");
                } else {
                    for (let i = 0; i < result.data.length; i++) {
                        $("<li>").html('<span class="comment-floor">' + (i + 1) + '楼</span><span class="comment-nickname">' + result.data[i].nickname + '</span><p>' + result.data[i].comment + '</p><span class="comment-time">' + result.data[i].publishTime + '</span>').appendTo('#comment-lists-ul');
                    }
                }
            } else {
                showWarningMessage(result.message);
            }
        },
        error: function () {
            showWarningMessage("网络异常，请检查网络连接");
        }
    });
}

//关键词分页查询
function searchKeyword() {
    let input = $("#search-bars-input").val();
    if (input != '') {
        //显示加载更多选项按钮
        $("#loadmore").show();
        //更新关键词和当前表白信息数量
        start = 0;
        keyword = input;
        //清空已加载的表白信息
        $("#main").empty();
        loadExpress();
    }
    else{
        showWarningMessage("请输入搜索关键词");
    }
}

//加载表白信息
function loadExpress() {
    if (keyword) {
        $.ajax({
            url: '/api/express/keyword/' + keyword + '/start/' + start + '/size/10',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data.length == 0) {
                        $("#loadmore").attr("disabled", false);
                        $("#loadmore span").text("没有更多信息了");
                    } else {
                        start = start + result.data.length;
                        for (let i = 0; i < result.data.length; i++) {
                            let data = result.data[i];
                            $("<div>").addClass('post').addClass('post-' + data.id).appendTo('#main');
                            $("<div>").addClass('post-title').addClass('post-title-' + data.id).appendTo('.post-' + data.id);
                            $("<ul>").html('<li class="' + getGenderCodeByIndex(data.selfGender) + '">' + data.nickname + '</li><li><img src="/img/express/to.png"/></li><li class="' + getGenderCodeByIndex(data.personGender) + '">' + data.name + '</li>').appendTo('.post-title-' + data.id);
                            $("<div>").addClass('post-body').addClass('post-body-' + data.id).appendTo('.post-' + data.id);
                            $("<p>").addClass('post-body-content').text(data.content).appendTo('.post-body-' + data.id);
                            $("<p>").addClass('post-body-time').text(data.publishTime).appendTo('.post-body-' + data.id);
                            $("<div>").addClass('post-actions action ui-navbar').addClass('post-actions-' + data.id).attr('data-role', 'navbar').attr('role', 'navigation').appendTo('.post-' + data.id);
                            $("<ul>").addClass('ui-grid-c').addClass('post-actions-ul-' + data.id).appendTo('.post-actions-' + data.id);
                            if (data.liked) {
                                $("<li>").addClass('ui-block-a').html('<a class="ui-link ui-btn ui-icon-like ui-btn-icon-left ui-icon-liked" href="#" post="' + data.id + '" data-icon="like">' + data.likeCount + '</a>').appendTo('.post-actions-ul-' + data.id);
                            } else {
                                $("<li>").addClass('ui-block-a').html('<a class="ui-link ui-btn ui-icon-like ui-btn-icon-left " href="#" post="' + data.id + '" data-icon="like">' + data.likeCount + '</a>').appendTo('.post-actions-ul-' + data.id);
                            }
                            $("<li>").addClass('ui-block-b').html('<a class="ui-link ui-btn ui-icon-guess ui-btn-icon-left " href="#guess-Name-Popup"  data-rel="popup" data-position-to="window"	data-transition="pop" post="' + data.id + '" data-icon="guess">' + data.guessCount + '/' + data.guessSum + '</a>').appendTo('.post-actions-ul-' + data.id);
                            $("<li>").addClass('ui-block-c').html('<a class="ui-link ui-btn ui-icon-comment ui-btn-icon-left " href="#comment-Popup" data-rel="popup" data-position-to="window"	data-transition="pop" post="' + data.id + '" data-icon="comment">' + data.commentCount + '</a>').appendTo('.post-actions-ul-' + data.id);
                        }
                    }
                } else {
                    showWarningMessage(result.message);
                }
            },
            error: function () {
                showWarningMessage("网络异常，请检查网络连接");
            }
        });
    }
}

//根据性别下标值获取性别参数值
function getGenderCodeByIndex(index) {
    switch (index) {
        case 0:
            //男性
            return "male";

        case 1:
            //女性
            return "female";

        case 2:
            //其他或保密
            return "secrecy";
    }
}

//显示错误信息
function showWarningMessage(text) {
    $("#warning").text(text);
    $(".layui-m-layer").show().delay(2000).hide(0);
}