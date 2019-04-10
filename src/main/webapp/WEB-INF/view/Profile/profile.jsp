<%@ page contentType="text/html;charset=UTF-8" %>

<jsp:include page="/js/profile/profile.jsp"/>

<div class="hd">
    <h1 class="page_title">个人中心</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<!-- 头像文件上传表单 -->
<form id="uploadForm" enctype="multipart/form-data">
    <input id="avatarFileInput" name="avatar" accept="image/*" type="file" hidden="hidden">
</form>

<!-- 头像裁剪参数 -->
<input id="x" type="hidden">
<input id="y" type="hidden">
<input id="width" type="hidden">
<input id="height" type="hidden">

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="selectAvatarImage()">
        <div class="weui-cell__bd">
            <p>头像</p>
        </div>
        <div class="weui-cell__ft">
            <img id="avatar" style="border-radius: 50%;width: 50px;height: 50px;"
                 src="/img/avatar/default.png"/>
        </div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showKicknameDialog()">
        <div class="weui-cell__bd">
            <p>昵称</p>
        </div>
        <div id="kickname_text" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeGender()">
        <div class="weui-cell__bd">
            <p>性别</p>
        </div>
        <div id="gender" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeGenderOrientation()">
        <div class="weui-cell__bd">
            <p>性取向</p>
        </div>
        <div id="genderOrientation" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeFaculty()">
        <div class="weui-cell__bd">
            <p>院系</p>
        </div>
        <div id="faculty" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showMajorDialog()">
        <div class="weui-cell__bd">
            <p>专业</p>
        </div>
        <div id="major_text" class="weui-cell__ft"></div>
    </div>
</div>

<input type="hidden" id="kickname_val" name="kickname_val">
<input type="hidden" id="gender_val" name="gender_val">
<input type="hidden" id="major_val" name="major_val">

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="changeRegion()">
        <div class="weui-cell__bd">
            <p>所在地</p>
        </div>
        <div id="location" class="weui-cell__ft"></div>
    </div>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/introduction'">
        <div class="weui-cell__bd">
            <p>个人简介</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/authentication'">
        <div class="weui-cell__bd">
            <p>实名认证</p>
        </div>
        <div class="weui-cell__ft">
            <div style="display: none" id="unauthenticated">
                未认证
                <span id="authenticationBadge" class="weui-badge weui-badge_dot"
                      style="margin-left: 5px;margin-right: 5px;"></span>
            </div>
            <div style="display: none" id="authenticated">
                已认证
            </div>
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/privacy'">
        <div class="weui-cell__bd">
            <p>隐私设置</p>
        </div>
        <div class="weui-cell__ft">
            <span id="privacyBadge" class="weui-badge" style="display:none;margin-left: 5px;">New</span>
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/function'">
        <div class="weui-cell__bd">
            <p>功能管理</p>
        </div>
        <div class="weui-cell__ft">
            <span id="functionBadge" class="weui-badge" style="display:none;margin-left: 5px;">New</span>
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/close'">
        <div class="weui-cell__bd">
            <p>删除账号</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<br>

<!-- 裁剪头像弹窗 -->
<div id="drawImageDialog" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <a href="javascript:" style="right:0" class="picker-button close-popup" onclick="drawImageAndUpload()">使用</a>
                <h1 class="title">上传头像</h1>
            </div>
        </div>
        <div class="modal-content img-container" style="text-align:center">
            <img id="drawImage" style=" width:80% ; height: 60% ;margin-top:1.17647059em"/>
        </div>
    </div>
</div>

<!-- 修改昵称窗口 -->
<div id="changeKickname" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">更改昵称</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells__title">昵称将作为你的个人资料公开显示</div>
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">昵称</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="kickname" class="weui-input" type="text" onkeyup="inputLengthCheck(this,24)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="changeKickname()">确认</a>
            </div>
        </div>
    </div>
</div>

<!-- 自定义性别窗口 -->
<div id="customGender" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">自定义性别</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells__title">请输入你的性别</div>
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">性别</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="customGenderName" class="weui-input" type="text" onkeyup="inputLengthCheck(this,50)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="submitCustomGender()">确认</a>
            </div>
        </div>
    </div>
</div>

<!-- 修改专业弹窗 -->
<div id="changeMajor" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">修改专业</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">专业</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="major" class="weui-input" type="text" onkeyup="inputLengthCheck(this,20)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="changeMajor()">确认</a>
            </div>
        </div>
    </div>
</div>