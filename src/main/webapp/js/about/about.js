//检测浏览器窗口大小并显示对应下载页
$(function () {
    if (document.body.clientWidth > 500) {
        $("#desktop").show();
    } else {
        $("#phone").show();
    }
});

//弹出Cookie政策提示
$(function () {
    if (!localStorage.getItem("cookiePolicy")) {
        $("#rnw_cookies_banner").show();
    }
});

//下载客户端APP应用
function downloadAPP() {
    $.actions({
        title: "请选择客户端平台",
        actions: [{
            text: "Android",
            onClick: function () {
                if (navigator.userAgent.toLowerCase().match(/MicroMessenger/i) == "micromessenger") {
                    //正在使用微信访问应用，跳转到腾讯应用宝进行下载
                } else {
                    //跳转到CDN或GooglePlay平台进行下载
                }
            }
        }, {
            text: "iOS",
            onClick: function () {
                //跳转到AppStore应用页面
            }
        }, {
            text: "UWP",
            onClick: function () {
                //跳转到WindowsStore应用页面
            }
        }]
    });
}

//显示微信公众号和微信小程序二维码
function showWechatQRCode() {

}