let keyword;

let start = 0;

//设置关键词并进行初始搜索
function queryTopicKeyword() {
    if ($("#keyword").val() != '' && $("#keyword").val().length <= 10) {
        keyword = $("#keyword").val();
        $("#search").hide();
        $("#result").show();
        loadTopicData();
    } else {
        weui.topTips('搜索关键词长度不合法');
    }
}

$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
});

//打开图片浏览器
function browseTopicImage(image) {
    let images = [];
    for (let i = 1; i <= $(image).attr("data-count"); i++) {
        let data = {
            image: $("#f-bg-img-" + $(image).attr("data-id") + "-" + i).attr("src")
        };
        images.push(data);
    }
    $.photoBrowser({
        items: images,
        onOpen: function () {
            $(".photo-container img").css("margin", "0 auto");
            $(".weui-tabbar").hide();
        },
        onClose: function () {
            $(".weui-tabbar").show();
        },
        initIndex: $(image).attr("data-index") - 1
    }).open();
}

//加载话题信息数据
function loadTopicData() {
    $("#loadmore").attr("disabled", true);
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: "/api/topic/keyword/" + keyword + "/start/" + start + "/size/10",
        type: "get",
        success: function (result) {
            $("#loadmore").attr("disabled", false);
            $("#loadingToast, .weui_mask").hide();
            if (result.success === true) {
                if (result.data.length != 0) {
                    start = start + result.data.length;
                    for (let i = 0; i < result.data.length; i++) {
                        let data = result.data[i];
                        $("<div>").attr("data-v-5388caba", "").addClass("wb-item-wrap").addClass("wb-item-wrap-" + data.id).appendTo("#data");
                        $("<div>").attr("data-v-5388caba", "").addClass("wb-item").addClass("wb-item-" + data.id).appendTo(".wb-item-wrap-" + data.id);
                        $("<div>").attr("data-v-5388caba", "").addClass("card").addClass("m-panel").addClass("card9").addClass("f-topic").addClass("m-panel-" + data.id).appendTo(".wb-item-" + data.id);
                        //个人资料和发布时间
                        $("<div>").addClass("card-wrap").addClass("card-wrap-first-" + data.id).appendTo(".m-panel-" + data.id);
                        $("<header>").addClass("topic-top").addClass("m-box").addClass("m-box-" + data.id).appendTo(".card-wrap-first-" + data.id);
                        $("<div>").addClass("m-avatar-box").addClass("m-box-center").html("<a href='/profile/user/" + data.username + "' class='m-img-box'><img id='profile-avatar-" + data.id + "' src=''/></a>").appendTo(".m-box-" + data.id);
                        $("<div>").addClass("m-box-dir").addClass("m-box-col").addClass("m-box-center").addClass("m-box-dir-" + data.id).appendTo(".m-box-" + data.id);
                        $("<div>").addClass("m-text-box").html("<a href='/profile/user/" + data.username + "'><h3 class='m-text-cut profile-nickname-" + data.id + "'></h3></a><h4 class='m-text-cut'><span class='time'>" + data.publishTime + "</span></h4>").appendTo(".m-box-dir-" + data.id);
                        //加载个人资料昵称和头像脚本
                        $(".card-wrap-first-" + data.id).append("<script>loadProfile(" + data.id + ",'" + data.username + "')<\/script>");
                        //话题信息正文
                        $("<article>").addClass("topic-main").addClass("topic-main-" + data.id).appendTo(".m-panel-" + data.id);
                        $("<div>").addClass("card-wrap").addClass("card-wrap-second-" + data.id).appendTo(".topic-main-" + data.id);
                        $("<div>").addClass("topic-og").addClass("topic-og-" + data.id).appendTo(".card-wrap-second-" + data.id);
                        $("<div>").addClass("topic-text").html("<a><span class='surl-text'>#" + data.topic + "# </span></a>" + data.content).appendTo(".topic-og-" + data.id);
                        if (data.count > 0) {
                            $("<div>").addClass("topic-image").addClass("topic-image-" + data.id).appendTo(".topic-og-" + data.id);
                            $("<div>").attr("data-v-58545971", "").addClass("topic-media-wraps").addClass("topic-media").addClass("f-media").addClass("media-b").addClass("topic-media-" + data.id).appendTo(".topic-image-" + data.id);
                            $("<ul>").attr("data-v-58545971", "").addClass("m-auto-list").addClass("m-auto-list-" + data.id).appendTo(".topic-media-" + data.id);
                            for (let j = 0; j < data.count; j++) {
                                $(".m-auto-list-" + data.id).append("<li data-v-58545971='' class='m-auto-box" + (data.count > 3 ? 3 : data.count) + "'><div data-v-58545971='' class='m-img-box m-imghold-4-3'><img id='f-bg-img-" + data.id + "-" + (j + 1) + "' data-id='" + data.id + "' data-count='" + data.count + "' data-index='" + (j + 1) + "' data-v-58545971='' src='' class='f-bg-img' onclick='browseTopicImage(this)'></div></li>");
                                //加载话题信息图片
                                $(".card-wrap-second-" + data.id).append("<script>loadTopicImage(" + data.id + "," + (j + 1) + ")<\/script>");
                            }
                        }
                        $("<footer>").addClass("f-footer-ctrl").addClass("f-footer-ctrl-" + data.id).appendTo(".m-panel-" + data.id);
                        $("<aside>").html("<div class='m-diy-btn m-box-center-a' onclick='likeTopic(" + data.id + ")'><i id='like-icon-" + data.id + "' class='lite-iconf lite-iconf-like" + (data.liked ? "d" : "") + "'></i><h4 id='like-count-" + data.id + "'>" + data.likeCount + "</h4></div>").appendTo(".f-footer-ctrl-" + data.id);
                    }
                } else {
                    $("#loadmore").text("已无更多信息");
                    $("#loadmore").attr("disabled", true);
                }
            } else {
                weui.topTips(result.message);
            }
        },
        error: function (result) {
            $("#loadmore").attr("disabled", false);
            $("#loadingToast, .weui_mask").hide();
            if (result.status) {
                weui.topTips(result.responseJSON.message);
            } else {
                weui.topTips('网络连接失败,请检查网络连接');
            }
        }
    });
}

