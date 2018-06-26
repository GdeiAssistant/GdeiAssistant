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
    <link rel="stylesheet" href="/css/secret/secret-publish.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
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
        <div class="edit">
            <textarea name="content" id="text" maxlength="100" autofocus placeholder="说个小秘密" v-align="center"
                      max-lenght="100"></textarea>
            <div class="length">100</div>
        </div>
    </form>
</div>

<div class="bar">
    <i></i>
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

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>

<script type="text/javascript">

    //树洞主题代码
    var rand;

    $(".themes>div").height($(".themes>div").width());
    rand = Math.ceil(Math.random() * 12);
    $("textarea").css("padding-top", ($(".edit").height() - $("textarea").height()) / 2);
    $(".form").addClass("theme" + rand);
    $(".selected").appendTo($(".themes .theme" + rand));
    if (rand != 1) {
        $(".form").addClass("font-normal");
        $(".back1").removeClass("back1").addClass("back");
    } else {
        $(".form").addClass("font-white");
        $(".back").removeClass("back").addClass("back1");
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
            $(".form").css("color", "#000");
            $(".back").removeClass("back").addClass("back1");
        } else {
            $(".form").css("color", "#fff");
            $(".back1").removeClass("back1").addClass("back");
        }
    });

    //提交树洞信息
    $(".btn").on("click", function () {
        if ($.trim($("textarea").val()).length <= 0) {
            $(".weui_warn").text("树洞内容不能为空！").show().delay(2000).hide(0);
            return false;
        }
        else if ($("textarea").val().length > 100) {
            $(".weui_warn").text("树洞内容长度超过限制！").show().delay(2000).hide(0);
            return false;
        }
        $.post("/rest/secret/info", $("form").serialize() + "&theme=" + rand, function (data) {
            if (data.success === true) {
                window.location.href = "/secret";
            } else {
                $(".weui_warn").text(data.errorMessage).show().delay(2000).hide(0);
            }
        });
    });

</script>

</html>
