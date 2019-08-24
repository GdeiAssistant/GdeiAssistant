<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="profile" uri="/WEB-INF/tld/profile.tld" %>
<%@ taglib prefix="access" uri="/WEB-INF/tld/access.tld" %>
<script>

    //获取个人资料页隐私设置和功能管理小红点图标显示配置，显示或隐藏小红点图标
    $(function () {
        if (!localStorage.getItem("privacyBadge")) {
            $("#privacyBadge").show();
        }
        if (!localStorage.getItem("functionBadge")) {
            $("#functionBadge").show();
        }
        if (!localStorage.getItem("themeBadge")) {
            $("#themeBadge").show();
        }
    });

    // 所在地选择器
    var locationPicker;

    // 所在地选择器选项
    var locationPickerItems = [];

    var genderMap = [];

    var professionMap = [];

    var degreeMap = [];

    var facultyMap = [];

    var userGroupMap = [];

    $(function () {
        loadUserGroupMap();
        loadProfile();
        loadAuthenticationState();
        loadAvatar();
        loadRegionMap();
    });

    //加载用户组映射表
    function loadUserGroupMap() {

        <c:forEach items="${access:loadUserGroupInfo()}" var="userGroup" varStatus="status">

        userGroupMap[${status.index}] = "${userGroup}";

        </c:forEach>
    }

    //加载所在地代码映射表
    function loadRegionMap() {
        $.ajax({
            url: '/api/locationList',
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    var locationData = result.data;
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
                                } else {
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
                        } else {
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
                } else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function (result) {
                if (result.status) {
                    showCustomErrorTip(result.responseJSON.message);
                } else {
                    showNetworkErrorTip();
                }
            }
        });
    }

    //加载实名认证状态
    function loadAuthenticationState() {
        $.ajax({
            url: "/api/authentication",
            method: "GET",
            success: function (result) {
                if (result.success) {
                    if (result.data) {
                        $("#authenticated").show();
                        $("#unauthenticated").hide();
                    } else {
                        $("#unauthenticated").show();
                        $("#authenticated").hide();
                    }
                } else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function (result) {
                if (result.status) {
                    showCustomErrorTip(result.responseJSON.message);
                } else {
                    showNetworkErrorTip();
                }
            }
        });
    }

    //加载个人资料
    function loadProfile() {

        <c:forEach items="${profile:getGenderMap()}" var="genderEntry">

        genderMap[${genderEntry.key}] = "${genderEntry.value}";

        </c:forEach>

        <c:forEach items="${profile:getProfessionMap()}" var="professionEntry">

        professionMap[${professionEntry.key}] = "${professionEntry.value}";

        </c:forEach>

        <c:forEach items="${profile:getDegreeMap()}" var="degreeEntry">

        degreeMap[${degreeEntry.key}] = "${degreeEntry.value}";

        </c:forEach>

        <c:forEach items="${profile:getFacultyMap()}" var="facultyEntry">

        facultyMap[${facultyEntry.key}] = "${facultyEntry.value}";

        </c:forEach>

        $.ajax({
            url: "/api/profile",
            type: 'get',
            success: function (result) {
                if (result.success === true) {
                    //用户组
                    $("#user_group").text(userGroupMap[${sessionScope.group-1}]);
                    //用户名
                    $("#username").text(result.data.username);
                    //昵称
                    $("#kickname_text").text(result.data.kickname);
                    $("#kickname_val").val(result.data.kickname);
                    $("#kickname").val(result.data.kickname);
                    //性别
                    let gender = result.data.gender == null ? 0 : result.data.gender;
                    if (gender == 3) {
                        $("#gender").text(result.data.customGenderName);
                        $("#gender_val").val(result.data.customGenderName);
                    } else {
                        $("#gender").text(genderMap[gender]);
                    }
                    var location = result.data.region;
                    if (location != null) {
                        if (result.data.state != null && result.data.state != result.data.region) {
                            location = location + result.data.state;
                        }
                        if (result.data.city != null && result.data.city != result.data.state) {
                            location = location + result.data.city;
                        }
                        $("#location").text(location);
                    } else {
                        $("#location").text("未选择");
                    }
                    //年龄
                    let birthday = result.data.birthday;
                    if (birthday) {
                        //根据出生时间和当前时间计算年龄
                        let age = Math.floor((new Date().getTime() - new Date(birthday).getTime()) / 31536000000);
                        $("#age").text(age);
                    } else {
                        $("#age").text("未填写");
                    }
                    //学历
                    var degree = result.data.degree == null ? 0 : result.data.degree;
                    $("#degree").text(degreeMap[degree]);
                    //院系
                    var faculty = result.data.faculty == null ? 0 : result.data.faculty;
                    $("#faculty").text(facultyMap[faculty]);
                    //专业
                    $("#major_text").text(result.data.major == null ? "未填写" : result.data.major);
                    $("#major_val").val(result.data.major == null ? "" : result.data.major);
                    //入学年份
                    $("#enrollment_text").text(result.data.enrollment == null ? "未选择" : result.data.enrollment + "年");
                    //职业信息
                    var profession = result.data.profession == null ? 0 : result.data.profession;
                    $("#profession_text").text(professionMap[profession]);
                    //学校信息
                    $("#high_school").text(result.data.highSchool == null ? "未填写" : result.data.highSchool);
                    $("#high_school_val").val(result.data.highSchool == null ? "" : result.data.highSchool);
                    $("#junior_high_school").text(result.data.juniorHighSchool == null ? "未填写" : result.data.juniorHighSchool);
                    $("#junior_high_school_val").val(result.data.juniorHighSchool == null ? "" : result.data.juniorHighSchool);
                    $("#primary_school").text(result.data.primarySchool == null ? "未填写" : result.data.primarySchool);
                    $("#primary_school_val").val(result.data.primarySchool == null ? "" : result.data.primarySchool);
                } else {
                    showCustomErrorTip(result.message);
                }
            },
            error: function (result) {
                if (result.status) {
                    showCustomErrorTip(result.responseJSON.message);
                } else {
                    showNetworkErrorTip();
                }
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

    //检测Input文字长度是否超过限制
    function inputLengthCheck(str, maxLen) {
        var w = 0;
        for (var i = 0; i < str.value.length; i++) {
            var c = str.value.charCodeAt(i);
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
                            } else {
                                showCustomErrorTip(updateResult.message);
                            }
                        },
                        error: function (result) {
                            if (result.status) {
                                showCustomErrorTip(result.responseJSON.message);
                            } else {
                                showNetworkErrorTip();
                            }
                        }
                    });
                }
            });
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

    //弹出学校修改窗口
    function showSchoolDialog(index) {
        $("#school_index").val(index);
        switch (index) {
            case 0:
                //高中/职中
                $("#school").val($("#high_school_val").val());
                break;

            case 1:
                //初中
                $("#school").val($("#junior_high_school_val").val());
                break;

            case 2:
            default:
                //小学
                $("#school").val($("#primary_school_val").val());
                break;
        }
        $("#changeSchool").popup();
    }

    //修改入学年份
    function showEnrollmentDialog() {
        var enrollmentPicker = [];
        for (var i = 0; i <= new Date().getFullYear() - 1955; i++) {
            enrollmentPicker[i] = {
                label: (i + 1955) + "年",
                value: i + 1955
            }
        }
        weui.picker(enrollmentPicker, {
            defaultValue: [new Date().getFullYear()],
            onConfirm: function (year) {
                $.ajax({
                    url: "/api/profile/enrollment",
                    data: {
                        year: year[0].value
                    },
                    type: 'post',
                    success: function (updateResult) {
                        if (updateResult.success === true) {
                            loadProfile();
                        } else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            showCustomErrorTip(result.responseJSON.message);
                        } else {
                            showNetworkErrorTip();
                        }
                    }
                });
            }
        });
    }

    //修改职业信息
    function showProfessionDialog() {
        var professionPicker = [];
        for (var i = 0; i < professionMap.length; i++) {
            professionPicker[i] = {
                label: professionMap[i],
                value: i
            }
        }
        weui.picker(professionPicker, {
            onConfirm: function (profession) {
                $.ajax({
                    url: "/api/profile/profession",
                    data: {
                        profession: profession[0].value
                    },
                    type: 'post',
                    success: function (updateResult) {
                        if (updateResult.success === true) {
                            loadProfile();
                        } else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            showCustomErrorTip(result.responseJSON.message);
                        } else {
                            showNetworkErrorTip();
                        }
                    }
                });
            }
        });
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
                    } else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function (result) {
                    if (result.status) {
                        showCustomErrorTip(result.responseJSON.message);
                    } else {
                        showNetworkErrorTip();
                    }
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
                    } else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function (result) {
                    if (result.status) {
                        showCustomErrorTip(result.responseJSON.message);
                    } else {
                        showNetworkErrorTip();
                    }
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
                } else {
                    $.ajax({
                        url: "/api/profile/gender",
                        data: {
                            gender: gender[0].value
                        },
                        type: 'post',
                        success: function (updateResult) {
                            if (updateResult.success === true) {
                                loadProfile();
                            } else {
                                showCustomErrorTip(updateResult.message);
                            }
                        },
                        error: function (result) {
                            if (result.status) {
                                showCustomErrorTip(result.responseJSON.message);
                            } else {
                                showNetworkErrorTip();
                            }
                        }
                    });
                }
            }
        });
    }

    //修改生日
    function changeBirthday() {
        weui.datePicker({
            start: 1900,
            end: new Date(),
            defaultValue: [2000, 1, 1],
            onConfirm: function (birthday) {
                $.ajax({
                    url: "/api/profile/birthday",
                    data: {
                        year: birthday[0].value,
                        month: birthday[1].value,
                        date: birthday[2].value
                    },
                    type: 'post',
                    success: function (updateResult) {
                        if (updateResult.success === true) {
                            loadProfile();
                        } else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            showCustomErrorTip(result.responseJSON.message);
                        } else {
                            showNetworkErrorTip();
                        }
                    }
                });
            }
        });
    }

    //修改学历
    function changeDegree() {
        var degreePicker = [];
        for (var i = 0; i < degreeMap.length; i++) {
            degreePicker[i] = {
                label: degreeMap[i],
                value: i
            }
        }
        weui.picker(degreePicker, {
            defaultValue: [0],
            onConfirm: function (degree) {
                $.ajax({
                    url: "/api/profile/degree",
                    data: {
                        degree: degree[0].value
                    },
                    type: 'post',
                    success: function (updateResult) {
                        if (updateResult.success === true) {
                            loadProfile();
                        } else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            showCustomErrorTip(result.responseJSON.message);
                        } else {
                            showNetworkErrorTip();
                        }
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
                        } else {
                            showCustomErrorTip(updateResult.message);
                        }
                    },
                    error: function (result) {
                        if (result.status) {
                            showCustomErrorTip(result.responseJSON.message);
                        } else {
                            showNetworkErrorTip();
                        }
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
                    } else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function (result) {
                    if (result.status) {
                        showCustomErrorTip(result.responseJSON.message);
                    } else {
                        showNetworkErrorTip();
                    }
                }
            });
        }
    }

    //修改学校信息
    function changeSchool() {
        if ($("#school").val().length > 0 && $("#school").val().length <= 45) {
            var school = $("#school").val();
            var index = $("#school_index").val();
            $.closePopup();
            $.ajax({
                url: '/api/profile/school',
                type: 'post',
                data: {
                    index: index,
                    school: school
                },
                success: function (updateResult) {
                    if (updateResult.success === true) {
                        loadProfile();
                    } else {
                        showCustomErrorTip(updateResult.message);
                    }
                },
                error: function (result) {
                    if (result.status) {
                        showCustomErrorTip(result.responseJSON.message);
                    } else {
                        showNetworkErrorTip();
                    }
                }
            });
        }
    }

    //弹出退出确认框
    function showLogoutConfirm() {
        if (!yibanUser) {
            $.confirm({
                title: '退出当前账号',
                text: '你确定退出当前账号并清除账号缓存吗？',
                onOK: function () {
                    //清空本地缓存
                    localStorage.clear();
                    window.location.href = '/logout';
                }
            });
        } else {
            $.alert("易班客户端不支持账号退出，你可以重新绑定易班账号", "错误提示");
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