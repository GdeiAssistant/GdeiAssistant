// 允许上传的图片类型
let allowTypes = ['image/jpg', 'image/jpeg', 'image/png', 'image/gif'];

// 图片最大大小为5MB
let maxSize = 1024 * 1024 * 5;

$(function () {
    //消除iOS点击延迟
    FastClick.attach(document.body);
    //设置点击事件
    $('#imageFileInput_1').on('change', function (event) {
        var files = event.target.files;
        // 如果没有选中文件，直接返回
        if (files.length === 0) {
            return;
        }
        var file = files[0];
        loadImage(file, 1);
    });
    $('#imageFileInput_2').on('change', function (event) {
        var files = event.target.files;
        // 如果没有选中文件，直接返回
        if (files.length === 0) {
            return;
        }
        var file = files[0];
        loadImage(file, 2);
    });
    $('#imageFileInput_3').on('change', function (event) {
        var files = event.target.files;
        // 如果没有选中文件，直接返回
        if (files.length === 0) {
            return;
        }
        var file = files[0];
        loadImage(file, 3);
    });
    $('#imageFileInput_4').on('change', function (event) {
        var files = event.target.files;
        // 如果没有选中文件，直接返回
        if (files.length === 0) {
            return;
        }
        var file = files[0];
        loadImage(file, 4);
    });
});

//清空图片
function clearPictures() {
    $("#main-photo-box").css("background-image", "");
    $("#second-photo-box-1").css("background-image", "");
    $("#second-photo-box-2").css("background-image", "");
    $("#second-photo-box-3").css("background-image", "");
}

//检测Input文字长度是否超过限制并进行实时字数反馈
function inputLengthCheck(str, maxLen) {
    if (str.value.length > maxLen) {
        str.value = str.value.substr(0, maxLen);
    }
    $("#word-count").text(str.value.length + "/150字");
}

//加载图片并显示预览图
function loadImage(file, index) {

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

            //宽度和高度为原图高度
            var w = img.width;
            var h = img.height;
            var canvas = document.createElement('canvas');
            var ctx = canvas.getContext('2d');

            // 设置 canvas 的宽度和高度
            canvas.width = w;
            canvas.height = h;
            ctx.drawImage(img, 0, 0, w, h);

            // 返回一个包含图片展示的 Data URI
            var base64 = canvas.toDataURL('image/jpeg', 0.8);

            if (index == 1) {
                //缓存图片
                $("#main-photo-box").attr("image", base64);
                //设置主图预览图
                $("#main-photo-box").css("background-repeat", "round");
                $("#main-photo-box").css("background-image", "url(" + base64 + ")");
            } else {
                //缓存图片
                $("#second-photo-box-" + (index - 1)).attr("image", base64);
                //设置副图预览图
                $("#second-photo-box-" + (index - 1)).css("background-repeat", "round");
                $("#second-photo-box-" + (index - 1)).css("background-image", "url(" + base64 + ")");
            }
        };
    };
}

//提交照片信息
function submitUpload() {
    if ($("#title").val().length == 0) {
        weui.alert('标题不能为空', {
            title: '错误提示',
            buttons: [{
                label: '确定',
                type: 'primary'
            }]
        });
    } else if ($("#title").val().length > 25) {
        weui.alert('标题长度过长', {
            title: '错误提示',
            buttons: [{
                label: '确定',
                type: 'primary'
            }]
        });
    } else if (!$("#main-photo-box").attr("image")) {
        weui.alert('请上传至少一张图片', {
            title: '错误提示',
            buttons: [{
                label: '确定',
                type: 'primary'
            }]
        });
    } else {
        let loading = weui.loading("提交中");
        $("#submit").attr("disabled", true);
        var formData = new FormData();
        //基本属性
        formData.append('title', $("#title").val());
        if ($("#content").val() != '') {
            formData.append('content', $("#content").val());
        }
        let count = 1;
        formData.append('type', $("input[name='type']:checked").val());
        //图片元素
        let base64 = $("#main-photo-box").attr("image");
        base64 = base64.split(',')[1];
        base64 = window.atob(base64);
        var ia = new Uint8Array(base64.length);
        for (var i = 0; i < base64.length; i++) {
            ia[i] = base64.charCodeAt(i);
        }
        var blob = new Blob([ia], {type: "image/jpg"});
        formData.append('image1', blob);
        for (let index = 1; index <= 3; index++) {
            if (typeof ($("#second-photo-box-" + index).attr("image")) != "undefined") {
                let base64 = $("#second-photo-box-" + index).attr("image");
                base64 = base64.split(',')[1];
                base64 = window.atob(base64);
                var ia = new Uint8Array(base64.length);
                for (var i = 0; i < base64.length; i++) {
                    ia[i] = base64.charCodeAt(i);
                }
                var blob = new Blob([ia], {type: "image/jpg"});
                formData.append('image' + (index + 1), blob);
                count++;
            }
        }
        //图片数量
        formData.append('count', count);
        $.ajax({
            url: "/api/photograph",
            type: "POST",
            data: formData,
            processData: false,
            contentType: false,
            success: function (result) {
                $("#submit").attr("disabled", false);
                loading.hide();
                if (result.success === true) {
                    window.location.href = '/photograph';
                } else {
                    weui.alert(result.message, {
                        title: '请求失败',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                }
            },
            error: function (result) {
                $("#submit").attr("disabled", false);
                loading.hide();
                if (result.status) {
                    weui.alert(result.responseJSON.message, {
                        title: '请求失败',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                } else {
                    weui.alert('网络访问异常，请检查网络连接', {
                        title: '网络异常',
                        buttons: [{
                            label: '确定',
                            type: 'primary'
                        }]
                    });
                }
            }
        });
    }
}