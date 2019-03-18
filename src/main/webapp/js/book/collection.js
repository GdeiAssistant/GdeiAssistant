//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//从本地存储加载缓存的馆藏信息
$(document).ready(function () {
    var sessionStorageList = sessionStorage.getItem("collectionList");
    var sessionStorageCurrentPage = sessionStorage.getItem("currentPage");
    var sessionStorageSumPage = sessionStorage.getItem("sumPage");
    var sessionStorageAnchor = sessionStorage.getItem("anchor");
    if (sessionStorageCurrentPage !== null) {
        $("#currentPage").val(parseInt(sessionStorageCurrentPage));
    }
    if (sessionStorageSumPage !== null) {
        $("#sumPage").val(parseInt(sessionStorageSumPage));
    }
    if (sessionStorageList !== null) {
        var collectionList = JSON.parse(sessionStorageList);
        for (var i = 0; i < collectionList.length; i++) {
            var bookname = collectionList[i].bookname;
            var author = collectionList[i].author;
            var publishingHouse = collectionList[i].publishingHouse;
            var detailURL = collectionList[i].detailURL.toString();
            if (typeof (author) != "undefined" && typeof (publishingHouse) != "undefined") {
                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                    "<div class='weui-cell__bd'>" +
                    "<p>" + bookname + "</p>" +
                    "<p style='font-size: 13px;color: #999'>" + author + "</p>" +
                    "<p style='font-size:13px;color:#999'>" + publishingHouse + "</p></div></a>");
            } else if (typeof (author) == "undefined") {
                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                    "<div class='weui-cell__bd'>" +
                    "<p>" + bookname + "</p>" +
                    "<p style='font-size:13px;color:#999'>" + publishingHouse + "</p></div></a>");
            } else if (typeof (publishingHouse) == "undefined") {
                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                    "<div class='weui-cell__bd'>" +
                    "<p>" + bookname + "</p>" +
                    "<p style='font-size:13px;color:#999'>" + publishingHouse + "</p></div></a>");
            } else {
                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                    "<div class='weui-cell__bd'>" +
                    "<p>" + bookname + "</p></div></a>");
            }
            setDetailURL(detailURL);
        }
        if ($("#currentPage").val() === $("#sumPage").val()) {
            //已经是最后一页
            $("#bottomLine").text("别拉了,已经到底了").show();
            $("#bottomLine").removeAttribute("onclick");
        } else {
            $("#bottomLine").text("点击加载更多信息").show();
            $("#bottomLine").attr("onclick", "queryMore();");
        }
        if (sessionStorageAnchor !== null) {
            //跳转到瞄点位置
            window.location.href = "#" + sessionStorageAnchor;
        }
    }
});

//设置图书详细信息URL
function setDetailURL(url) {
    document.getElementById(url).onclick = function () {
        getDetailInfo(url);
    };
}

//访问高级检索URL
function openAdvancedSearchPage() {
    window.location.href = 'http://agentdockingopac.featurelib.libsou.com/showhome/search/showSearch?schoolId=705';
}

//监听返回事件
$(function () {
    window.addEventListener("popstate", function () {
        //退出时删除缓存的馆藏信息
        sessionStorage.removeItem("collectionList");
        sessionStorage.removeItem("currentPage");
        sessionStorage.removeItem("sumPage");
        sessionStorage.removeItem("anchor");
        $(document).ready(function () {
            window.location.href = '/index';
        });
    }, true);
});

//查看图书详细信息
function getDetailInfo(url) {
    sessionStorage.setItem("anchor", url);
    window.location.href = '/collectiondetail?' + url;
}

//提交查询请求
function postQueryForm() {
    if ($("#bookname").val() === "") {
        $(".weui_warn").text("请将信息填写完整！").show().delay(2000).hide(0);
    } else {
        //显示进度条
        $("#loadingToast, .weui_mask").show();
        //提交表单请求
        var action = '/collectionquery';
        var form = $("<form></form>");
        form.attr('action', action);
        form.attr('method', 'post');
        var bookname = $("<input type='hidden' name='bookname'/>");
        bookname.attr('value', $("#bookname").val());
        var page = $("<input type='hidden' name='page'/>");
        page.attr('value', 1);
        form.append(bookname);
        form.append(page);
        form.appendTo("body");
        form.css('display', 'none');
        form.submit();
    }
}

