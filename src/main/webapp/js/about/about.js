//检测浏览器窗口大小并显示对应下载页
$(function () {
    if (document.body.clientWidth > 500) {
        $("#desktop").show();
    } else {
        $("#phone").show();
    }
});

//获取客户端下载地址
$(function () {
    $.ajax({
        url: '/download/android',
        type: 'get',
        success: function (result) {
            $("#android_url").val(result.url);
        }
    });
    $.ajax({
        url: '/download/android/googleplay',
        type: 'get',
        success: function (result) {
            $("#android_google_play_url").val(result.url);
        }
    });
    $.ajax({
        url: '/download/android/amazon',
        type: 'get',
        success: function (result) {
            $("#android_amazon_url").val(result.url);
        }
    });
    $.ajax({
        url: '/download/uwp',
        type: 'get',
        success: function (result) {
            $("#uwp_url").val(result.url);
        }
    })
});

//用户使用微信浏览器打开时，提示用户使用系统浏览器打开下载链接
function isWechat() {
    return navigator.userAgent.toLowerCase().match(/MicroMessenger/i) == "micromessenger"
}

function androidDownload() {
    if (isWechat()) {
        $(".wxtip").show();
    } else {
        if ($("#android_url").val() !== '#') {
            window.location.href = $("#android_url").val();
        }
    }
}

function androidGooglePlayDownload() {
    if ($("#android_google_play_url").val() !== '#') {
        window.location.href = $("#android_google_play_url").val();
    }
}

function androidAmazonDownload() {
    if ($("#android_amazon_url").val() !== '#') {
        window.location.href = $("#android_amazon_url").val();
    }
}

function iOSDownload() {
    alert("暂无iOS版本");
}

function UWPDownload() {
    if ($("#uwp_url").val() !== '#') {
        window.location.href = $("#uwp_url").val();
    }
}

function QuickApp() {
    alert("暂无快应用版本");
}

function AlipayApp() {
    alert("暂无支付宝小程序版本");
}

function Wechat() {
    $.photoBrowser({
        items: [
            {
                image: "/img/download/wechat_qrcode.jpg",
            }
        ],
        onOpen: function () {
            $(".photo-container img").width($(window).width());
        }
    }).open();
}

function WechatApp() {
    $.photoBrowser({
        items: [
            {
                image: "/img/download/wechatapp_qrcode.jpg",
            }
        ],
        onOpen: function () {
            $(".photo-container img").width($(window).width());
        }
    }).open();
}