<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>课表查询</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <script>document.write("<link rel='stylesheet' href='/css/schedule/schedule.css?time=" + Date.now() + "'><\/>");</script>
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/schedule/schedule.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div>
    <div class="weui_cells_title" style="float: left;" onclick="history.go(-1)">返回</div>
    <div class="weui_cells_title" style="float: right" onclick="showAddCustomScheduleDialog()">添加自定义课程</div>
    <div class="hd">
        <h1 class="page_title" style="clear:both;margin-top: 35px">我的课程表</h1>
    </div>
</div>

<!-- 选择查询的周数 -->
<div onclick="selectQueryWeek()" style="width:100%;height:auto;box-sizing: border-box;margin-bottom:10px;">
    <div style="height:3rem;width:100%;line-height: 3rem;text-align: center;position:relative">
        <p id="currentWeek">选择周数</p>
        <span style="margin-left:35px;position: absolute;top: 0;display: inline-block;width:1rem;height:100%">
            <img style="width:0.7rem;margin-top: 1.18rem;"
                 src="/img/schedule/select.png">
        </span>
    </div>
</div>

<table>
    <tr>
        <th></th>
        <th>周一</th>
        <th>周二</th>
        <th>周三</th>
        <th>周四</th>
        <th>周五</th>
        <th>周六</th>
        <th>周日</th>
    </tr>
    <tr>
        <td>1</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>2</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>3</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>4</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>5</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>6</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>7</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>8</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>9</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
    <tr>
        <td>10</td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
        <td></td>
    </tr>
</table>

<!-- 添加自定义课程窗口 -->
<div id="addCustomScheduleDialog" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup"
                   onclick="isDialogOpen=false">取消</a>
                <h1 class="title">添加自定义课程</h1>
            </div>
        </div>
        <div class="modal-content">
            <div id="error" style="display: none;color: red;" class="weui-cells__title"></div>
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">课程名称</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="scheduleName" class="weui-input" type="text" maxlength="50"
                               placeholder="请填写课程名称，不超过50字" onkeyup="inputLengthCheck(this,50)">
                    </div>
                </div>
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">上课地点</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="scheduleLocation" class="weui-input" type="text" maxlength="25"
                               placeholder="请填写上课地点，不超过25字" onkeyup="inputLengthCheck(this,25)">
                    </div>
                </div>
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">上课时间</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input type="hidden" id="row" value="">
                        <input type="hidden" id="column" value="">
                        <a id="timeSelector" href="javascript:" onclick="selectScheduleTime()">点击选择上课时间</a>
                    </div>
                </div>
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">上课时长</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input type="hidden" id="length" value="">
                        <a id="lengthSelector" href="javascript:" onclick="selectScheduleLength()">点击选择上课时长</a>
                    </div>
                </div>
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">上课周数</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input type="hidden" id="min_week" value="">
                        <input type="hidden" id="max_week" value="">
                        <a id="weekSelector" href="javascript:" onclick="selectScheduleWeek()">点击选择上课周数</a>
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a id="submit" class="weui_btn weui_btn_primary" href="javascript:" onclick="addCustomSchedule()">提交</a>
            </div>
        </div>
    </div>
</div>

<!-- 加载中提示框 -->
<div id="loadingToast" class="weui_loading_toast" style="display: none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <div class="weui_loading">
            <div class="weui_loading_leaf weui_loading_leaf_0"></div>
            <div class="weui_loading_leaf weui_loading_leaf_1"></div>
            <div class="weui_loading_leaf weui_loading_leaf_2"></div>
            <div class="weui_loading_leaf weui_loading_leaf_3"></div>
            <div class="weui_loading_leaf weui_loading_leaf_4"></div>
            <div class="weui_loading_leaf weui_loading_leaf_5"></div>
            <div class="weui_loading_leaf weui_loading_leaf_6"></div>
            <div class="weui_loading_leaf weui_loading_leaf_7"></div>
            <div class="weui_loading_leaf weui_loading_leaf_8"></div>
            <div class="weui_loading_leaf weui_loading_leaf_9"></div>
            <div class="weui_loading_leaf weui_loading_leaf_10"></div>
            <div class="weui_loading_leaf weui_loading_leaf_11"></div>
        </div>
        <p class="weui_toast_content">数据加载中</p>
    </div>
