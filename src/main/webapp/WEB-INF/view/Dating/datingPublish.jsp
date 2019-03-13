<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>广东第二师范学院卖室友</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" type="text/css" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" type="text/css" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/dating/global.css">
    <link rel="stylesheet" href="/css/dating/layout.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>

        var imageBase64;

        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });

        //监听图片上传
        $(function () {

            // 允许上传的图片类型
            var allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif'];

            // 图片最大大小为5MB
            var maxSize = 1024 * 1024 * 5;

            // 图片最大宽度
            var maxWidth = 720;

            $("#image").on("change", function (event) {
                var files = event.target.files;

                // 如果没有选中文件，直接返回
                if (files.length === 0) {
                    return;
                }

                var file = files[0];
                var reader = new FileReader();

                // 如果类型不在允许的类型范围内
                if (allowTypes.indexOf(file.type) === -1) {
                    showErrorTip("不合法的图片文件类型", "上传错误");
                    return;
                }

                if (file.size > maxSize) {
                    showErrorTip("图片文件不能超过5MB", "文件过大");
                    return;
                } else {
                    reader.readAsDataURL(file);
                    reader.onload = function (e) {
                        var img = new Image();
                        img.src = e.target.result;
                        img.onload = function () {
                            // 不要超出最大宽度
                            var w = Math.min(maxWidth, img.width);
                            // 高度按比例计算
                            var h = img.height * (w / img.width);
                            var canvas = document.createElement('canvas');
                            var ctx = canvas.getContext('2d');
                            // 设置 canvas 的宽度和高度
                            canvas.width = w;
                            canvas.height = h;
                            ctx.drawImage(img, 0, 0, w, h);
                            // 返回一个包含图片展示的 Data URI
                            var base64 = canvas.toDataURL('image/jpeg', 0.8);
                            // 缓存图片Base64编码
                            imageBase64 = base64;
                            //显示图片预览图
                            $("#showimg").attr("src", imageBase64);
                            $(".uploading").hide();
                        }
                    };
                }
            });
        });

        function showErrorTip(message) {
            $(".weui_warn").text(message).show().delay(2000).hide(0);
        }

        //发布卖室友信息
        function publishDating() {
            if (typeof (imageBase64) == 'undefined') {
                showErrorTip("上传一张美美的照片吧");
            } else if ($("#kickname").val().length == 0 || $("#kickname").val().length > 15) {
                showErrorTip("昵称长度不合法");
            } else if ($("#grade_value").val() == '') {
                showErrorTip("年级未选择");
            } else if ($("#area_value").val() == '') {
                showErrorTip("性别未选择");
            } else if ($("#faculty").val().length == 0 || $("#faculty").val().length > 12) {
                showErrorTip("专业长度不合法");
            } else if ($("#hometown").val().length == 0 || $("#hometown").val().length > 10) {
                showErrorTip("家乡长度不合法");
            } else if ($("#qq").val().length == 0 && $("#wechat").val().length == 0) {
                showErrorTip("QQ号码和微信至少填写一个");
            } else if ($("#qq").val().length > 15 || $("#wechat").val().length > 20) {
                showErrorTip("联系方式长度不合法");
            } else if ($("#content").val().length == 0) {
                showErrorTip("填一下你心目中的那个TA吧");
            } else if ($("#content").val().length > 100) {
                showErrorTip("心动条件长度不合法");
            } else {
                var formData = new FormData();
                var canvas = document.createElement('canvas');
                var ctx = canvas.getContext('2d');
                var image = new Image();
                image.src = imageBase64;
                // 设置 canvas 的宽度和高度
                canvas.width = image.width;
                canvas.height = image.height;
                ctx.drawImage(image, 0, 0, image.width, image.height);
                var base64 = canvas.toDataURL('image/jpeg', 1);
                base64 = base64.split(',')[1];
                base64 = window.atob(base64);
                var ia = new Uint8Array(base64.length);
                for (var j = 0; j < base64.length; j++) {
                    ia[j] = base64.charCodeAt(j);
                }
                formData.append('image', new Blob([ia], {type: "image/jpg"}));
                formData.append('kickname', $("#kickname").val());
                formData.append('grade', $("#grade_value").val());
                formData.append('area', $("#area_value").val());
                formData.append('faculty', $("#faculty").val());
                formData.append('hometown', $("#hometown").val());
                formData.append('content', $("#content").val());
                if ($("#qq").val() != '') {
                    formData.append('qq', $("#qq").val());
                }
                if ($("#wechat").val() != '') {
                    formData.append('wechat', $("#wechat").val());
                }

                //防止用户重复点击提交
                $(".circleBtn").attr("disabled", true);

                //显示等待动画
                var loading = weui.loading('提交中');

                $.ajax({
                    url: '/dating/profile',
                    type: 'post',
                    data: formData,
                    processData: false,
                    contentType: false,
                    success: function (result) {
                        loading.hide();
                        if (result.success === true) {
                            window.location.href = '/dating';
                        } else {
                            $(".circleBtn").attr("disabled", false);
                            showErrorTip(result.message);
                        }
                    },
                    error: function () {
                        $(".circleBtn").attr("disabled", false);
                        loading.hide();
                        showErrorTip("网络异常，请检查网络连接");
                    }
                });
            }
        }

        //显示分区选择框
        function showAreaSelect() {
            var areaPicker = [
                {
                    label: '小姐姐',
                    value: 0,
                },
                {
                    label: '小哥哥',
                    value: 1,
                }];
            weui.picker(areaPicker, {
                defaultValue: ['1'],
                onConfirm: function (area) {
                    $("#area_value").val(area[0].value);
                    $("#area").val(area[0].label);
                }
            });
        }

        //显示年级选择框
        function showGradeSelect() {
            var gradePicker = [
                {
                    label: '大一',
                    value: 1,
                },
                {
                    label: '大二',
                    value: 2
                },
                {
                    label: '大三',
                    value: 3
                },
                {
                    label: '大四',
                    value: 4,
                }
            ];
            weui.picker(gradePicker, {
                defaultValue: ['1'],
                onConfirm: function (grade) {
                    $("#grade_value").val(grade[0].value);
                    $("#grade").val(grade[0].label);
                }
            });
        }

    </script>
