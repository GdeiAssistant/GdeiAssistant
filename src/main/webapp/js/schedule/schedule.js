let isDialogOpen = false;

let hasDeleteAnyCustomSchedule = false;

let dataList = [];

let columnPicker = [
    {
        label: '周一',
        value: 0
    }, {
        label: '周二',
        value: 1
    }, {
        label: '周三',
        value: 2
    }, {
        label: '周四',
        value: 3
    }, {
        label: '周五',
        value: 4
    }, {
        label: '周六',
        value: 5
    }, {
        label: '周日',
        value: 6
    }];

let rowPicker = [
    {
        label: '第一节',
        value: 0
    }, {
        label: '第二节',
        value: 1
    }, {
        label: '第三节',
        value: 2
    }, {
        label: '第四节',
        value: 3
    }, {
        label: '第五节',
        value: 4
    }, {
        label: '第六节',
        value: 5
    }, {
        label: '第七节',
        value: 6
    }, {
        label: '第八节',
        value: 7
    }, {
        label: '第九节',
        value: 8
    }, {
        label: '第十节',
        value: 9
    }];

let lengthPicker = [
    {
        label: '单独一节',
        value: 1
    }, {
        label: '连续两节',
        value: 2
    }, {
        label: '连续三节',
        value: 3
    }, {
        label: '连续四节',
        value: 4
    }, {
        label: '连续五节',
        value: 5
    }];

let weekPicker = [
    {
        label: '第1周',
        value: 1
    },
    {
        label: '第2周',
        value: 2
    },
    {
        label: '第3周',
        value: 3
    },
    {
        label: '第4周',
        value: 4
    },
    {
        label: '第5周',
        value: 5
    },
    {
        label: '第6周',
        value: 6
    },
    {
        label: '第7周',
        value: 7
    },
    {
        label: '第8周',
        value: 8
    },
    {
        label: '第9周',
        value: 9
    },
    {
        label: '第10周',
        value: 10
    },
    {
        label: '第11周',
        value: 11
    },
    {
        label: '第12周',
        value: 12
    },
    {
        label: '第13周',
        value: 13
    },
    {
        label: '第14周',
        value: 14
    },
    {
        label: '第15周',
        value: 15
    },
    {
        label: '第16周',
        value: 16
    },
    {
        label: '第17周',
        value: 17
    },
    {
        label: '第18周',
        value: 18
    },
    {
        label: '第19周',
        value: 19
    },
    {
        label: '第20周',
        value: 20
    }
];

//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//检测Input文字长度是否超过限制
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
}

//监听返回事件
if (window.history && window.history.pushState) {
    $(window).on('popstate', function () {
        let hashLocation = location.hash;
        let hashSplit = hashLocation.split("#!/");
        let hashName = hashSplit[1];
        if (hashName !== '') {
            let hash = window.location.hash;
            if (hash === '') {
                if (isDialogOpen) {
                    $.closePopup();
                } else {
                    sessionStorage.removeItem("scheduleWeek");
                    history.go(-1);
                }
            }
        }
    });
    window.history.pushState('forward', null, './schedule');
}

//默认查询当前周课表
$(function () {
    postQueryForm();
    setTextColor();
});

//选择查询的周数
function selectQueryWeek() {
    weui.picker(weekPicker, {
        defaultValue: [$("#currentWeek").val()],
        onConfirm: function (result) {
            //更新当前选中周数
            sessionStorage.setItem("scheduleWeek", result[0].value);
            window.location.reload();
        }
    });
}

//弹出自定义课程添加窗口
function showAddCustomScheduleDialog() {
    isDialogOpen = true;
    $("#addCustomScheduleDialog").popup();
}

//选择上课时间
function selectScheduleTime() {
    weui.picker(columnPicker, rowPicker, {
        defaultValue: [0, 0],
        onConfirm: function (result) {
            $("#column").val(result[0].value);
            $("#row").val(result[1].value);
            $("#timeSelector").text(result[0].label + result[1].label);
            $("#length").val("");
            $("#lengthSelector").text("点击选择上课时长");
        }
    });
}

