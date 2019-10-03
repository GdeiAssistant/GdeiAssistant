<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="zh-CN">
<head>
    <title>拍好校园</title>
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/common_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/common_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-1.1.1.min_blue.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link title="pink" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_pink.css">
    <link title="blue" type="text/css" rel="alternate stylesheet" href="/css/common/weui-0.2.2.min_blue.css">
    <link title="pink" rel="alternate stylesheet" href="/css/common/jquery-weui.min_pink.css">
    <link title="blue" rel="alternate stylesheet" href="/css/common/jquery-weui.min_blue.css">
    <link rel="stylesheet" href="/css/common/amazeui.min.css">
    <link rel="stylesheet" href="/css/photograph/index.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery.scrollTo.min.js"></script>
    <script type="text/javascript" src="/js/common/amazeui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/photograph/upload.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<!-- 图片文件上传表单 -->
<form id="uploadForm" style="display: none" enctype="multipart/form-data">
    <input id="imageFileInput_1" accept="image/*" type="file" hidden="hidden">
    <input id="imageFileInput_2" accept="image/*" type="file" hidden="hidden">
    <input id="imageFileInput_3" accept="image/*" type="file" hidden="hidden">
    <input id="imageFileInput_4" accept="image/*" type="file" hidden="hidden">
</form>

<section id="upload">

    <div class="am-form-group">
        <label>标题/名字<span class="red-text">*</span> </label>
        <input type="text" maxlength="25" id="title" class="am-form-field am-round" placeholder="输入照片标题或你的名字">
    </div>
    <div class="am-form-group">
        <label>照片类型</label>
        <input type="radio" class="" name="type" value="1" checked>生活照
        <input type="radio" class="" name="type" value="2">校园照
    </div>
    <div style="margin-bottom: 10px">
        <i class="am-icon-picture-o"></i>选择主图<span class="red-text">*</span>
    </div>
    <div id="main-photo-box" class="am-vertical-align" onclick="$('#imageFileInput_1').click()">
        <div class="am-vertical-align am-vertical-align-middle preview-box">
            <i class="am-icon-plus-circle add-picture" style="font-size: 40px;line-height: 120px;color: #17bcd4;"></i>
        </div>
    </div>

    <br>

    <div style="margin-bottom: 10px">
        <i class="am-icon-picture-o"></i>选择副图(选填，可多选，最多三张)
    </div>
    <div class="am-g" id="second-photo-box">
        <div class="am-u-sm-4" onclick="$('#imageFileInput_2').click()">
            <div id="second-photo-box-1" class="am-vertical-align">
                <div class="am-vertical-align-middle">
                    <i class="am-icon-plus-circle"></i>
                </div>
            </div>
        </div>
        <div class="am-u-sm-4" onclick="$('#imageFileInput_3').click()">
            <div id="second-photo-box-2" class="am-vertical-align">
                <div class="am-vertical-align-middle">
                    <i class="am-icon-plus-circle"></i>
                </div>
            </div>
        </div>
        <div class="am-u-sm-4" onclick="$('#imageFileInput_4').click()">
            <div id="second-photo-box-3" class="am-vertical-align">
                <div class="am-vertical-align-middle">
                    <i class="am-icon-plus-circle"></i>
                </div>
            </div>
        </div>
    </div>

    <br>

    <button class="am-btn am-btn-default" id="clearSecondPicture" onclick="clearPictures()">
        <i class="am-icon-trash"></i>清空图片
    </button>

    <div class="am-form-group" id="say-something">
        <label>说点什么吧</label>
        <textarea class="am-form-field am-radius" onkeyup="inputLengthCheck(this,150)" rows="4" id="content"
                  placeholder="选填，可以填写感慨/对大学的期待..."></textarea>
        <span id="word-count">0/150字</span>
    </div>

    <br>

    <button type="button" class="am-btn am-btn-primary  am-round" id="submit" onclick="submitUpload()">确认提交</button>

</section>

<br><br>

<!-- 底部菜单 -->
<footer id="toolbar">
    <div class="am-btn-group am-btn-group-justify bottom-btns" style="box-shadow: 0 -1px 5px #989898;">
        <a class="am-btn am-btn-danger" role="button" href="javascript:" onclick="window.location.href='/photograph'">
            <img width="20px" height="20px" src="/img/photograph/home.png"/><br>返回首页</a>
        <a class="am-btn am-btn-success" role="button" href="javascript:"
           onclick="window.location.href='/photograph/upload'">
            <img width="20px" height="20px" src="/img/photograph/refresh.png"/><br>刷新页面</a>
    </div>
</footer>

</body>
</html>