//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//选择头像文件
function selectAvatarImage() {
    $("#avatarFileInput").click();
}

//删除头像文件
function deleteAvatar() {
    weui.confirm('将删除用户上传的头像，并恢复为系统默认头像', {
        title: '删除头像',
        buttons: [{
            label: '取消',
            type: 'default',
            onClick: function(){}
        }, {
            label: '确定',
            type: 'primary',
            onClick: function(){
                $.ajax({
                    url: "/api/avatar/remove",
                    type: "post",
                    processData: false,
                    contentType: false,
                    success: function (result) {
                        if (result.success === true) {
                            window.location.reload();
                        } else {
                            weui.topTips(result.message);
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            weui.topTips(result.responseJSON.message);
                        } else {
                            weui.topTips('网络连接失败,请检查网络连接');
                        }
                    }
                });
            }
        }]
    });
}

//压缩后的图片
var image = new Image();

//未压缩的高清原图
var image_hd = new Image();

//用户选择图片后的回调
$(function () {

    // 允许上传的图片类型
    var allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif'];
    // 图片最大大小为2MB
    var maxSize = 1024 * 1024 * 2;
    // 图片最大宽度
    var maxWidth = 300;

    $('#avatarFileInput').on('change', function (event) {

        var files = event.target.files;

        // 如果没有选中文件，直接返回
        if (files.length === 0) {
            return;
        }

        var file = files[0];
        var reader = new FileReader();

        // 如果类型不在允许的类型范围内
        if (allowTypes.indexOf(file.type) === -1) {
            weui.alert('不合法的图片文件类型', { title: '上传错误' });
            return;
        }

        if (file.size > maxSize) {
            weui.alert('图片文件不能超过5MB', { title: '文件过大' });
            return;
        }

        reader.readAsDataURL(file);

        reader.onload = function (e) {

            var img = new Image();
            img.src = e.target.result;
            img.onload = function () {

                //原图图片

                //宽度和高度为原图高度
                var w_hd = img.width;
                var h_hd = img.height;
                var canvas_hd = document.createElement('canvas');
                var ctx_hd = canvas_hd.getContext('2d');

                // 设置 canvas 的宽度和高度
                canvas_hd.width = w_hd;
                canvas_hd.height = h_hd;
                ctx_hd.drawImage(img, 0, 0, w_hd, h_hd);

                // 返回一个包含图片展示的 Data URI
                var base64_hd = canvas_hd.toDataURL('image/jpeg', 1);

                image_hd.src = base64_hd;

                //压缩图片

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

                image.src = base64;

                $("#drawImage").attr("src", "");
                $("#drawImage").removeAttr("src");
                $("#drawImage").attr("src", base64);

                //裁剪图片
                $("#drawImage").cropper({
                    aspectRatio: 1,
                    crop: function (e) {
                        //保存裁剪图片结果参数
                        $("#x").val(e.x);
                        $("#y").val(e.y);
                        $("#width").val(e.width);
                        $("#height").val(e.height);
                    }
                });
                $("#drawImage").cropper('replace', base64);

                //弹出裁剪预览窗口
                $("#drawImageDialog").popup();
            };
        };
    });
});

// 裁剪图片后对图片进行处理和上传
function drawImageAndUpload() {
    //压缩图片
    var canvas = $('<canvas width="' + $("#width").val() + '" height="' + $("#height").val() + '"></canvas>')[0];
    var ctx = canvas.getContext('2d');
    ctx.drawImage(image, $("#x").val(), $("#y").val(), $("#width").val()
        , $("#height").val(), 0, 0, $("#width").val(), $("#height").val());
    var base64 = canvas.toDataURL('image/jpeg', 1);

    base64 = base64.split(',')[1];
    base64 = window.atob(base64);
    var ia = new Uint8Array(base64.length);
    for (var i = 0; i < base64.length; i++) {
        ia[i] = base64.charCodeAt(i);
    }

    var blob = new Blob([ia], {type: "image/jpg"});

    //高清图片

    //高清图片放大倍数
    var scale = image_hd.width / image.width;
    var canvas_hd = $('<canvas width="' + $("#width").val() * scale + '" height="' + $("#height").val() * scale + '"></canvas>')[0];
    var ctx_hd = canvas_hd.getContext('2d');
    ctx_hd.drawImage(image_hd, $("#x").val() * scale, $("#y").val() * scale, $("#width").val() * scale
        , $("#height").val() * scale, 0, 0, $("#width").val() * scale, $("#height").val() * scale);
    var base64_hd = canvas_hd.toDataURL('image/jpeg', 1);

    base64_hd = base64_hd.split(',')[1];
    base64_hd = window.atob(base64_hd);
    var ia_hd = new Uint8Array(base64_hd.length);
    for (var j = 0; j < base64_hd.length; j++) {
        ia_hd[j] = base64_hd.charCodeAt(j);
    }

    var blob_hd = new Blob([ia_hd], {type: "image/jpg"});

    var formData = new FormData();
    formData.append('avatar', blob);
    formData.append('avatar_hd', blob_hd);

    //上传头像到服务器
    $.ajax({
        url: "/api/avatar",
        type: "post",
        data: formData,
        processData: false,
        contentType: false,
        success: function (result) {
            if (result.success === true) {
                window.location.reload();
            } else {
                weui.topTips(result.message);
            }
        },
        error: function (result) {
            if (result.status) {
                weui.topTips(result.responseJSON.message);
            } else {
                weui.topTips('网络连接失败,请检查网络连接');
            }
        }
    });
}