</div>

<!-- 查询成功弹框 -->
<div id="toast" style="display:none">
    <div class="weui_mask_transparent"></div>
    <div class="weui_toast">
        <i class="weui_icon_toast"></i>
        <p class="weui_toast_content">加载课表成功</p>
    </div>
</div>

<!-- 普通课程详细信息窗口 -->
<div id="scheduleDetailDialog" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button" onclick="closeScheduleDetailDialog()">关闭</a>
                <h1 class="title">课程详细信息</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class='weui-form-preview'>
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 12rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'></label>
                    <em class='weui-form-preview__value'></em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'><label class='weui-form-preview__label'>课程长度</label><span
                            class='weui-form-preview__value'></span></div>
                    <div class='weui-form-preview__item'><label class='weui-form-preview__label'>上课节数</label><span
                            class='weui-form-preview__value'></span></div>
                    <div class='weui-form-preview__item'><label class='weui-form-preview__label'>任课教师</label><span
                            class='weui-form-preview__value'></span></div>
                    <div class='weui-form-preview__item'><label class='weui-form-preview__label'>上课地点</label><span
                            class='weui-form-preview__value'></span></div>
                    <div class='weui-form-preview__item'><label class='weui-form-preview__label'>上课周数</label><span
                            class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div style="display: none" class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

<!-- 自定义课程详细信息窗口 -->
<div id="customScheduleDetailDialog" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button" onclick="closeCustomScheduleDetailDialog()">关闭</a>
                <h1 class="title">课程详细信息</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class='weui-form-preview'>
                <input type="hidden" name="id">
                <input type="hidden" name="index">
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 10rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'>
                    </label>
                    <em class='weui-form-preview__value'>自定义课程</em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>课程长度</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课节数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课地点</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课周数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
            <div class='weui-form-preview'>
                <input type="hidden" name="id">
                <input type="hidden" name="index">
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 10rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'>
                    </label>
                    <em class='weui-form-preview__value'>自定义课程</em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>课程长度</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课节数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课地点</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课周数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
            <div class='weui-form-preview'>
                <input type="hidden" name="id">
                <input type="hidden" name="index">
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 10rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'>
                    </label>
                    <em class='weui-form-preview__value'>自定义课程</em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>课程长度</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课节数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课地点</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课周数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
            <div class='weui-form-preview'>
                <input type="hidden" name="id">
                <input type="hidden" name="index">
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 10rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'>
                    </label>
                    <em class='weui-form-preview__value'>自定义课程</em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>课程长度</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课节数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课地点</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课周数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
            <div class='weui-form-preview'>
                <input type="hidden" name="id">
                <input type="hidden" name="index">
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 10rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'>
                    </label>
                    <em class='weui-form-preview__value'>自定义课程</em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>课程长度</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课节数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课地点</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课周数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
            <div class='weui-form-preview'>
                <input type="hidden" name="id">
                <input type="hidden" name="index">
                <div class='weui-form-preview__hd'>
                    <label class='weui-form-preview__label'
                           style='max-width: 10rem;overflow: hidden;text-overflow:ellipsis;white-space: nowrap;'>
                    </label>
                    <em class='weui-form-preview__value'>自定义课程</em>
                </div>
                <div class='weui-form-preview__bd'>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>课程长度</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课节数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课地点</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                    <div class='weui-form-preview__item'>
                        <label class='weui-form-preview__label'>上课周数</label>
                        <span class='weui-form-preview__value'></span>
                    </div>
                </div>
                <div class='weui-form-preview__ft'>
                    <button type='submit' class='weui-form-preview__btn weui-form-preview__btn_primary'
                            href='javascript:'>删除自定义课程
                    </button>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
