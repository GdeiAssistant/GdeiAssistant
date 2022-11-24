//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
    //加载最近登录记录
    getIPAddressRecord();
});

//获取最近登录记录
function getIPAddressRecord() {
    let loading = weui.loading('正在加载最近登录记录');
    $.ajax({
        url: '/api/ip/start/0/size/10',
        method: 'GET',
        success: function (result) {
            loading.hide();
            if (result.success === true) {
                $(".recent-login-record").append("<div class='weui-cells__title'>最近一周的登录记录</div>").append("<div class='weui-cells'></div>");
                for (i = 0; i < result.data.length; i++) {
                    let location = "";
                    if (result.data[i].country != null && result.data[i].country != '') {
                        location = location + result.data[i].country;
                    }
                    if (result.data[i].province != null && result.data[i].province != '') {
                        location = location + result.data[i].province;
                    }
                    if (result.data[i].city != null && result.data[i].city != '') {
                        location = location + result.data[i].city;
                    }
                    if (result.data[i].network != null && result.data[i].network != '') {
                        location = location + result.data[i].network;
                    }
                    let time = new Date(result.data[i].time);
                    let year = time.getFullYear() + '-';
                    let month = (time.getMonth() + 1 < 10 ? '0' + (time.getMonth() + 1) : time.getMonth() + 1) + '-';
                    let date = time.getDate() + ' ';
                    let hour = (time.getHours() < 10 ? '0' + time.getHours() : time.getHours()) + ':';
                    let minute = time.getMinutes() + ':';
                    let second = time.getSeconds();
                    $(".recent-login-record .weui-cells").append("<div class='weui-cell'><div class='weui-cell__bd'>" +
                        "<img src='/img/ipaddress/location.png' style='width: 20px; margin-right: 10px; display: block; float: left'/>" +
                        "<span>" + location + "</span>" +
                        "<div class='weui-cell__desc' style='margin-top: 5px'><div class='weui-cell__bd'>" +
                        "<img src='/img/ipaddress/clock.png' style='width: 20px; margin-right: 10px; display: block; float: left'/>" +
                        "<span>" + year + month + date + hour + minute + second + "</span></div></div></div></div>");
                }
            }
        },
        error: function () {
            loading.hide();
        }
    });
}