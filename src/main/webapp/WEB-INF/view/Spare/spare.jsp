<%@ page import="java.util.Date" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2018/1/6
  Time: 01:12
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>空课室查询</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script type="text/javascript" src="/js/spare/spare.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">空课室查询</h1>
</div>

<div id="form">

    <div class="weui-cells__title">查询条件</div>
    <form id="spareForm" class="weui-cells weui-cells_form">

        <!-- 校区 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">校区</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="zone" name="zone">
                    <option value="0" select="selected">不限</option>
                    <option value="1">海珠</option>
                    <option value="2">花都</option>
                    <option value="3">广东轻工南海校区</option>
                    <option value="4">业余函授校区</option>
                </select>
            </div>
        </div>

        <!-- 教室类型 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">教室类别</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="type" name="type">
                    <option value="0">不限</option>
                    <option value="1">不用课室的课程</option>
                    <option value="2">操场</option>
                    <option value="3">大多媒体</option>
                    <option value="4">电脑专业机房</option>
                    <option value="5">雕塑室</option>
                    <option value="6">多媒体教室</option>
                    <option value="7">翻译室</option>
                    <option value="8">服装实验室</option>
                    <option value="9">钢琴室</option>
                    <option value="10">钢琴室</option>
                    <option value="11">公共机房</option>
                    <option value="12">国画临摹室</option>
                    <option value="13">画室</option>
                    <option value="14">化学实验室</option>
                    <option value="15">机房</option>
                    <option value="16">教具室</option>
                    <option value="17">教育实验室</option>
                    <option value="18">解剖实验室</option>
                    <option value="19">金融数学实验室</option>
                    <option value="20">美术课室</option>
                    <option value="21">蒙氏教学法专用课室</option>
                    <option value="22">模型制作实验室</option>
                    <option value="23">平面制作实验室</option>
                    <option value="24">琴房</option>
                    <option value="25">摄影实验室</option>
                    <option value="26">声乐课室</option>
                    <option value="27">生物实验室</option>
                    <option value="28">实训室</option>
                    <option value="29">视唱练耳室</option>
                    <option value="30">陶艺室</option>
                    <option value="31">体操房</option>
                    <option value="32">网络实验室</option>
                    <option value="33">微格课室</option>
                    <option value="34">无须课室</option>
                    <option value="35">舞蹈室</option>
                    <option value="36">舞蹈室</option>
                    <option value="37">物理实验室</option>
                    <option value="38">小多媒体</option>
                    <option value="39">小多媒体(&lt;70)</option>
                    <option value="40">小普通课室(&lt;70)</option>
                    <option value="41">小组课室</option>
                    <option value="42">形体房</option>
                    <option value="43">音乐室</option>
                    <option value="44">音乐专业课室</option>
                    <option value="45">语音室</option>
                    <option value="46">智能录像室</option>
                    <option value="47">中多媒体(70-100)</option>
                    <option value="48">专业课教室</option>
                    <option value="49">专业理论课室</option>
                    <option value="50">专业实验室</option>
                    <option value="51">咨询室</option>
                    <option value="52">综合绘画实验室</option>
                </select>
            </div>
        </div>

        <!-- 座位数大于等于 -->
        <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">座位数</label></div>
            <div class="weui-cell__bd">
                <input id="minSeating" name="minSeating" class="weui-input" type="number" pattern="[0-9]*"
                       placeholder="大于等于，选填"/>
            </div>
        </div>

        <!-- 座位数小于等于 -->
        <div class="weui-cell">
            <div class="weui-cell__hd"><label class="weui-label">座位数</label></div>
            <div class="weui-cell__bd">
                <input id="maxSeating" name="maxSeating" class="weui-input" type="number" pattern="[0-9]*"
                       placeholder="小于等于，选填"/>
            </div>
        </div>

        <!-- 起始时间 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">起始时间</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="startTime" name="startTime">
                    <c:forEach var="date" items="${DateList}" varStatus="status">
                        <fmt:formatDate value="${date}" pattern="yyyy-MM-dd" var="dateString"/>
                        <option value="${status.index}">${dateString}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <!-- 结束时间 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">结束时间</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="endTime" name="endTime">
                    <c:forEach var="date" items="${DateList}" varStatus="status">
                        <fmt:formatDate value="${date}" pattern="yyyy-MM-dd" var="dateString"/>
                        <option value="${status.index}">${dateString}</option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <!-- 星期 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">星期</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="week" name="week">
                    <option value="0" select="selected">一</option>
                    <option value="1">二</option>
                    <option value="2">三</option>
                    <option value="3">四</option>
                    <option value="4">五</option>
                    <option value="5">六</option>
                    <option value="6">日</option>
                </select>
            </div>
        </div>

        <!-- 单双周 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">单双周</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="weekType" name="weekType">
                    <option value="0" select="selected">不限</option>
                    <option value="1">单</option>
                    <option value="2">双</option>
                </select>
            </div>
        </div>

        <!-- 节数 -->
        <div class="weui-cell weui-cell_select weui-cell_select-after">
            <div class="weui-cell__hd">
                <label class="weui-label">节数</label>
            </div>
            <div class="weui-cell__bd">
                <select class="weui-select" id="classNumber" name="classNumber">
                    <option selected="selected" value="0">第1,2节</option>
                    <option value="1">第3节</option>
                    <option value="2">第4,5节</option>
                    <option value="3">第6,7节</option>
                    <option value="4">第8,9节</option>
                    <option value="5">第10,11,12节</option>
                    <option value="6">上午</option>
                    <option value="7">下午</option>
                    <option value="8">晚上</option>
                    <option value="9">白天</option>
                    <option value="10">整天</option>
                </select>
            </div>
        </div>

    </form>

    <!-- 查询中弹框 -->
    <div class="weui_mask" style="display: none"></div>
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
            <p class="weui_toast_content">查询中</p>
        </div>
    </div>

    <!-- 查询按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="postQueryForm()">查询</a>
    </div>

</div>

<div id="result" style="display: none">
    <div class="weui-cells__title">查询结果</div>
    <div id="resultList" class="weui-cells">

    </div>

    <!-- 全屏弹出详细信息 -->
    <div id="detail" class="weui-popup__container">
        <div class="weui-popup__overlay"></div>
        <div class="weui-popup__modal">
            <div class="toolbar">
                <div class="toolbar-inner">
                    <a href="javascript:" style="left:0" class="picker-button close-popup">关闭</a>
                    <h1 class="title">课室详细信息</h1>
                </div>
            </div>
            <div class="modal-content">
                <div class="hd">
                    <h1 class="page_title">空课室信息</h1>
                </div>
                <div class="weui-cells">
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>教室编号</p>
                        </div>
                        <div id="detail_number" class="weui-cell__ft"></div>
                    </div>
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>教室名称</p>
                        </div>
                        <div id="detail_name" class="weui-cell__ft"></div>
                    </div>
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>教室类别</p>
                        </div>
                        <div id="detail_type" class="weui-cell__ft"></div>
                    </div>
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>上课座位数</p>
                        </div>
                        <div id="detail_classSeating" class="weui-cell__ft"></div>
                    </div>
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>考试座位数</p>
                        </div>
                        <div id="detail_examSeating" class="weui-cell__ft"></div>
                    </div>
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>校区</p>
                        </div>
                        <div id="detail_zone" class="weui-cell__ft"></div>
                    </div>
                    <div class="weui-cell" href="javascript:">
                        <div class="weui-cell__bd">
                            <p>使用部门</p>
                        </div>
                        <div id="detail_seation" class="weui-cell__ft"></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

</body>
</html>