//选择上课时长
function selectScheduleLength() {
    if ($("#row").val() === '') {
        $("#error").text("请先选择上课时间").show().delay(2000).hide(0);
    } else {
        let maxLength = 0;
        if ($("#row").val() <= 4) {
            maxLength = 4 - $("#row").val();
        } else if ($("#row").val() <= 9) {
            maxLength = 9 - $("#row").val();
        }
        //禁用不可选的上课时长
        for (let index = maxLength + 1; index < lengthPicker.length; index++) {
            lengthPicker[index].disabled = true;
        }
        for (let index = 0; index < maxLength + 1; index++) {
            if (index < lengthPicker.length) {
                lengthPicker[index].disabled = false;
            }
        }
        weui.picker(lengthPicker, {
            defaultValue: [1],
            onConfirm: function (result) {
                $("#length").val(result[0].value);
                $("#lengthSelector").text(result[0].label);
            }
        });
    }
}

//选择上课周数
function selectScheduleWeek() {
    let startWeekPicker = [];
    let endWeekPicker = [];
    for (let index = 1; index <= 20; index++) {
        startWeekPicker[index - 1] = {
            label: '从第' + index + '周',
            value: index
        };
        endWeekPicker[index - 1] = {
            label: '至第' + index + '周',
            value: index
        };
    }
    weui.picker(startWeekPicker, endWeekPicker, {
        defaultValue: [1, 1],
        onChange: function (result) {
            for (let week = 1; week < result[0].value; week++) {
                endWeekPicker[week - 1].disabled = true;
            }
            for (let week = result[0].value; week <= 20; week++) {
                endWeekPicker[week - 1].disabled = false;
            }
        },
        onConfirm: function (result) {
            if (result[0].value > result[1].value) {
                $("#error").text("起始周数不能大于结束周数").show().delay(2000).hide(0);
            } else {
                $("#min_week").val(result[0].value);
                $("#max_week").val(result[1].value);
                $("#weekSelector").text(result[0].label + result[1].label);
            }
        }
    });
}

//提交删除自定义课程请求
function deleteCustomSchedule(id, dataListIndex, detailListIndex) {
    let loading = weui.loading('提交信息中');
    $("button[type='submit']").attr("disabeled", true);
    $.ajax({
        url: '/api/customschedule/id/' + id,
        type: 'DELETE',
        success: function (result) {
            loading.hide();
            if (result.success) {
                $.toptip('删除成功', 'success');
                //同步删除缓存数据列表中对应的数据
                if (detailListIndex) {
                    dataList[dataListIndex].detailScheduleList.splice(detailListIndex, 1);
                    if (dataList[dataListIndex].detailScheduleList.length === 0) {
                        setTimeout(function () {
                            $.closePopup();
                            window.location.reload();
                        }, 1000);
                    } else {
                        //隐藏被删除位置的自定义课程信息
                        $("button[type='submit']").attr("disabeled", false);
                        $("#customScheduleDetailDialog .weui-form-preview:eq(" + detailListIndex + ")").hide();
                    }
                } else {
                    dataList.splice(dataList, 1);
                    setTimeout(function () {
                        $.closePopup();
                        window.location.reload();
                    }, 1000);
                }
                hasDeleteAnyCustomSchedule = true;
            } else {
                $.toptip(result.message, 'error');
                $("button[type='submit']").attr("disabeled", false);
            }
        },
        error: function (result) {
            loading.hide();
            $("button[type='submit']").attr("disabeled", false);
            if (result.status) {
                $.toptip(result.message, 'error');
            } else {
                $.toptip('网络连接异常，请检查并重试', 'error');
            }
        }
    });
}

