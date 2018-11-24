<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="profile" uri="/WEB-INF/tld/profile.tld" %>
<script>

    //消除iOS点击延迟
    $(function () {
        FastClick.attach(document.body);
    });

    // 压缩后的图片
    var image = new Image();

    // 所在地选择器
    var locationPicker;

    // 所在地选择器选项
    var locationPickerItems = [];

    var genderMap = [];

    var genderOrientationMap = [];

    var facultyMap = [];

    $(function () {
        loadProfile();
        loadAvatar();
        loadRegionMap();
    });

    //加载所在地代码映射表
    function loadRegionMap() {
        $.ajax({
            url: '/api/locationList',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    var locationData = result.data;
                    localStorage.setItem("locationData", JSON.stringify(locationData));
                    for (var i = 0; i < locationData.length; i++) {
                        if (locationData[i].hasOwnProperty("stateMap") && Object.getOwnPropertyNames(locationData[i].stateMap).length > 0) {
                            var stateMap = locationData[i].stateMap;
                            var statePickerItems = [];
                            var j = 0;
                            for (var stateKey in stateMap) {
                                if (stateMap[stateKey].hasOwnProperty("cityMap") && Object.getOwnPropertyNames(stateMap[stateKey]).length) {
                                    var cityMap = stateMap[stateKey].cityMap;
                                    var cityPickerItems = [];
                                    var k = 0;
                                    for (var cityKey in cityMap) {
                                        cityPickerItems[k] = {
                                            label: cityMap[cityKey].name,
                                            value: cityKey
                                        };
                                        k++;
                                    }
                                    statePickerItems[j] = {
                                        label: stateMap[stateKey].name,
                                        value: stateKey,
                                        children: cityPickerItems
                                    }
                                }
                                else {
                                    statePickerItems[j] = {
                                        label: stateMap[stateKey].name,
                                        value: stateKey,
                                        children: [
                                            {
                                                label: stateMap[stateKey].name,
                                                value: ''
                                            }
                                        ]
                                    }
                                }
                                locationPickerItems[i] = {
                                    label: locationData[i].name,
                                    value: locationData[i].code,
                                    children: statePickerItems
                                };
                                j++;
                            }
                        }
                        else {
                            locationPickerItems[i] = {
                                label: locationData[i].name,
                                value: locationData[i].code,
                                children: [
                                    {
                                        label: locationData[i].name.substring(4),
                                        value: '',
                                        children: [
                                            {
                                                label: locationData[i].name.substring(4),
                                                value: ''
                                            }
                                        ]
                                    }
                                ]
                            }
                        }
                    }
                }
                else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function () {
                showNetworkErrorTip();
            }
        });
    }

    //加载个人资料
    function loadProfile() {

        <c:forEach items="${profile:getGenderMap()}" var="genderEntry">

        genderMap[${genderEntry.key}] = "${genderEntry.value}";

        </c:forEach>

        <c:forEach items="${profile:getGenderOrientationMap()}" var="genderOrientationEntry">

        genderOrientationMap[${genderOrientationEntry.key}] = "${genderOrientationEntry.value}";

        </c:forEach>

        <c:forEach items="${profile:getFacultyMap()}" var="facultyEntry">

        facultyMap[${facultyEntry.key}] = "${facultyEntry.value}";

        </c:forEach>

        $.ajax({
            url: "/api/profile",
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    //昵称
                    $("#kickname_text").text(result.data.kickname);
                    $("#kickname_val").val(result.data.kickname);
                    $("#kickname").val(result.data.kickname);
                    //姓名
                    let realname = result.data.realname == null ? "暂未录入" : result.data.realname;
                    $("#realname").text(realname);
                    //性别
                    let gender = result.data.gender == null ? 0 : result.data.gender;
                    if (gender == 3) {
                        $("#gender").text(result.data.customGenderName);
                        $("#gender_val").val(result.data.customGenderName);
                    }
                    else {
                        $("#gender").text(genderMap[gender]);
                    }
                    //性取向
                    var genderOrientation = result.data.genderOrientation == null ? 0 : result.data.genderOrientation;
                    $("#genderOrientation").text(genderOrientationMap[genderOrientation]);
                    var location = result.data.region;
                    if (location != null) {
                        if (result.data.state != null && result.data.state != result.data.region) {
                            location = location + result.data.state;
                        }
                        if (result.data.city != null && result.data.city != result.data.state) {
                            location = location + result.data.city;
                        }
                        $("#location").text(location);
                    }
                    else {
                        $("#location").text("未选择");
                    }
                    //院系
                    var faculty = result.data.faculty == null ? 0 : result.data.faculty;
                    $("#faculty").text(facultyMap[faculty]);
                    //专业
                    $("#major_text").text(result.data.major == null ? "未填写" : result.data.major);
                    $("#major_val").val(result.data.major == null ? "" : result.data.major);
                }
                else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function () {
                showNetworkErrorTip();
            }
        });
    }

    //加载头像
    function loadAvatar() {
        $.ajax({
            url: '/api/avatar',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    if (result.data !== '') {
                        $("#avatar").attr("src", result.data);
                    }
                }
            }
        });
    }

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
                $.alert("不合法的图片文件类型", "上传错误");
                return;
            }

            if (file.size > maxSize) {
                $.alert("图片文件不能超过2MB", "文件过大");
                return;
            }

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

        var formData = new FormData();
        formData.append('avatar', blob);

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
                }
                else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function () {
                showNetworkErrorTip();
            }
        });
    }

    //检测Input文字长度是否超过限制
    function inputLengthCheck(str, maxLen) {
        var w = 0;
        for (var i = 0; i < str.value.length; i++) {
            var c = str.value.charCodeAt(i);
            if ((c >= 0x0001 && c <= 0x007e) || (0xff60 <= c && c <= 0xff9f)) {
                w++;
            }
            else {
                w += 2;
            }
            if (w > maxLen) {
                str.value = str.value.substr(0, i);
                break;
            }
        }
    }

    //修改用户所在地
    function changeRegion() {
        locationPicker = weui.picker(locationPickerItems,
            {
                defaultValue: ['CN', '44', '1'],
                container: 'body',
                onConfirm: function (result) {
                    $.ajax({
                        url: '/api/profile/location',
                        type: 'post',
                        data: {
                            region: result[0].value,
                            state: result[1].value,
                            city: result[2].value
                        },
                        success: function (updateResult) {
                            if (updateResult.success === true) {
                                loadProfile();
                            }
                            else {
                                showCustomErrorTip(updateResult.message);
                            }
                        },
                        error: function () {
                            showNetworkErrorTip();
                        }
                    });
                }
            });
    }

    //选择头像文件
    function selectAvatarImage() {
        $("#avatarFileInput").click();
    }

    //弹出昵称修改窗口
    function showKicknameDialog() {
        $("#kickname").val($("#kickname_val").val());
        $("#changeKickname").popup();
    }

    //弹出自定义性别窗口
    function showCustomGenderDialog() {
        $("#customGenderName").val($("#gender_val").val());
        $("#customGender").popup();
    }

    //弹出专业修改窗口
    function showMajorDialog() {
        $("#major").val($("#major_val").val());
        $("#changeMajor").popup();
    }

    //修改昵称
    function changeKickname() {
        if ($("#kickname").val().length > 0) {
            $.closePopup();
            $.ajax({
                url: "/api/profile/kickname",
                data: {
                    kickname: $("#kickname").val()
                },
                type: 'post',
                success: function (updateResult) {
                    if (updateResult.success === true) {
                        loadProfile();
                    }
                    else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function () {
                    showNetworkErrorTip();
                }
            });
        }
    }

    //提交自定义性别
    function submitCustomGender() {
        if ($("#customGenderName").val().length > 0) {
            $.closePopup();
            $.ajax({
                url: "/api/profile/gender",
                data: {
                    gender: 3,
                    customGenderName: $("#customGenderName").val()
                },
                type: 'post',
                success: function (updateResult) {
                    if (updateResult.success === true) {
                        loadProfile();
                    }
                    else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function () {
                    showNetworkErrorTip();
                }
            });
        }
    }

    //修改性别
    function changeGender() {
        var genderPicker = [];
        for (var i = 0; i < genderMap.length; i++) {
            genderPicker[i] = {
                label: genderMap[i],
                value: i
            }
        }
        weui.picker(genderPicker, {
            defaultValue: [0],
            onConfirm: function (gender) {
                if (gender == 3) {
                    showCustomGenderDialog();
                }
                else {
                    $.ajax({
                        url: "/api/profile/gender",
                        data: {
                            gender: gender[0].value
                        },
                        type: 'post',
                        success: function (updateResult) {
                            if (updateResult.success === true) {
                                loadProfile();
                            }
                            else {
                                showCustomErrorTip(updateResult.message);
                            }
                        },
                        error: function () {
                            showNetworkErrorTip();
                        }
                    });
                }
            }
        });
    }

    //修改性取向
    function changeGenderOrientation() {
        var genderOrientationPicker = [];
        for (var i = 0; i < genderOrientationMap.length; i++) {
            genderOrientationPicker[i] = {
                label: genderOrientationMap[i],
                value: i
            }
        }
        weui.picker(genderOrientationPicker, {
            defaultValue: [0],
            onConfirm: function (genderOrientation) {
                $.ajax({
                    url: "/api/profile/genderOrientation",
                    data: {
                        genderOrientation: genderOrientation[0].value
                    },
                    type: 'post',
                    success: function (updateResult) {
                        if (updateResult.success === true) {
                            loadProfile();
                        }
                        else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function () {
                        showNetworkErrorTip();
                    }
                });
            }
        });
    }

    //修改院系
    function changeFaculty() {
        var facultyPicker = [];
        for (var i = 0; i < facultyMap.length; i++) {
            facultyPicker[i] = {
                label: facultyMap[i],
                value: i
            }
        }
        weui.picker(facultyPicker, {
            defaultValue: [0],
            onConfirm: function (faculty) {
                $.ajax({
                    url: "/api/profile/faculty",
                    data: {
                        faculty: faculty[0].value
                    },
                    type: 'post',
                    success: function (updateResult) {
                        if (updateResult.success === true) {
                            loadProfile();
                        }
                        else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function () {
                        showNetworkErrorTip();
                    }
                });
            }
        });
    }

    //修改专业
    function changeMajor() {
        if ($("#major").val().length > 0 && $("#major").val().length <= 20) {
            $.closePopup();
            $.ajax({
                url: "/api/profile/major",
                data: {
                    major: $("#major").val()
                },
                type: 'post',
                success: function (updateResult) {
                    if (updateResult.success === true) {
                        loadProfile();
                    }
                    else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function () {
                    showNetworkErrorTip();
                }
            });
        }
    }

    //显示网络错误提示
    function showNetworkErrorTip() {
        $(".weui_warn").text('网络连接失败,请检查网络连接').show().delay(2000).hide(0);
    }

    //显示服务端错误提示
    function showCustomErrorTip(message) {
        $(".weui_warn").text(message).show().delay(2000).hide(0);
    }

</script>