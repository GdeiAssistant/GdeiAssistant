<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/18
  Time: 16:45
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0,  minimum-scale=1.0, maximum-scale=1.0">
    <title>广东第二师范学院树洞</title>
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/secret/secret-publish.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/recorder.mp3.min.js"></script>
    <script>

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });


    </script>
</head>
<body>

<!-- 树洞发布框 -->
<div class="form">
    <form>
        <header>
            <i class="back" onclick="history.back()"></i>
            <span>小秘密</span>
            <label class="btn">发布</label>
        </header>
        <div class="edit" style="text-align: center">
            <!-- 语音树洞 -->
            <div id="voice">
                <!-- 语音图标 -->
                <img id="record" width="50px" height="50px" src="/img/secret/voice_normal.png">
                <br>
                <text id="voice_tip">长按开始录音，最长不超过60秒</text>
            </div>
            <!-- 文字树洞 -->
            <div id="word" style="display: none;">
                <!-- 文本输入框 -->
                <textarea name="content" id="text" maxlength="100" autofocus placeholder="说个小秘密" v-align="center"
                          max-lenght="100"></textarea>
                <!-- 剩余可输入字符数 -->
                <div class="length">100</div>
            </div>
        </div>
    </form>
</div>

<div class="bar">
    <div onclick="replayRecord()">
        <img id="voice_button" width="20px" height="20px" style="margin-right:3px;margin-top: 3px;float: left"
             src="/img/secret/init.png"/>
        <p id="voice_state" style="display: block;float: left">未录音</p>
    </div>
    <i style="float: right"></i>
    <div id="voice_volume"
         style="height:20px;margin-top:3px;margin-left:15px;margin-right:15px;width:100px;background:#cdcdcd;float: right">
        <div id="volume" style="width:0"></div>
    </div>
</div>

<!-- 主题选择 -->
<div class="themes">
    <div class="theme1"><i class="selected"></i></div>
    <div class="theme2"></div>
    <div class="theme3"></div>
    <div class="theme4"></div>
    <div class="theme5"></div>
    <div class="theme6"></div>
</div>

<div class="themes">
    <div class="theme7"></div>
    <div class="theme8"></div>
    <div class="theme9"></div>
    <div class="theme10"></div>
    <div class="theme11"></div>
    <div class="theme12"></div>
</div>
<div class="attach"></div>

<!-- 切换到文字树洞 -->
<div id="switchToWord" style="margin-top: 1rem;color:grey;text-align:center;display: block">
    切换到<p style="display:inline;color: deepskyblue" onclick="switchToWord()">文字树洞</p>
    ，用文字分享你的小秘密
</div>

<!-- 切换到语音树洞 -->
<div id="switchToVoice" style="display:none;margin-top: 1rem;color:grey;text-align:center">
    切换到<p style="display:inline;color: deepskyblue" onclick="switchToVoice()">语音树洞</p>
    ，用语音分享你的小秘密
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>

