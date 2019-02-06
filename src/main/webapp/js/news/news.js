let news = [[], [], [], [], []];

let type = 0;

let anchor;

$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //加载SessionStorage缓存的分类、瞄点和新闻通知信息列表信息
    let newsData = sessionStorage.getItem("newsData");
    let newsType = sessionStorage.getItem("newsType");
    let newsAnchor = sessionStorage.getItem("newsAnchor");
    if (newsData && newsType) {
        //从缓存中加载分类和新闻通知信息列表信息
        type = parseInt(newsType);
        news = JSON.parse(newsData);
        loadNewCellReload(type);
    }
    //加载默认分类下的新闻通知信息
    switchNewsType(type);
    //绑定新闻分类切换按钮点击响应事件
    $(".weui-tabbar a").each(function (index) {
        $(this).on('click', function () {
            switchNewsType(index);
        });
    });
    //绑定加载更多按钮点击响应事件
    $("#loadMore").on('click', function () {
        loadNews(type, function (result) {
            if ($(".weui-cells div").length == 0) {
                loadNewCellReload(type);
            }
            loadNewCellAmend(result);
        }, function () {

        });
    });
    if (newsAnchor) {
        //从缓存中加载瞄点位置
        anchor = newsAnchor;
    }
});

function loadNewCellAmend(data) {
    //累加新闻通知信息列表
    for (let i = 0; i < data.length; i++) {
        $(".weui-cells").append("<div id='" + data[i].id + "'  onclick='showNewDetailInfo(\"" + data[i].id + "\")' class='weui-cell weui-cell_access' href='javascript:'><div class='weui-cell__bd'><p style='padding-right: 1rem'>" + data[i].title
            + "</p></div><div class='weui-cell__ft'>" + data[i].publishDate + "</div></div>")
    }
    //跳转到瞄点位置
    if (anchor) {
        window.location.href = "#" + anchor;
    }
}

function loadNewCellReload(index) {
    //清空新闻通知信息列表
    $(".weui-cells").empty();
    //重新加载新闻通知信息列表
    for (let i = 0; i < news[index].length; i++) {
        $(".weui-cells").append("<div id='" + news[index][i].id + "' onclick='showNewDetailInfo(\"" + news[index][i].id + "\")' class='weui-cell weui-cell_access' href='javascript:'><div class='weui-cell__bd'><p style='padding-right: 1rem'>" + news[index][i].title
            + "</p></div><div class='weui-cell__ft'>" + news[index][i].publishDate + "</div></div>")
    }
    //跳转到瞄点位置
    if (anchor) {
        window.location.href = "#" + anchor;
    }
}

function switchNewsType(index) {
    //修改当前选中分类显示效果
    for (let i = 0; i < $(".weui-tabbar a").length; i++) {
        $(".weui-tabbar a:eq(" + i + ")").removeClass("weui-bar__item_on");
    }
    $(".weui-tabbar a:eq(" + index + ")").addClass("weui-bar__item_on");
    //若当前分类的缓存列表中没有新闻信息，则主动加载
    if (news[index].length == 0) {
        loadNews(index, function (result) {
            if (type != index) {
                //重新加载新闻信息
                loadNewCellReload(index);
                //切换当前选中的分类值
                type = index;
            } else {
                loadNewCellAmend(result);
            }
        }, function () {
            //清空新闻通知信息列表
            $(".weui-cells").empty();
            //切换当前选中的分类值
            type = index;
        });
    } else {
        if (type != index) {
            //重新加载新闻信息
            loadNewCellReload(index);
            //切换当前选中的分类值
            type = index;
        }
    }
}

function showNewDetailInfo(id) {
    //缓存瞄点、分类和新闻通知列表信息
    sessionStorage.setItem("newsData", JSON.stringify(news));
    sessionStorage.setItem("newsType", type);
    sessionStorage.setItem("newsAnchor", id);
    //显示新闻详细信息
    window.location.href = '/new/id/' + id;
}

function loadNews(index, onSuccess, onError) {
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/rest/new/type/' + index + '/start/' + news[index].length + '/size/10',
        method: 'GET',
        success: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                for (let i = 0; i < result.data.length; i++) {
                    news[index].push(result.data[i]);
                }
                onSuccess(result.data);
            } else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                onError();
            }
        },
        error: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.status) {
                $(".weui_warn").text("服务暂不可用，请稍后再试").show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
            }
            onError();
        }
    })
}