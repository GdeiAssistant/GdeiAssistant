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

//显示错误提示
function showErrorTip(message) {
    $(".weui_warn").text(message).show().delay(2000).hide(0);
}

var hasMore = true;

//加载更多数据
function loadMoreInfo() {
    if (hasMore === true) {
        loadInfo($(".list").length);
    }
}

//加载初始数据
$(function () {
    loadInfo(0);
});

//加载失物招领信息
function loadInfo(start) {
    var loading = weui.loading('加载中');
    $.ajax({
        url: '/api/lostandfound/lostitem/start/' + start,
        type: 'get',
        success: function (result) {
            loading.hide();
            if (result.success === true) {
                if (result.data !== null && result.data.length !== 0) {
                    for (var i = 0; i < result.data.length; i++) {
                        $(".lists").append("<div class='list' onload='getPreviewPicture(result.data[i].id)'>" +
                            "<a href='/lostandfound/detail/id/" + result.data[i].id + "'>" +
                            "<i class='img'>" + "<img id='" + result.data[i].id + "' onload='window.checkimg(this)'>" +
                            "</i>" + "<h5 class='tit'>" + result.data[i].name + "</h5>" +
                            "<p class='summary'>" + result.data[i].description + "</p>" + "</a></div>");
                        $(".lists").append("<script>getPreviewPicture('" + result.data[i].id + "')<\/script>");
                    }
                } else {
                    $("#loadmore").text("没有更多信息");
                    hasMore = false;
                }
            } else {
                showErrorTip(result.message);
            }
        },
        error: function () {
            loading.hide();
            showErrorTip("网络异常，请检查网络连接");
        }
    });
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