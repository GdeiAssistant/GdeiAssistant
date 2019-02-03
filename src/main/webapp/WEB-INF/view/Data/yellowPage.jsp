<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>黄页查询</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"/>
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit"/>
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="stylesheet" href="/css/common/common.css">
    <link rel="stylesheet" href="/css/common/weui-0.2.2.min.css">
    <link rel="stylesheet" href="/css/common/weui-1.1.1.min.css">
    <link rel="stylesheet" href="/css/common/jquery-weui.min.css">
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/jquery-weui.min.js"></script>
    <script>document.write("<script type='text/javascript' src='/js/data/yellowPage.js?time=" + Date.now() + "'><\/script>");</script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
</head>
<body>

<div>
    <div class="weui_cells_title" onclick="history.go(-1)">返回</div>
    <div class="hd">
        <h1 class="page_title" style="margin-top: 15px">黄页查询</h1>
    </div>
</div>

<div id="result">

</div>

<!-- 加载中弹框 -->
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
        <p class="weui_toast_content">加载数据中</p>
    </div>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<!-- 黄页详细信息弹窗 -->
<div id="detail" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">关闭</a>
                <h1 class="title">详细信息</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="hd">
                <h1 class="page_title" style="font-size: 25px;margin-top: 15px"></h1>
            </div>
            <div id="detail_info" class="weui-cells">
                <div class="weui-cell" href="javascript:">
                    <div class="weui-cell__bd">
                        <p>类别</p>
                    </div>
                    <div class="weui-cell__ft"
                         style="padding-left:1rem;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;"></div>
                </div>
                <div class="weui-cell" href="javascript:">
                    <div class="weui-cell__bd">
                        <p>区域</p>
                    </div>
                    <div class="weui-cell__ft"
                         style="padding-left:1rem;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;"></div>
                </div>
                <div class="weui-cell" href="javascript:"
                     onclick="phoneCall($(this.getElementsByClassName('weui-cell__ft')).text())">
                    <div class="weui-cell__bd">
                        <p>主要电话</p>
                    </div>
                    <div class="weui-cell__ft"></div>
                </div>
                <div class="weui-cell" href="javascript:"
                     onclick="phoneCall($(this.getElementsByClassName('weui-cell__ft')).text())">
                    <div class="weui-cell__bd">
                        <p>次要电话</p>
                    </div>
                    <div class="weui-cell__ft"></div>
                </div>
                <div class="weui-cell" href="javascript:"
                     onclick="copyDetailInfo($(this.getElementsByClassName('weui-cell__ft')).text())">
                    <div class="weui-cell__bd">
                        <p>地址</p>
                    </div>
                    <div class="weui-cell__ft"></div>
                </div>
                <div class="weui-cell" href="javascript:"
                     onclick="sendEmail($(this.getElementsByClassName('weui-cell__ft')).text())">
                    <div class="weui-cell__bd">
                        <p>邮箱</p>
                    </div>
                    <div class="weui-cell__ft"
                         style="padding-left:1rem;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;"></div>
                </div>
                <div class="weui-cell" href="javascript:"
                     onclick="switchWebSite($(this.getElementsByClassName('weui-cell__ft')).text())">
                    <div class="weui-cell__bd" style="margin-right: 1rem">
                        <p>网站</p>
                    </div>
                    <div class="weui-cell__ft"
                         style="padding-left:1rem;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;"></div>
                </div>
            </div>
            <p style="text-align: center;color: #888;padding-top: 0.5rem">点击对应的信息项可以实现快速操作</p>
        </div>
    </div>
</div>

</body>
</html>
