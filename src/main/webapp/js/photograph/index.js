let images = [];

$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //加载统计数据
    loadPhotographStatisticalData();
    //默认加载最新10条照片信息
    loadPhotographs();
});

//加载统计数据
function loadPhotographStatisticalData() {
    $.ajax({
        url: "/api/photograph/statistics/photos",
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                $("#statistics-photos").text(result.data);
            }
        }
    });
    $.ajax({
        url: "/api/photograph/statistics/comments",
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                $("#statistics-comments").text(result.data);
            }
        }
    });
    $.ajax({
        url: "/api/photograph/statistics/likes",
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                $("#statistics-likes").text(result.data);
            }
        }
    });
}

//加载照片信息图片
function loadPhotoImage(id, index) {
    $.ajax({
        url: "/api/photograph/id/" + id + "/index/" + index + "/image",
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                if (result.data && result.data != '') {
                    $("#image-" + id + "-" + index).attr("src", result.data);
                    if (typeof images[id] == "undefined") {
                        let array = [];
                        array[index - 1] = result.data;
                        images[id] = array;
                    } else {
                        images[id][index - 1] = result.data;
                    }
                }
            }
        }
    });
}

//打开图片浏览器
function browseImage(id) {
    $.photoBrowser({
        items: images[id],
        onOpen: function () {
            $(".card").hide();
            $("#toolbar").hide();
            $(".photo-container img").css("margin", "0 auto");
        },
        onClose: function () {
            $(".card").show();
            $("#toolbar").show();
            $.scrollTo('#figure-' + id, 500);
        }
    }).open();
}

//加载评论者头像图片
function loadCommenterAvatar(id, username) {
    $.ajax({
        url: "/rest/avatar/" + username,
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                if (result.data && result.data != '') {
                    $("#comment-avatar-" + id).attr("src", result.data);
                }
            }
        }
    });
}