//提交添加自定义课程请求
function addCustomSchedule() {
    if ($("#scheduleName").val().length <= 0 || $("#scheduleName").val().length > 50) {
        $("#error").text("请正确填写课程名称").show().delay(2000).hide(0);
    } else if ($("#scheduleLocation").val().length <= 0 || $("#scheduleLocation").val().length > 25) {
        $("#error").text("请正确填写上课地点").show().delay(2000).hide(0);
    } else if ($("#row").val() === '' || $("#column").val() === '') {
        $("#error").text("请选择上课时间").show().delay(2000).hide(0);
    } else if ($("#length").val() === '') {
        $("#error").text("请选择上课时长").show().delay(2000).hide(0);
    } else if ($("#min_week").val() === '' || $("#max_week").val() === '') {
        $("#error").text("请选择上课周数").show().delay(2000).hide(0);
    } else if (parseInt($("#min_week").val()) < parseInt($("#max_week"))) {
        $("#error").text("初始上课周数不能小于结束上课周数").show().delay(2000).hide(0);
    } else {
        //提交自定义课程信息
        let loading = weui.loading('提交信息中');
        $("#submit").attr("disabled", true);
        $.ajax({
            url: '/api/customshedule',
            method: 'POST',
            data: {
                scheduleLength: parseInt($("#length").val()),
                scheduleName: $("#scheduleName").val(),
                scheduleLocation: $("#scheduleLocation").val(),
                position: parseInt($("#row").val()) * 7 + parseInt($("#column").val()),
                minScheduleWeek: parseInt($("#min_week").val()),
                maxScheduleWeek: parseInt($("#max_week").val())
            },
            success: function (result) {
                loading.hide();
                if (result.success) {
                    $.toptip('提交成功', 'success');
                    setTimeout(function () {
                        $("#submit").attr("disabled", false);
                        $.closePopup();
                        window.location.reload();
                    }, 1000);

                } else {
                    $.toptip(result.message, 'error');
                    $("#submit").attr("disabled", false);
                }
            },
            error: function (result) {
                loading.hide();
                $("#submit").attr("disabled", false);
                if (result.status) {
                    $.toptip(result.message, 'error');
                } else {
                    $.toptip('网络连接异常，请检查并重试', 'error');
                }
            }
        })
    }
}