//显示更多图书信息
function queryMore() {
    if ($("#currentPage").val() !== $("#sumPage").val()) {
        //显示进度条
        $("#loadingToast, .weui_mask").show();
        $.ajax({
            url: "/rest/collectionquery",
            data: {
                bookname: $("#queriedBookName").val(),
                page: (parseInt($("#currentPage").val()) + 1)
            },
            type: 'post',
            success: function (cetQueryResultEnum) {
                if (cetQueryResultEnum.success === true) {
                    //查询成功
                    var collectionList = cetQueryResultEnum.collectionList;
                    if (collectionList.length === 0) {
                        //隐藏进度条
                        $(document).ready(function () {
                            $("#loadingToast, .weui_mask").hide();
                        });
                        //查询结果为空,显示错误提示
                        weui.alert('查询图书出现异常', {
                            title: '错误提示',
                            buttons: [{
                                label: '确定',
                                type: 'primary'
                            }]
                        });
                    } else {
                        //隐藏进度条
                        $(document).ready(function () {
                            $("#loadingToast, .weui_mask").hide();
                        });
                        $("#currentPage").val(parseInt($("#currentPage").val()) + 1);
                        $("#sumPage").val(cetQueryResultEnum.sumPage);
                        var sessionStorageListString = sessionStorage.getItem("collectionList");
                        if (sessionStorageListString === null) {
                            //本地缓存为空
                            var newListJSONString = JSON.stringify(collectionList);
                            sessionStorage.setItem("collectionList", newListJSONString);
                        } else {
                            //本地缓存不为空
                            var sessionStorageList = JSON.parse(sessionStorageListString);
                            //扩充List缓存
                            var newList = [];
                            for (var j = 0; j < sessionStorageList.length; j++) {
                                newList.push(sessionStorageList[j]);
                            }
                            for (var k = 0; k < collectionList.length; k++) {
                                newList.push(collectionList[k]);
                            }
                            var newListJSONString = JSON.stringify(newList);
                            //重新缓存到本地
                            sessionStorage.setItem("collectionList", newListJSONString);
                        }
                        sessionStorage.setItem("currentPage", parseInt($("#currentPage").val()));
                        sessionStorage.setItem("sumPage", cetQueryResultEnum.sumPage);
                        for (var i = 0; i < collectionList.length; i++) {
                            var bookname = collectionList[i].bookname;
                            var author = collectionList[i].author;
                            var publishingHouse = collectionList[i].publishingHouse;
                            var detailURL = collectionList[i].detailURL.toString();
                            if (typeof (author) != "undefined" && typeof (publishingHouse) != "undefined") {
                                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                                    "<div class='weui-cell__bd'>" +
                                    "<p>" + bookname + "</p>" +
                                    "<p style='font-size: 13px;color: #999'>" + author + "</p>" +
                                    "<p style='font-size:13px;color:#999'>" + publishingHouse + "</p></div></a>");
                            } else if (typeof (author) == "undefined") {
                                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                                    "<div class='weui-cell__bd'>" +
                                    "<p>" + bookname + "</p>" +
                                    "<p style='font-size:13px;color:#999'>" + publishingHouse + "</p></div></a>");
                            } else if (typeof (publishingHouse) == "undefined") {
                                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                                    "<div class='weui-cell__bd'>" +
                                    "<p>" + bookname + "</p>" +
                                    "<p style='font-size:13px;color:#999'>" + publishingHouse + "</p></div></a>");
                            } else {
                                $(".weui-cells").append("<a id='" + detailURL + "' class='weui-cell' href='javascript:' style='color: black'>" +
                                    "<div class='weui-cell__bd'>" +
                                    "<p>" + bookname + "</p></div></a>");
                            }
                            setDetailURL(detailURL);
                        }
                        if ($("#currentPage").val() === $("#sumPage").val()) {
                            //已经是最后一页
                            $("#bottomLine").text("别拉了,已经到底了").show();
                            $("#bottomLine").removeAttribute("onclick");
                        } else {
                            $("#bottomLine").text("点击加载更多信息").show();
                            $("#bottomLine").attr("onclick", "queryMore();");
                        }
                    }
                } else {
                    $(document).ready(function () {
                        //隐藏进度条
                        $("#loadingToast, .weui_mask").hide();
                        //显示错误提示
                        weui.alert(cetQueryResultEnum.message, {
                            title: '错误提示',
                            buttons: [{
                                label: '确定',
                                type: 'primary'
                            }]
                        });
                    });
                }
            },
            error: function (cetQueryResultEnum) {
                $(document).ready(function () {
                    //隐藏进度条
                    $("#loadingToast, .weui_mask").hide();
                    //显示错误提示
                    weui.alert('查询馆藏失败,请检查网络连接', {
                        title: '错误提示',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                });
            }
        });
    } else {
        $(document).ready(function () {
            //隐藏进度条
            $("#loadingToast, .weui_mask").hide();
            //显示错误提示
            weui.alert('查询页数超出范围', {
                title: '错误提示',
                buttons: [{
                    label: '确定',
                    type: 'primary'
                }]
            });
        });
    }
}