//加载照片评论列表
function loadCommentList(id) {
    if ($("#comment-box-" + id).attr("comment-count") != 0) {
        $.ajax({
            url: "/api/photograph/id/" + id + "/comment",
            type: "GET",
            success: function (result) {
                if (result.success === true) {
                    //清除原评论信息列表，重新加载
                    $("#comment-box-" + id).empty();
                    for (let i = 0; i < result.data.length; i++) {
                        $("#comment-box-" + id).append("<article class='am-comment'>" +
                            "<a href='/profile/user/" + result.data[i].username + "'><img id='comment-avatar-" + result.data[i].commentId + "' src='/img/avatar/default.png'" +
                            " class='am-comment-avatar' width='48' height='48'></a>" +
                            "<div class='am-comment-main'><header class='am-comment-hd'><div class='am-comment-meta'>" +
                            "<a href='/profile/user/" + result.data[i].username + "' class='am-comment-author'><time datetime=''>" + result.data[i].createTime + "</time> " + result.data[i].kickname + "</a></div></header>" +
                            "<div class='am-comment-bd'>" + result.data[i].comment + "</div></div></article>" +
                            "<script>loadCommenterAvatar(" + result.data[i].commentId + ",'" + result.data[i].username + "')<\/script>");
                    }
                } else {
                    weui.alert(result.message, {
                        title: '加载失败',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                }
            },
            error: function (result) {
                if (result.status) {
                    weui.alert(result.responseJSON.message, {
                        title: '请求失败',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                } else {
                    weui.alert('网络访问异常，请检查网络连接', {
                        title: '网络异常',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                }
            }
        });
    }
}

//加载照片列表
function loadPhotographs() {
    if (!$("#loadmore").attr("disabled")) {
        let loading = weui.loading("加载信息中");
        $("#loadmore").attr("disabled", true);
        $.ajax({
            url: '/api/photograph/type/' + $("#type").val() + '/start/' + $("#start").val() + '/size/' + 10,
            method: 'GET',
            success: function (result) {
                loading.hide();
                if (result.success) {
                    if (result.data.length == 0) {
                        $("#loadmore").attr("disabled", true);
                        $("#loadmore").text("没有更多信息");
                    } else {
                        $("#loadmore").attr("disabled", false);
                        $("#start").val(parseInt($("#start").val()) + result.data.length);
                        let minorImages = "";
                        for (let i = 0; i < result.data.length; i++) {
                            for (let index = 1; index < result.data[i].count; index++) {
                                minorImages = minorImages + "<img id='image-" + result.data[i].id + "-" + (index + 1) + "' style='display: none' class='lazy'/>" +
                                    "<script>loadPhotoImage(" + result.data[i].id + "," + (index + 1) + ")<\/script>";
                            }
                            let card = "<div class='card'>" +
                                "    <div class='card-img' onclick='browseImage(" + result.data[i].id + ")'>" +
                                "        <div id='figure-" + result.data[i].id + "' class='figure-div-box'>" +
                                "            <figure data-am-widget='figure' class='am am-figure card-img-tag" +
                                "                am-figure-zoomable' data-am-figure='{ pureview: 'true' }'>" +
                                "                <img class='lazy' id='image-" + result.data[i].id + "-1'>" +
                                "                <script>loadPhotoImage(" + result.data[i].id + ",1)<\/script>" + minorImages +
                                "            </figure>" +
                                "        </div>" +
                                "    </div>" +
                                "    <div class='tags'>" +
                                "        <span class='am-badge am-badge-warning am-text-sm img-num'>" + result.data[i].count + "图</span>" +
                                "    </div>" +
                                "    <div class='card-desc'>" +
                                "        <p class='card-name'>" + result.data[i].title + "</p>" +
                                "        <p class='card-say'>" + (result.data[i].content ? result.data[i].content : '') + "</p>" +
                                "    </div>" +
                                "    <div class='card-btn-group'>" +
                                "        <div class='am-btn-group am-btn-group-justify'>" +
                                "            <a class='am-btn am-btn-photo' href='javascript:;'" +
                                "                onclick='likePhoto(" + result.data[i].id + ")' id='like-btn-" + result.data[i].id + "' role='button' like-count='" + result.data[i].likeCount + "' liked='" + result.data[i].liked + "'><i" +
                                "                    class='" + (result.data[i].liked ? 'am-icon-check-square' : 'am-icon-check-square-o') + "'></i>" + result.data[i].likeCount + " " + (result.data[i].liked ? '已点赞' : '点赞') + "</a>" +
                                "            <a class='am-btn am-btn-photo' href='javascript:;'" +
                                "                id='comment-btn-" + result.data[i].id + "' onclick='showComment(" + result.data[i].id + ")' role='button'><i" +
                                "                    class='am-icon-th-list'></i>" + result.data[i].commentCount + " 评论</a>" +
                                "        </div>" +
                                "    </div>" +
                                "    <div id='card-comment-" + result.data[i].id + "' style='display: none'>" +
                                "        <p class='comment-hint'>" + (result.data[i].commentCount == 0 ? '目前没有评论！快来抢沙发吧！' : '') + "</p>" +
                                "        <div class='comment-box' id='comment-box-" + result.data[i].id + "' comment-count='" + result.data[i].commentCount + "'></div>" +
                                "        <div class='am-g' style='text-align: center'>" +
                                "            <div class='am-u-sm-6'>" +
                                "                <button type='button' class='am-btn am-btn-primary'" +
                                "                    onclick='startComment(" + result.data[i].id + ")'><i class='am-icon-commenting'></i>" +
                                "                    我要评论</button>" +
                                "            </div>" +
                                "            <div class='am-u-sm-6'>" +
                                "                <button type='button' class='am-btn am-btn-danger'" +
                                "                    onclick='hideComment(" + result.data[i].id + ")'><i class='am-icon-arrow-up'></i>" +
                                "                    收起面板</button>" +
                                "            </div>" +
                                "        </div>" +
                                "    </div>" +
                                "</div>";
                            $("#card-box").append(card);
                        }
                    }
                } else {
                    $("#loadmore").attr("disabled", false);
                    $.alert({
                        title: '加载失败',
                        text: result.message
                    });
                }
            },
            error: function (result) {
                loading.hide();
                $("#loadmore").attr("disabled", false);
                if (result.status) {
                    $.alert({
                        title: '请求失败',
                        text: result.responseJSON.message
                    });
                } else {
                    $.alert({
                        title: '网络异常',
                        text: '网络访问异常，请检查网络连接'
                    });
                }
            }
        });
    }
}

//打开评论界面
function showComment(id) {
    //加载评论列表信息
    loadCommentList(id);
    //展开评论界面
    $("#card-comment-" + id).slideDown();
    //重置评论按钮功能
    $("#comment-btn-" + id).attr("onclick", "hideComment(" + id + ")");
    //跳转到评论容器
    $.scrollTo('#comment-box-' + id, 500);
}

//隐藏评论界面
function hideComment(id) {
    //关闭评论界面
    $("#card-comment-" + id).slideUp();
    //重置评论按钮功能
    $("#comment-btn-" + id).attr("onclick", "showComment(" + id + ")");
    //跳转到照片信息
    $.scrollTo('#figure-' + id, 500);
}

//插入评论内容
function startComment(id) {
    $('#comment-modal').modal({
        relatedTarget: this,
        onConfirm: function (e) {
            if (e.data == "") {
                $.alert({
                    title: '评论内容不能为空',
                    text: '请输入评论内容'
                });
            } else {
                $.ajax({
                    url: "/api/photograph/id/" + id + "/comment",
                    type: "POST",
                    data: {
                        comment: e.data
                    },
                    success: function (result) {
                        if (result.success === true) {
                            $.alert({
                                title: '请求成功',
                                text: '添加评论成功'
                            });
                            //重新加载照片信息评论列表
                            loadCommentList(id);
                        } else {
                            $.alert({
                                title: '请求失败',
                                text: result.message
                            });
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            $.alert({
                                title: '请求失败',
                                text: result.responseJSON.message
                            });
                        } else {
                            $.alert({
                                title: '网络异常',
                                text: '网络访问异常，请检查网络连接'
                            });
                        }
                    }
                });
                $("#comment-name").val("");
                $("#comment-say").val("");
            }
        },
        onCancel: function () {
            $("#comment-name").val("");
            $("#comment-say").val("");
        }
    });
}

//点赞照片信息
function likePhoto(id) {
    if ($("#like-btn-" + id).attr("liked") != 'true') {
        $.ajax({
            url: '/api/photograph/id/' + id + '/like',
            method: 'POST',
            success: function (result) {
                if (result.success) {
                    let count = parseInt($("#like-btn-" + id).attr("like-count")) + 1;
                    $("#like-btn-" + id).html('<i class="am-icon-check-square"></i>' + count + ' 已点赞');
                    $("#like-btn-" + id).attr("liked", "true");
                } else {
                    $.alert({
                        title: '请求失败',
                        text: result.message
                    });
                }
            },
            error: function (result) {
                if (result.status) {
                    $.alert({
                        title: '请求失败',
                        text: result.responseJSON.message
                    });
                } else {
                    $.alert({
                        title: '网络异常',
                        text: '网络访问异常，请检查网络连接'
                    });
                }
            }
        });
    }
}

//切换到生活照模块
function switchToLifePhoto() {
    //清空已加载的照片信息
    $("#card-box").empty();
    $("#start").val(0);
    $("#loadmore").attr("disabled", false);
    //设置为生活照模块
    $("#type").val(1);
    //重新加载照片列表
    loadPhotographs();
}

//切换到校园照模块
function switchToSchoolPhoto() {
    //清空已加载的照片信息
    $("#card-box").empty();
    $("#start").val(0);
    $("#loadmore").attr("disabled", false);
    //设置为校园照模块
    $("#type").val(2);
    //重新加载照片列表
    loadPhotographs();
}