//点赞话题信息
function likeTopic(id) {
    if (!$("#like-icon-" + id).hasClass("lite-iconf-liked")) {
        $(".m-diy-btn").attr("disabled", true);
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: "/api/topic/id/" + id + "/like",
            type: "post",
            success: function (result) {
                $(".m-diy-btn").attr("disabled", false);
                $("#loadingToast, .weui_mask").hide();
                if (result.success === true) {
                    $("#like-icon-" + id).removeClass("lite-iconf-like").addClass("lite-iconf-liked");
                    $("#like-count-" + id).text(parseInt($("#like-count-" + id).text()) + 1);
                } else {
                    weui.topTips(result.message);
                }
            },
            error: function (result) {
                $(".m-diy-btn").attr("disabled", false);
                $("#loadingToast, .weui_mask").hide();
                if (result.status) {
                    weui.topTips(result.responseJSON.message);
                } else {
                    weui.topTips('网络连接失败,请检查网络连接');
                }
            }
        });
    }
}

//加载发布者头像和昵称信息
function loadProfile(id, username) {
    $.ajax({
        url: "/rest/avatar/" + username,
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                if (result.data && result.data != '') {
                    $("#profile-avatar-" + id).attr("src", result.data);
                }
            }
        }
    });
    $.ajax({
        url: "/rest/nickname/" + username,
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                if (result.data && result.data != '') {
                    $(".profile-nickname-" + id).text(result.data);
                }
            }
        }
    });
}

//加载话题信息图片
function loadTopicImage(id, index) {
    $.ajax({
        url: "/api/topic/id/" + id + "/index/" + index + "/image",
        type: "GET",
        success: function (result) {
            if (result.success === true) {
                if (result.data && result.data != '') {
                    $("#f-bg-img-" + id + "-" + index).attr("src", result.data);
                }
            }
        }
    });
}