</head>
<body>

<div class="panel-overlay"></div>

<div class="warp">

    <div class="head" onclick="window.location.href='/dating'">
        <div class="logo"><span class="t">卖室友</span></div>
    </div>

    <div class="conterInfo">

        <div class="releaseBox">

            <div class="releaseName">发布信息
                <div class="yuan" style="top: -5.5px; left: -5.5px;"></div>
                <div class="yuan" style="top: -5.5px; right: -5.5px;"></div>
                <div class="yuan" style="top: -5.5px; left: 50%; margin-left: -5.5px;"></div>
            </div>

            <form>
                <div class="Photo" id="imgshow">
                    <img id="showimg"/>
                    <div class="uploading" style="top:11rem ; width:100%;position:absolute;">
                        <input type="button" class="circleBtn" value="上传"/>
                        <input name="image" id="image"
                               style="opacity:0; height: 5rem;position: absolute;top: 0rem; width: 100%;" type="file"
                               class="upload" accept="image/*"/>
                    </div>
                </div>

                <div class="formInput">
                    <p><input type="text" class="inputBox" id="kickname" placeholder="请输入你的昵称"/></p>
                    <p>
                        <input type="hidden" id="grade_value" value=""/>
                        <input type="text" readOnly="readOnly" class="inputBox" id="grade" placeholder="请选择你的年级"
                               onclick="showGradeSelect()"/>
                    </p>
                    <p>
                        <input type="hidden" id="area_value" value=""/>
                        <input type="text" readOnly="readOnly" class="inputBox" id="area" placeholder="请选择你的性别"
                               onclick="showAreaSelect()"/>
                    </p>
                    <p><input type="text" class="inputBox" id="faculty" placeholder="请输入你的专业"/></p>
                    <p><input type="text" class="inputBox" id="hometown" placeholder="请输入你的家乡"/></p>
                    <p><input type="text" class="inputBox" id="qq" placeholder="请输入你的QQ"/></p>
                    <p><input type="text" class="inputBox" id="wechat" placeholder="请输入你的微信"/></p>
                </div>
                <div style="text-align: center;margin-bottom: 10px;margin-left: 15px;margin-right: 15px">
                    <span>在接受撩一下请求前，QQ和微信不会公开显示</span><br>
                    <span style="color:red">请勿违规盗用他人照片或冒充他人，欢迎举报监督</span>
                </div>
                <div class="textBox">
                    <textarea class="textarea" id="content" placeholder="什么样的TA会让你心动呢？谈谈你的理想对象，不超过100字"></textarea>
                    <input type="button" class="circleBtn" value="发布" onclick="publishDating()"/>
                    <div class="yuan" style="top: -5.5px; left: -5.5px;"></div>
                    <div class="yuan" style="top: -5.5px; right: -5.5px;"></div>
                </div>
            </form>

        </div>
    </div>

</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

</body>
</html>
