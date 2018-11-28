//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//默认查询当前周课表
$(function () {
    postQueryForm();
    setTextColor();
});

//选择查询的周数
function selectQueryWeek() {
    weui.picker([
        {
            label: '1',
            value: 1
        },
        {
            label: '2',
            value: 2
        },
        {
            label: '3',
            value: 3
        },
        {
            label: '4',
            value: 4
        },
        {
            label: '5',
            value: 5
        },
        {
            label: '6',
            value: 6
        },
        {
            label: '7',
            value: 7
        },
        {
            label: '8',
            value: 8
        },
        {
            label: '9',
            value: 9
        },
        {
            label: '10',
            value: 10
        },
        {
            label: '11',
            value: 11
        },
        {
            label: '12',
            value: 12
        },
        {
            label: '13',
            value: 13
        },
        {
            label: '14',
            value: 14
        },
        {
            label: '15',
            value: 15
        },
        {
            label: '16',
            value: 16
        },
        {
            label: '17',
            value: 17
        },
        {
            label: '18',
            value: 18
        },
        {
            label: '19',
            value: 19
        },
        {
            label: '20',
            value: 20
        }
    ], {
        defaultValue: $("#currentWeek").val(),
        onConfirm: function (result) {
            changeCurrentWeek(result[0].value);
            postQueryForm(result[0].value);
        }
    });
}

//重新加载课表信息
function reQuerySchedule() {
    if ($("#currentWeek").val() === '0') {
        postQueryForm();
    } else {
        postQueryForm($("#currentWeek").val());
    }
}

//提交课表查询请求
function postQueryForm(week) {
    //显示进度条
    $("#loadingToast, .weui_mask").show();
    //判断是否查询指定周数
    if (week) {
        //异步请求查询课表
        $.ajax({
            url: "/schedulequery",
            type: 'post',
            success: function (scheduleQueryResult) {
                //隐藏进度条
                $("#loadingToast, .weui_mask").hide();
                if (scheduleQueryResult.success === true) {
                    changeCurrentWeek(scheduleQueryResult.week);
                    handleScheduleQueryResult(scheduleQueryResult);
                } else {
                    showCustomErrorTip(scheduleQueryResult.message);
                }
            },
            error: function (result) {
                if (result.status == 503) {
                    //网络连接超时
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
                    handleScheduleQueryResult(scheduleQueryResult);
                } else {
                    showCustomErrorTip(scheduleQueryResult.message);
                }
            },
            error: function (result) {
                $("#loadingToast, .weui_mask").hide();
                if (result.status == 503) {
                    //网络连接超时
                    showCustomErrorTip(result.responseJSON.message);
                }
                showNetworkErrorTip();
            }
        });
    }
}

//更新当前选中的周数信息
function changeCurrentWeek(week) {
    //更新当前选中周数
    $("#currentWeek").val(week);
    $("#currentWeekText").text("第" + week + "周");
}

//处理课表查询结果
function handleScheduleQueryResult(scheduleQueryResult) {
    //显示查询成功提示
    $("#toast").show().delay(1000).hide(0);
    //获取课表信息列表
    var scheduleList = scheduleQueryResult.scheduleList;
    //清除原课表信息
    clearTableSchedule();
    //显示课表信息
    if (scheduleList.length > 0) {
        for (var i = 0; i < scheduleList.length; i++) {
            if (scheduleList[i].scheduleLength > 1) {
                for (var j = scheduleList[i].row + 2; j <= scheduleList[i].row + scheduleList[i].scheduleLength; j++) {
                    hideCell(j, scheduleList[i].column + 1)
                }
            }
            updateTableSchedule(scheduleList[i].row + 1, scheduleList[i].column + 1
                , scheduleList[i].scheduleName, scheduleList[i].scheduleType
                , scheduleList[i].scheduleLesson, scheduleList[i].scheduleWeek
                , scheduleList[i].scheduleTeacher, scheduleList[i].scheduleLocation
                , scheduleList[i].scheduleLength, scheduleList[i].colorCode);
        }
    }
}

//清除原课表信息
function clearTableSchedule() {
    for (var i = 1; i <= 10; i++) {
        for (var j = 1; j <= 7; j++) {
            $("tr:eq(" + i + ") td:eq(" + j + ")").show()
                .text("").removeAttr("rowspan")
                .css("backgroundColor", "").removeAttr("style");
        }
    }
}

//更新单元格课表信息
function updateTableSchedule(row, column, name, type, lesson, week, teacher, region, length, color) {
    //更新本次查询单元格课表信息
    $("tr:eq(" + row + ") td:eq(" + column + ")").text(name + "@" + region).attr("rowspan", length).css("backgroundColor", color);
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
                reQuerySchedule();
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
                reQuerySchedule();
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