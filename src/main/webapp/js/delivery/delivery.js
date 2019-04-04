$(function () {
    FastClick.attach(document.body);
    loadAuthenticationState();
});

//检测Input文字长度是否超过限制
function inputLengthCheck(str, maxLen) {
    if (str.value.length > maxLen) {
        str.value = str.value.substr(0, maxLen);
    }
}

//检测Input文字长度是否超过限制并进行实时字数反馈
function textAreaInputLengthCheck(str, maxLen) {
    inputLengthCheck(str, maxLen);
    $("#length").text(str.value.length);
}

//检查实名认证状态
function loadAuthenticationState() {
    $.ajax({
        url: "/api/authentication",
        method: "GET",
        success: function (result) {
            if (result.success) {
                if (!result.data) {
                    //未完成实名认证不可使用全民快递
                    $.alert({
                        title: '未完成实名认证',
                        text: '你未完成实名认证，暂不可以使用全民快递功能，请先前往个人中心进行实名认证',
                        onOK: function () {
                            history.go(-1);
                        }
                    });
                }
            } else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
            }
        },
        error: function (result) {
            if (result.status) {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text('网络连接失败,请检查网络连接').show().delay(2000).hide(0);
            }
        }
    });
}

//检查下单属性并提交
function submitForm() {
    if ($("#name").val() == '' || $("#name").val() > 10) {
        $(".weui_warn").text('姓名长度不合法').show().delay(2000).hide(0);
    } else if ($("#number").val() == '' || $("#number").val().length != 11) {
        $(".weui_warn").text('学号长度不合法').show().delay(2000).hide(0);
    } else if ($("#phone").val() == '' || $("#phone").val().length > 11) {
        $(".weui_warn").text('手机号长度不合法').show().delay(2000).hide(0);
    } else if ($("#price").val() == '' || Number(parseFloat($("#price").val())) == 0 || Number(parseFloat($("#price").val())).toFixed(2) > 99) {
        $(".weui_warn").text('请输入正确的报酬').show().delay(2000).hide(0);
    } else if ($("#company").val() == '' || $("#company").val().length > 10) {
        $(".weui_warn").text('快递公司名称长度不合法').show().delay(2000).hide(0);
    } else if ($("#address").val() == '' || $("#address").val().length > 50) {
        $(".weui_warn").text('收件地址长度不合法').show().delay(2000).hide(0);
    } else if ($("#remarks").val().length > 100) {
        $(".weui_warn").text('备注长度不合法').show().delay(2000).hide(0);
    } else {
        $("#loadingToast, .weui_mask").show();
        $(".weui-btn_primary").attr("disabled", true);
        $.ajax({
            url: '/api/delivery/order',
            method: 'POST',
            data: {
                name: $("#name").val(),
                number: $("#number").val(),
                phone: $("#phone").val(),
                price: parseFloat($("#price").val()),
                company: $("#company").val(),
                address: $("#address").val(),
                remarks: $("#remarks").val()
            },
            success: function (result) {
                $("#loadingToast, .weui_mask").hide();
                $(".weui-btn_primary").attr("disabled", false);
                if (result.success) {
                    window.location.href = '/delivery';
                } else {
                    $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                }
            },
            error: function (result) {
                $("#loadingToast, .weui_mask").hide();
                $(".weui-btn_primary").attr("disabled", false);
                if (result.status) {
                    $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                } else {
                    $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                }
            }
        })
    }
}

//加载快递代收订单信息
function loadDeliveryOrder() {
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/api/delivery/order/start/' + $(".weui_cells a").length + '/size/10',
        method: 'GET',
        success: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                if (result.data.length == 0) {
                    $("#bottomLine").text("已经到底了");
                    $("#bottomLine").attr("onclick", "javascript:void(0)");
                    $("#bottomLine").show();
                } else {
                    $("#bottomLine").text("点击加载更多");
                    $("#bottomLine").show();
                }
                for (var i = 0; i < result.data.length; i++) {
                    $(".weui_cells")
                        .append("<a class='weui-cell weui-cell_access' href='/delivery/order/id/" + result.data[i].orderId + "'>" +
                            "<div class='weui-cell__bd'><label class='weui-label'><p>" + result.data[i].company + "</p>" +
                            "<p style='overflow: hidden;text-overflow:ellipsis;white-space: nowrap;font-size:13px;color:#999'>" + result.data[i].address + "</p>" +
                            "</div><div class='weui-cell__ft'> " + result.data[i].price.toFixed(2) + "元</div></a>");
                }
            } else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
            }
        },
        error: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.status) {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
            }
        }
    })
}

//删除订单
function deleteOrder(orderId) {
    $.confirm({
        title: '删除订单信息',
        text: '将删除该订单，订单信息无法恢复，你确定吗？',
        onOK: function () {
            $("#loadingToast, .weui_mask").show();
            $.ajax({
                url: '/api/delivery/order/id/' + orderId,
                method: 'DELETE',
                success: function (result) {
                    if (result.success) {
                        $("#loadingToast, .weui_mask").hide();
                        $.alert({
                            title: '订单已删除',
                            text: '删除订单成功',
                            onOK: function () {
                                history.go(-1);
                            }
                        });
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.status) {
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            })
        }
    });
}

//用户接单
function acceptOrder(orderId) {
    $("#loadingToast, .weui_mask").show();
    $.ajax({
        url: '/api/delivery/acceptorder',
        method: 'POST',
        data: {
            orderId: orderId
        },
        success: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.success) {
                $.alert({
                    title: '接单成功',
                    text: '接单成功，点击确定刷新订单信息',
                    onOK: function () {
                        window.location.reload();
                    }
                });
            } else {
                $(".weui_warn").text(result.message).show().delay(2000).hide(0);
            }
        },
        error: function (result) {
            $("#loadingToast, .weui_mask").hide();
            if (result.status) {
                $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
            }
        }
    })
}

//完成快递代收交易
function finishTrade(tradeId) {
    $.confirm({
        title: '确认快递已交付',
        text: '将确认快递交付，并完成当前快递代收交易，你确定吗？',
        onOK: function () {
            $("#loadingToast, .weui_mask").show();
            $.ajax({
                url: '/api/delivery/trade/id/' + tradeId + '/finishtrade',
                method: 'POST',
                success: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.success) {
                        $.alert({
                            title: '交易完成',
                            text: '交易已完成',
                            onOK: function () {
                                history.go(-1);
                            }
                        });
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function (result) {
                    $("#loadingToast, .weui_mask").hide();
                    if (result.status) {
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络访问异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            })
        }
    });
}