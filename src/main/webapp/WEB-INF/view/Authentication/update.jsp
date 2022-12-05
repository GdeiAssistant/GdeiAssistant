<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>实名认证</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/authentication/authentication.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui_cells_title" onclick="history.go(-1)">返回</div>

<div class="hd">
    <h1 class="page_title">实名认证</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<!-- 中国居民身份证实名认证 -->
<div class="mainland_authentication">
    <div class="weui_cells weui_cells_form">
        <form>
            <div class="weui_cell">
                <div class="weui_cell_hd" style="width: 4rem">
                    <label class="weui_label" style="width: 4rem">姓名</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="mainland-idcard-name" class="weui_input" type="text" maxlength="15" name="name"
                           placeholder="请输入你的姓名">
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd" style="width: 4rem">
                    <label class="weui_label" style="width: 4rem">身份证号</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="mainland-idcard-number" class="weui_input" type="text" maxlength="18" name="number"
                           placeholder="请输入你的15位或18位中国居民身份证号码">
                </div>
            </div>
        </form>
    </div>

    <!-- 提交按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="updateMainLandIDCardAuthentication()">认证</a>
    </div>

    <p class="page_desc" style="margin-top: 25px" onclick="switchToHMTFAuthentication()">港澳台及海外用户实名认证</p>

</div>

<!-- 港澳台和海外用户实名认证 -->
<div class="hmtf_authentication" style="display: none">
    <div class="weui_cells weui_cells_form">
        <form>
            <div class="weui_cell">
                <div class="weui_cell_hd" style="width: 4rem">
                    <label class="weui_label" style="width: 4rem">姓名</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="hmtf-idcard-name" class="weui_input" type="text" maxlength="15" name="name"
                           placeholder="请输入你的姓名">
                </div>
            </div>
            <div class="weui_cell">
                <div class="weui_cell_hd" style="width: 4rem">
                    <label class="weui_label" style="width: 4rem">证件号码</label>
                </div>
                <div class="weui_cell_bd weui_cell_primary">
                    <input id="hmtf-idcard-number" class="weui_input" type="text" maxlength="18" name="number"
                           placeholder="请输入你的证件号码">
                </div>
            </div>
            <div class="weui-cell weui-cell_select weui-cell_select-after">
                <div class="weui-cell__hd" style="width: 4.1rem">
                    <label class="weui-label" style="width: 4.1rem">证件类型</label>
                </div>
                <div class="weui-cell__bd">
                    <select class="weui-select" id="hmtf-idcard-type" name="type">
                        <option value="1" selected>港澳居民来往内地通行证</option>
                        <option value="2">台湾居民来往大陆通行证</option>
                        <option value="3">港澳台居民居住证</option>
                        <option value="4">护照</option>
                        <option value="5">外国人永久居留证</option>
                        <option value="6">其他证件</option>
                    </select>
                </div>
            </div>
            <!-- 证件照片上传 -->
            <div class="weui-gallery" id="gallery">
                <span class="weui-gallery__img" id="hmtf-idcard-gallery-image" index="0"></span>
                <div class="weui-gallery__opr">
                    <a href="javascript:" class="weui-gallery__del" onclick="deleteIDCardImage()">
                        <i class="weui-icon-delete weui-icon_gallery-delete"></i>
                    </a>
                </div>
            </div>
            <div class="weui-cell">
                <div class="weui-cell__bd">
                    <div class="weui-uploader">
                        <div class="weui-uploader__hd">
                            <p class="weui-uploader__title">证件照片上传</p>
                            <div class="weui-uploader__info">0/3    </div>
                        </div>
                        <div class="weui-uploader__bd">
                            <ul class="weui-uploader__files" id="hmtf-idcard-uploader-files"></ul>
                            <div class="weui-uploader__input-box">
                                <input id="hmtf-idcard-uploader-input" class="weui-uploader__input" type="file" accept="image/*">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </form>
    </div>

    <!-- 提交按钮 -->
    <div class="weui_btn_area">
        <a class="weui_btn weui_btn_primary" href="javascript:" onclick="updateHMTFIDCardAuthentication()">认证</a>
    </div>

    <p class="page_desc" style="margin-top: 25px" onclick="switchToMainlandAuthentication()">
        中国居民身份证实名认证</p>

</div>

</body>
</html>