//提交课表查询请求
function postQueryForm() {
    //显示进度条
    $("#loadingToast, .weui_mask").show();
    let week = sessionStorage.getItem("scheduleWeek");
    //判断是否查询指定周数
    if (!week) {
        //异步请求查询课表
        $.ajax({
            url: "/schedulequery",
            type: 'post',
            success: function (scheduleQueryResult) {
                //隐藏进度条
                $("#loadingToast, .weui_mask").hide();
                if (scheduleQueryResult.success === true) {
                    //更新当前选中周数
                    sessionStorage.setItem("scheduleWeek", scheduleQueryResult.week);
                    $("#currentWeek").text("第" + scheduleQueryResult.week + "周");
                    handleScheduleQueryResult(scheduleQueryResult);
                } else {
                    showCustomErrorTip(scheduleQueryResult.message);
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
    } else {
        //异步请求查询指定周数课表
        $.ajax({
            url: "/schedulequery",
            data: {week: week},
            type: 'post',
            success: function (scheduleQueryResult) {
                //隐藏进度条
                $("#loadingToast, .weui_mask").hide();
                if (scheduleQueryResult.success === true) {
                    //更新当前选中周数
                    sessionStorage.setItem("scheduleWeek", scheduleQueryResult.week);
                    $("#currentWeek").text("第" + scheduleQueryResult.week + "周");
                    handleScheduleQueryResult(scheduleQueryResult);
                } else {
                    showCustomErrorTip(scheduleQueryResult.message);
                }
            },
            error: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.status === 503) {
                    //网络连接超时
                    showCustomErrorTip(result.responseJSON.message);
                }
                showNetworkErrorTip();
            }
        });
    }
}

//处理课表查询结果
function handleScheduleQueryResult(scheduleQueryResult) {
    //获取课表信息列表
    let scheduleList = scheduleQueryResult.scheduleList;
    //清除原课表信息
    clearTableSchedule();
    //显示课表信息
    if (scheduleList.length > 0) {
        //根据课表信息的单元格位置进行排序，位置值小的排列在列表的前方
        scheduleList.sort(function (a, b) {
            return a.position - b.position;
        });
        //合并相同位置的课程
        let list = [];
        for (let i = 0; i < scheduleList.length; i++) {
            //若当前元素的单元格位置的接下来多个元素课程的单元格位置相同，则需要将课程信息转化为合并课程信息
            let item = {};
            //合并课程信息取同位置课程信息中上课时长最大的值
            let mergedItemMaxLength = scheduleList[i].scheduleLength;
            for (let j = i + 1; j < scheduleList.length; j++) {
                if (scheduleList[j].position === scheduleList[i].position) {
                    if (j === i + 1) {
                        item.detailScheduleList = [];
                        item.merged = true;
                        item.detailScheduleList.push(scheduleList[i]);
                    }
                    item.detailScheduleList.push(scheduleList[j]);
                    if (scheduleList[j].scheduleLength > mergedItemMaxLength) {
                        mergedItemMaxLength = scheduleList[j].scheduleLength;
                    }
                } else {
                    break;
                }
            }
            if (item.merged) {
                item.row = scheduleList[i].row;
                item.column = scheduleList[i].column;
                item.colorCode = scheduleList[i].colorCode;
                item.maxLength = mergedItemMaxLength;
                list.push(item);
                i = i + item.detailScheduleList.length;
            } else {
                scheduleList[i].merged = false;
                list.push(scheduleList[i]);
            }
        }
        //解析课程信息
        for (let i = 0; i < list.length; i++) {
            let hiddenLength = list[i].merged ? list[i].maxLength : list[i].scheduleLength;
            if (hiddenLength > 1) {
                for (let j = list[i].row + 2; j <= list[i].row + hiddenLength; j++) {
                    //隐藏对应长度的单元格
                    hideCell(j, list[i].column + 1)
                }
            }
            //为每个课程配置在缓存课程数据列表中的下标位置，用于处理点击事件
            list[i].index = i;
            //更新课表视图
            updateTableSchedule(list[i]);
        }
        //缓存课程数据列表
        dataList = list;
    }
    //配置课程单元格点击响应事件
    $("td[data-index]").click(function () {
        if (dataList[$(this).attr("data-index")].merged) {
            showCustomScheduleDetail($(this).attr("data-index"));
            $("#customScheduleDetailDialog").popup();
        } else {
            showScheduleDetail($(this).attr("data-index"));
            $("#scheduleDetailDialog").popup();
        }
        isDialogOpen = true;
    });
    //显示查询成功提示
    $("#toast").show().delay(1000).hide(0);

}

//显示课程详细信息
function showScheduleDetail(index) {
    //加载课程详细信息
    let data = dataList[index];
    //加载视图数据
    $("#scheduleDetailDialog .modal-content .weui-form-preview__hd label").text(data.scheduleName);
    $("#scheduleDetailDialog .modal-content .weui-form-preview__hd em").text(data.scheduleType ? data.scheduleType : "自定义课程");
    $("#scheduleDetailDialog .modal-content .weui-form-preview__bd span").eq(0).text(data.scheduleLength + "节");
    $("#scheduleDetailDialog .modal-content .weui-form-preview__bd span").eq(1).text(data.scheduleLesson);
    if (data.scheduleTeacher) {
        $("#scheduleDetailDialog .modal-content .weui-form-preview__bd span").eq(2).text(data.scheduleTeacher);
    } else {
        $("#scheduleDetailDialog .modal-content .weui-form-preview__bd label").eq(2).hide();
        $("#scheduleDetailDialog .modal-content .weui-form-preview__bd span").eq(2).hide();
    }
    $("#scheduleDetailDialog .modal-content .weui-form-preview__bd span").eq(3).text(data.scheduleLocation);
    $("#scheduleDetailDialog .modal-content .weui-form-preview__bd span").eq(4).text("第" + data.minScheduleWeek + "周至第" + data.maxScheduleWeek + "周");
    if (data.id) {
        $("#scheduleDetailDialog .modal-content .weui-form-preview__ft button").attr("onclick", "deleteCustomSchedule('" + data.id + "'," + index + ")");
        $("#scheduleDetailDialog .modal-content .weui-form-preview__ft").show();
    }
}

//显示自定义课程详细信息
function showCustomScheduleDetail(index) {
    //加载课程详细信息
    let data = dataList[index];
    //加载视图数据
    let scheduleList = data.detailScheduleList;
    //隐藏多余的视图组件
    for (let i = scheduleList.length; i < 6; i++) {
        $("#customScheduleDetailDialog .modal-content .weui-form-preview").eq(i).hide();
    }
    for (let i = 0; i < scheduleList.length; i++) {
        let schedule = scheduleList[i];
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") .weui-form-preview__hd .weui-form-preview__label").text(schedule.scheduleName);
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") input[name='id']").val(schedule.id);
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") input[name='index']").val(i);
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") .weui-form-preview__bd .weui-form-preview__value:eq(0)").text(schedule.scheduleLength + "节");
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") .weui-form-preview__bd .weui-form-preview__value:eq(1)").text(schedule.scheduleLesson);
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") .weui-form-preview__bd .weui-form-preview__value:eq(2)").text(schedule.scheduleLocation);
        $("#customScheduleDetailDialog .weui-form-preview:eq(" + i + ") .weui-form-preview__bd .weui-form-preview__value:eq(3)").text("第" + schedule.minScheduleWeek + "周至第周");
        //配置按钮事件
        $("#customScheduleDetailDialog .weui-form-preview__ft button").each(function (j) {
            $(this).attr("onclick", "deleteCustomSchedule('" + $("#customScheduleDetailDialog .weui-form-preview:eq(" + j + ") input[name='id']").val() +
                "'," + index + "," + i + ")");
        })
    }
}

