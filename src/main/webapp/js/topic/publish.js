let images = [];

// 允许上传的图片类型
let allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif'];

// 图片最大大小为5MB
let maxSize = 1024 * 1024 * 5;

$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
});

//上传图片
$(function () {
    let tmpl = '<li class="weui-uploader__file" style="background-image:url(#url#)"></li>',
        $gallery = $("#gallery"), $galleryImg = $("#galleryImg"),
        $uploaderInput = $("#uploaderInput"),
        $uploaderFiles = $("#uploaderFiles");

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
                $(".weui-uploader__info").text((count + 1) + "/9");
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
function deleteImage() {
    let index = $("#galleryImg").attr("index");
    images.splice(index, 1);
    $("#uploaderFiles li:eq(" + index + ")").remove();
    //更新图片显示数量
    let count = parseInt($(".weui-uploader__info").text().split("/")[0]);
    $(".weui-uploader__info").text((count - 1) + "/9");
}

//动态设置话题表单宽度
function setTopicInputDynamicWidth(element) {
    if ($(element).val() == '') {
        $(element).width(120);
    } else {
        $("#dynamic_topic_span").text($(element).val());
        $(element).width($("#dynamic_topic_span").width() + 20);
    }
}

//检测Input文字长度是否超过限制并进行实时字数反馈
function inputLengthCheck(str, maxLen) {
    let w = 0;
    for (let i = 0; i < str.value.length; i++) {
        let c = str.value.charCodeAt(i);
        if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
            w++;
        } else {
            w += 2;
        }
        if (w > maxLen) {
            str.value = str.value.substr(0, i);
            break;
        }
    }
    $(".weui-textarea-counter span").text(str.value.length);
}

function postTopicForm() {
    if ($("#topic").val() == '' || $("#topic").val().length > 15) {
        $.alert("话题长度不合法");
    } else if ($("#content").val() == '' || $("content").val() > 250) {
        $.alert("内容长度不合法");
    } else {
        //禁用提交按钮并显示进度条
        $(".weui_btn_primary").attr("disabled", true);
        $("#loadingToast, .weui_mask").show();
        let formData = new FormData();
        formData.append('topic', $("#topic").val());
        formData.append('content', $("#content").val());
        formData.append('count', images.length);
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
                formData.append('images', blob);
            }
        }
        //上传头像到服务器
        $.ajax({
            url: "/api/topic",
            type: "post",
            data: formData,
            processData: false,
            contentType: false,
            success: function (result) {
                $(".weui_btn_primary").attr("disabled", false);
                $("#loadingToast, .weui_mask").hide();
                if (result.success === true) {
                    window.location.href = '/topic';
                } else {
                    $.toptip(result.message, 'error');
                }
            },
            error: function (result) {
                $(".weui_btn_primary").attr("disabled", false);
                $("#loadingToast, .weui_mask").hide();
                if (result.status) {
                    $.toptip(result.responseJSON.message, 'error');
                } else {
                    $.toptip('网络连接失败,请检查网络连接', 'error');
                }
            }
        });
    }
}