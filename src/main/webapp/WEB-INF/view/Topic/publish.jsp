<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>话题</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/common.css">
    <link title="default" type="text/css" rel="stylesheet" href="/css/common/weui.min.css">
    <link rel="stylesheet" href="/css/topic/publish.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/weui.min.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script type="application/javascript" src="/js/common/themeLoader.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/topic/publish.js?time=" + Date.now() + "'><\/script>");</script>
</head>
<body>

<div class="weui-tab">

    <div class="weui-tab__panel">

        <div class="hd">
            <h1 class="page_title">发布话题</h1>
        </div>

        <span id="dynamic_topic_span"></span>

        <!-- 提交的话题信息表单 -->
        <div class="weui-cells weui-cells_form">
            <form>
                <!-- 话题标签 -->
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">话题</label>
                    </div>
                    <div class="weui-cell__bd weui-cell_primary">
                        <p class="topic_line">#</p>
                        <input id="topic" class="weui-input" type="text" maxlength="15"
                               name="topic" placeholder="请输入你的话题" onkeyup="setTopicInputDynamicWidth(this)">
                        <p class="topic_line">#</p>
                    </div>
                </div>
                <!-- 话题内容 -->
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">内容</label>
                    </div>
                    <div class="weui-cell__bd">
                        <textarea id="content" name="content" class="weui-textarea" placeholder="请输入你的内容" rows="3"
                                  onkeyup="inputLengthCheck(this,250)"></textarea>
                        <div class="weui-textarea-counter"><span>0</span>/250</div>
                    </div>
                </div>
                <!-- 图片上传 -->
                <div class="weui-gallery" id="gallery">
                    <span class="weui-gallery__img" id="galleryImg" index="0"></span>
                    <div class="weui-gallery__opr">
                        <a href="javascript:" class="weui-gallery__del" onclick="deleteImage()">
                            <i class="weui-icon-delete weui-icon_gallery-delete"></i>
                        </a>
                    </div>
                </div>
                <div class="weui-cell">
                    <div class="weui-cell__bd">
                        <div class="weui-uploader">
                            <div class="weui-uploader__hd">
                                <p class="weui-uploader__title">图片上传（可选）</p>
                                <div class="weui-uploader__info">0/9</div>
                            </div>
                            <div class="weui-uploader__bd">
                                <ul class="weui-uploader__files" id="uploaderFiles"></ul>
                                <div class="weui-uploader__input-box">
                                    <input id="uploaderInput" class="weui-uploader__input" type="file" accept="image/*">
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </form>
        </div>

        <!-- 提交按钮 -->
        <div class="weui-btn_area">
            <a class="weui-btn weui-btn_primary" href="javascript:" onclick="postTopicForm()">提交</a>
        </div>

    </div>

    <!-- 提交中弹框 -->
    <div role="alert" id="loadingToast" style="display: none;">
        <div class="weui-mask_transparent"></div>
        <div class="weui-toast">
            <span class="weui-primary-loading weui-icon_toast">
              <span class="weui-primary-loading__dot"></span>
            </span>
            <p class="weui-toast__content">提交中</p>
        </div>
    </div>

    <div class="weui-tabbar">
        <a href="javascript:" onclick="window.location.href='/topic'" class="weui-tabbar__item weui-bar__item">
            <img src="/img/topic/home.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">首页</p>
        </a>
        <a href="javascript:" onclick="window.location.href='/topic/publish'"
           class="weui-tabbar__item weui-bar__item_on">
            <img src="/img/topic/publish.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">发布</p>
        </a>
        <a href="javascript:" onclick="window.location.href='/topic/search'" class="weui-tabbar__item">
            <img src="/img/topic/search.png" class="weui-tabbar__icon">
            <p class="weui-tabbar__label">搜索</p>
        </a>
    </div>
</div>

</body>
</html>