//课程详细信息窗口关闭按钮响应事件
function closeScheduleDetailDialog() {
    isDialogOpen = false;
    $.closePopup();
}

//自定义课程详细信息窗口关闭按钮响应事件
function closeCustomScheduleDetailDialog() {
    isDialogOpen = false;
    $.closePopup();
    if (hasDeleteAnyCustomSchedule) {
        window.location.reload();
    }
}

//清除原课表信息
function clearTableSchedule() {
    for (let i = 1; i <= 10; i++) {
        for (let j = 1; j <= 7; j++) {
            $("tr:eq(" + i + ") td:eq(" + j + ")").show()
                .text("").removeAttr("rowspan")
                .css("backgroundColor", "").removeAttr("style");
        }
    }
}

//更新单元格课表信息
function updateTableSchedule(schedule) {
    //更新单元格课表信息
    if (schedule.merged) {
        $("tr:eq(" + parseInt(schedule.row + 1) + ") td:eq(" + parseInt(schedule.column + 1) + ")").text("该时间段存在多个课程，点击查看详情")
            .attr("rowspan", schedule.maxLength).attr("data-index", schedule.index).css("backgroundColor", schedule.colorCode);
    } else {
        $("tr:eq(" + parseInt(schedule.row + 1) + ") td:eq(" + parseInt(schedule.column + 1) + ")").text(schedule.scheduleName + "@"
            + schedule.scheduleLocation).attr("rowspan", schedule.scheduleLength).attr("data-index", schedule.index).css("backgroundColor", schedule.colorCode);
    }
}

//显示网络错误提示
function showNetworkErrorTip() {
    weui.confirm('查询课表信息失败，请检查网络连接', {
        title: '错误提示',
        buttons: [{
            label: '返回主页',
            type: 'default',
            onClick: function () {
                window.location.href = '/index';
            }
        }, {
            label: '重新加载',
            type: 'primary',
            onClick: function () {
                window.location.reload();
            }
        }]
    });
}

//显示自定义错误提示
function showCustomErrorTip(message) {
    weui.confirm(message, {
        title: '错误提示',
        buttons: [{
            label: '返回主页',
            type: 'default',
            onClick: function () {
                window.location.href = '/index';
            }
        }, {
            label: '重新加载',
            type: 'primary',
            onClick: function () {
                window.location.reload();
            }
        }]
    });
}

//隐藏课程时长大于2时对应被占用的单元格,从0开始计算
function hideCell(row, column) {
    $("tr:eq(" + row + ") td:eq(" + column + ")").hide();
}

//设置显示今天星期几的文字特殊颜色
function setTextColor() {
    $day = new Date();
    $today = $day.getDay();
    if ($today === 0) {
        $("table th").eq(7).addClass("now");
    } else {
        $("table th").eq($today).addClass("now");
    }
}