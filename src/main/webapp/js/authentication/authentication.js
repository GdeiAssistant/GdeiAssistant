let images = [];

// 允许上传的图片类型
let allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif'];

// 图片最大大小为5MB
let maxSize = 1024 * 1024 * 5;

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//切换到中国居民身份证实名认证界面
function switchToMainlandAuthentication() {
    $(".hmtf_authentication").hide();
    $(".mainland_authentication").show();
}

//切换到港澳台和海外用户实名认证界面
function switchToHMTFAuthentication() {
    $(".mainland_authentication").hide();
    $(".hmtf_authentication").show();
}

//上传证件照片
$(function () {
    let tmpl = '<li class="weui-uploader__file" style="background-image:url(#url#)"></li>',
        $gallery = $("#gallery"), $galleryImg = $("#hmtf-idcard-gallery-image"),
        $uploaderInput = $("#hmtf-idcard-uploader-input"),
        $uploaderFiles = $("#hmtf-idcard-uploader-files");

    $uploaderInput.on("change", function (event) {

        var files = event.target.files;

        // 如果没有选中文件，直接返回
        if (files.length === 0) {
            return;
        }

        var file = files[0];
        var reader = new FileReader();

        // 如果类型不在允许的类型范围内
        if (allowTypes.indexOf(file.type) === -1) {
            $.alert("不合法的图片文件类型", "上传错误");
            return;
        }

        if (file.size > maxSize) {
            $.alert("图片文件不能超过5MB", "文件过大");
            return;
        }

        reader.readAsDataURL(file);

        reader.onload = function (e) {

            var img = new Image();
            img.src = e.target.result;
            img.onload = function () {

                var canvas = document.createElement('canvas');
                var ctx = canvas.getContext('2d');
                // 设置 canvas 的宽度和高度
                canvas.width = img.width;
                canvas.height = img.height;
                ctx.drawImage(img, 0, 0, img.width, img.height);

                // 返回一个包含图片展示的 Data URI
                var base64 = canvas.toDataURL('image/jpeg', 0.8);

                images.push(base64);

                $uploaderFiles.append($(tmpl.replace('#url#', base64)));

                //更新图片显示数量
                let count = parseInt($(".weui-uploader__info").text().split("/")[0]);
                $(".weui-uploader__info").text((count + 1) + "/3");

                // 如果当前图片数量已经超过3张，则拒绝上传
                if (count + 1 >= 4) {
                    $uploaderInput.attr("disabled", true);
                    $uploaderInput.hide();
                }
            }
        }
    });

    //放大显示图片详情
    $uploaderFiles.on("click", "li", function () {
        $galleryImg.attr("index", $('li').index(this));
        $galleryImg.attr("style", this.getAttribute("style"));
        $gallery.fadeIn(100);
    });

    //退出图片放大显示框
    $gallery.on("click", function () {
        $galleryImg.attr("index", 0);
        $gallery.fadeOut(100);
    });
});

//删除已上传缓存的图片
function deleteIDCardImage() {
    let index = $("#hmtf-idcard-gallery-image").attr("index");
    images.splice(index, 1);
    $("#hmtf-idcard-uploader-files li:eq(" + index + ")").remove();
    //更新图片显示数量
    let count = parseInt($(".weui-uploader__info").text().split("/")[0]);
    $(".weui-uploader__info").text((count - 1) + "/3");
    //图片少于3张时允许继续上传
    if (count + 1 <= 3) {
        $("#hmtf-idcard-uploader-input").attr("disabled", false);
        $("#hmtf-idcard-uploader-input").show();
    }
}

//中国居民身份证更新实名认证
function updateMainLandIDCardAuthentication() {
    let name = $("mainland-idcard-name").val();
    let number = $("#mainland-idcard-number").val();
    if (name != '' && number != '') {
        let loading = weui.loading('正在提交实名认证');
        $.ajax({
            url: '/api/authentication/update',
            type: 'post',
            data: {
                type: 0,
                name: $("#name").val(),
                number: $("#number").val()
            },
            success: function (result) {
                $(".weui-btn").attr("disabled", false);
                loading.hide();
                if (result.success === true) {
                    $.alert({
                        title: '实名认证结果',
                        text: '实名认证成功',
                        onOK: function () {
                            history.go(-1);
                        }
                    });
                } else {
                    $.toptip(result.message, 'error');
                }
            },
            error: function () {
                $(".weui-btn").attr("disabled", false);
                loading.hide();
                $.toptip('网络异常，请检查网络连接', 'error');
            }
        });
    } else {
        $.toptip('请将证件信息填写完整', 'error');
    }
}

//港澳台和海外用户更新实名认证
function updateHMTFIDCardAuthentication() {
    let name = $("#hmtf-idcard-type").val();
    let number = $("#hmtf-idcard-number").val();
    let type = $("#hmtf-idcard-type").val();
    if (name != '' && number != '' && type != '') {
        //解析图片信息
        if (images.length > 0) {
            for (let i = 0; i < images.length; i++) {
                let base64 = images[i];
                base64 = base64.split(',')[1];
                base64 = window.atob(base64);
                var ia = new Uint8Array(base64.length);
                for (var j = 0; j < base64.length; j++) {
                    ia[j] = base64.charCodeAt(j);
                }
                let blob = new Blob([ia], {type: "image/jpg"});
                formData.append('image', blob);
            }
        }
        //提交实名认证信息
        let loading = weui.loading('正在提交实名认证');
        $.ajax({
            url: "/api/authentication/update",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            success: function (result) {
                loading.hide();
                if (result.success === true) {
                    $.alert({
                        title: '实名认证结果',
                        text: '实名认证成功',
                        onOK: function () {
                            history.go(-1);
                        }
                    });
                } else {
                    $.toptip(result.message, 'error');
                }
            },
            error: function (result) {
                loading.hide();
                if (result.status) {
                    $.toptip(result.responseJSON.message, 'error');
                } else {
                    $.toptip('网络连接失败,请检查网络连接', 'error');
                }
            }
        });
    }
}

//取消实名认证
function deleteAuthentication() {
    $.confirm({
        title: '取消实名认证',
        text: '你是否确认要取消实名认证？',
        onOK: function () {
            $.ajax({
                url: '/api/authentication/delete',
                type: 'post',
                success: function (result) {
                    if (result.success) {
                        $.alert({
                            title: '取消实名认证成功',
                            text: '已注销当前用户的实名认证信息'
                        });
                    } else {
                        $(".weui_warn").text(result.message).show().delay(2000).hide(0);
                    }
                },
                error: function () {
                    if (result.status == 503) {
                        //网络连接超时
                        $(".weui_warn").text(result.responseJSON.message).show().delay(2000).hide(0);
                    } else {
                        $(".weui_warn").text("网络连接异常，请检查网络连接").show().delay(2000).hide(0);
                    }
                }
            });
        }
    });
}