<script type="text/javascript">

    //树洞信息类型，0为文字树洞，1为语音树洞
    let type = 1;

    //切换到文字树洞
    function switchToWord() {
        $("#switchToWord").hide();
        $("#switchToVoice").show();
        $("#word").show();
        $("#voice").hide();
        $("#voice_button").hide();
        $("#voice_state").hide();
        $("#voice_volume").hide();
        type = 0;
    }

    //切换到语音树洞
    function switchToVoice() {
        $("#switchToVoice").hide();
        $("#switchToWord").show();
        $("#word").hide();
        $("#voice").show();
        $("#voice_button").show();
        $("#voice_state").show();
        $("#voice_volume").show();
        type = 1;
    }

    $(function () {
        record.open(function () {
            //授权麦克风权限成功
        }, function (errMsg) {
            //未授权或不支持
            console.log(errMsg);
            $(".weui_warn").text("用户拒绝了麦克风权限或你的浏览器不支持相关API").show().delay(2000).hide(0);
        });
    });

    //禁止鼠标点击右键
    document.oncontextmenu = function () {
        return false
    };

    //禁用长按弹出菜单
    window.addEventListener('contextmenu', function (e) {
        e.preventDefault();
    });

    //录音音频对象
    let voice;

    //录音实时音量
    let volume = 0;

    let record = Recorder({
        type: "mp3",
        bitRate: 16,
        sampleRate: 16000,
        bufferSize: 8192,
        onProcess: function (buffer, powerLevel, bufferDuration) {
            volume = powerLevel;
        }
    });

    //录音中标记，用于超时自动停止录音，标记不处理松手事件
    let recording = 0;

    //定时刷新显示实时音量
    let volumeInterval;

    //定时刷新显示剩余录音时间
    let remainTimeInterval;

    //超时自动停止录音
    let recordTimeOut;

    //音频对象
    let audio = document.createElement("AUDIO");

    //开始录音
    function startRecordVoice() {
        //申请授权，开启录音功能
        record.open(function () {
            recording = recording + 1;
            //授权成功，可以开始录音
            audio.src = undefined;
            record.start();
            //显示录音音量
            if (volumeInterval) {
                clearInterval(volumeInterval);
            }
            volumeInterval = setInterval(function () {
                $("#volume").css("width", volume + "%");
            }, 50);
            //设置最长录制60秒
            recordTimeOut = setTimeout(stopRecordVoice, 60000);
            //显示录音状态，并提示剩余录音时间
            $("#voice_button").attr("src", "/img/secret/record.png");
            let remainTime = 60;
            if (remainTimeInterval) {
                clearInterval(remainTimeInterval);
            }
            remainTimeInterval = setInterval(function () {
                remainTime = remainTime - 1;
                $("#voice_state").text("正在录音，还剩" + remainTime + "秒");
            }, 1000);
        }, function (errMsg) {
            //未授权或不支持
            console.log(errMsg);
            $(".weui_warn").text("用户拒绝了麦克风权限或你的浏览器不支持相关API").show().delay(2000).hide(0);
        });
    }

    //停止录音
    function stopRecordVoice() {
        recording = recording - 1;
        //重置音量
        clearInterval(volumeInterval);
        $("#volume").css("width", "0");
        //重置剩余时间
        clearInterval(remainTimeInterval);
        clearTimeout(recordTimeOut);
        //等待音频编码完成
        $("#voice_button").attr("src", "/img/secret/init.png");
        $("#voice_state").text("正在编码音频...");
        let loading = weui.loading("编码音频中");
        $(".btn").attr("disabled", true);
        record.stop(function (blob, duration) {
            loading.hide();
            $(".btn").attr("disabled", false);
            if (duration < 1000) {
                $(".weui_warn").text("录音时间太短，请重试").show().delay(2000).hide(0);
                //设置录音状态
                $("#voice_button").attr("src", "/img/secret/init.png");
                $("#voice_state").text("未录音");
            } else {
                //缓存录音对象
                voice = blob;
                audio.src = URL.createObjectURL(blob);
                audio.load();
                //设置录音状态
                $("#voice_button").attr("src", "/img/secret/play.png");
                $("#voice_state").text("播放录音");
            }
        }, function (msg) {
            loading.hide();
            $(".btn").attr("disabled", false);
            if (msg === '未开始录音') {
                $(".weui_warn").text("录音时间太短，请重试").show().delay(2000).hide(0);
            } else if (msg === '未采集到录音') {
                $(".weui_warn").text("未采集到录音，请重试").show().delay(2000).hide(0);
            } else {
                $(".weui_warn").text("录音失败，请确认你已授权麦克风相关权限").show().delay(2000).hide(0);
            }
            //重置录音对象
            voice = undefined;
            audio.src = undefined;
            audio.load();
            //设置录音状态
            $("#voice_button").attr("src", "/img/secret/init.png");
            $("#voice_state").text("未录音");
        });
    }

    //播放或暂停播放录音
    function replayRecord() {
        if (audio.src) {
            if (audio.paused) {
                //播放音频
                $("#voice_button").attr("src", "/img/secret/pause.png");
                $("#voice_state").text("正在播放");
                audio.play();
                audio.onended = function () {
                    $("#voice_button").attr("src", "/img/secret/play.png");
                    $("#voice_state").text("播放录音");
                };
            } else {
                //暂停播放音频
                $("#voice_button").attr("src", "/img/secret/play.png");
                $("#voice_state").text("暂停");
                audio.pause();
            }
        }
    }

    //监听用户长按和松手动作
    $("#voice").on('touchstart', function () {
        if (recording === 0) {
            //更改语音图标和提示文字的颜色
            $("#voice_tip").css("color", "#707070");
            $("#record").attr("src", "/img/secret/voice_pressed.png");
            startRecordVoice();
        }
    }).on('touchend', function () {
        if (recording > 0) {
            //更改语音图标和提示文字的颜色
            if (rand === "1") {
                $("#record").attr("src", "/img/secret/voice_normal_white.png");
                $("#voice_tip").css("color", "#bfbfbf");
            } else {
                $("#record").attr("src", "/img/secret/voice_normal.png");
                $("#voice_tip").css("color", "#fff");
            }
            stopRecordVoice();
        }
    }).on('mousedown', function () {
        if (recording === 0) {
            //更改语音图标和提示文字的颜色
            $("#voice_tip").css("color", "#707070");
            $("#record").attr("src", "/img/secret/voice_pressed.png");
            startRecordVoice();
        }
    }).on('mouseup', function () {
        if (recording > 0) {
            //更改语音图标和提示文字的颜色
            if (rand === "1") {
                $("#voice_tip").css("color", "#bfbfbf");
                $("#record").attr("src", "/img/secret/voice_normal_white.png");
            } else {
                $("#voice_tip").css("color", "#fff");
                $("#record").attr("src", "/img/secret/voice_normal.png");
            }
            stopRecordVoice();
        }
    });

    //树洞主题代码
    var rand;

    $(".themes>div").height($(".themes>div").width());
    rand = Math.ceil(Math.random() * 12);
    $("textarea").css("padding-top", ($(".edit").height() - $("textarea").height()) / 2);
    $(".form").addClass("theme" + rand);
    $(".selected").appendTo($(".themes .theme" + rand));
    if (rand != 1) {
        $(".form").addClass("font-normal");
        $("#voice_tip").css("color", "#fff");
        $(".back1").removeClass("back1").addClass("back");
        $("#record").attr("src", "/img/secret/voice_normal.png");
    } else {
        $(".form").addClass("font-white");
        $("#voice_tip").css("color", "#bfbfbf");
        $(".back").removeClass("back").addClass("back1");
        $("#record").attr("src", "/img/secret/voice_pressed.png");
    }

    var i = true;
    $(".bar i").on("click", function () {
        $(this).toggleClass("gray-pallet");
        if (i) {
            $(".themes").css({
                "display": "-moz-box",
                "display": "-webkit-flex",
                "display": "-moz-flex",
                "display": "-ms-flexbox",
                "display": "flex",
                "display": "-webkit-box"
            }).find("div").height($(".themes div").width());
        } else {
            $(".themes").css("display", "none");
        }
        i = !i;
    });

    //树洞信息输入框字数必须少于100字
    $("textarea").on("keyup", function () {
        var length = $("textarea").val().length;
        $(".length").text(100 - length);
    });

    //更改主题
    $(".themes").on("click touchend", "div", function () {
        $(".form").css("background-color", $(this).css("background-color"));
        $(".selected").appendTo($(this));
        rand = $(this).attr("class").substr(5);
        if ($(this).css("background-color") === "rgb(255, 255, 255)") {
            $(".form").css("color", "#bfbfbf");
            $("#voice_tip").css("color", "#bfbfbf");
            $(".back").removeClass("back").addClass("back1");
            $("#record").attr("src", "/img/secret/voice_normal_white.png");
        } else {
            $(".form").css("color", "#fff");
            $("#voice_tip").css("color", "#fff");
            $(".back1").removeClass("back1").addClass("back");
            $("#record").attr("src", "/img/secret/voice_normal.png");
        }
    });

    //提交树洞信息
    $(".btn").on("click", function () {
        if (type === 0) {
            //文字树洞提交
            if ($.trim($("textarea").val()).length <= 0) {
                $(".weui_warn").text("树洞内容不能为空！").show().delay(2000).hide(0);
                return false;
            } else if ($("textarea").val().length > 100) {
                $(".weui_warn").text("树洞内容长度超过限制！").show().delay(2000).hide(0);
                return false;
            }
            let loading = weui.loading('提交中');
            $(".btn").attr("disabled", true);
            $.ajax({
                url: "/api/secret/info",
                type: "POST",
                data: $("form").serialize() + "&theme=" + rand + "&type=" + 0,
                success: function (result) {
                    if (result.success === true) {
                        window.location.href = "/secret";
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                    loading.hide();
                    $(".btn").attr("disabled", false);
                },
                error: function (result) {
                    loading.hide();
                    $(".btn").attr("disabled", false);
                    if (result.status) {
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            });
        } else if (type === 1) {
            //语音树洞提交
            if (voice) {
                let loading = weui.loading('提交中');
                $(".btn").attr("disabled", true);
                let formData = new FormData();
                formData.append('voice', voice);
                formData.append('theme', rand);
                formData.append('type', 1);
                //上传音频和提交树洞信息
                $.ajax({
                    url: "/api/secret/info",
                    type: "POST",
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (result) {
                        if (result.success) {
                            window.location.href = "/secret";
                        } else {
                            $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                        }
                        loading.hide();
                        $(".btn").attr("disabled", false);
                    },
                    error: function (result) {
                        loading.hide();
                        $(".btn").attr("disabled", false);
                        if (result.status) {
                            $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                        } else {
                            $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                        }
                    }
                });
            } else {
                $(".weui_warn").text("未采集到任何录音信息").show().delay(2000).hide(0);
            }
        }

    });

</script>

</html>
