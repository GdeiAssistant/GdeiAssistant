<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<jsp:include page="/js/profile/profile.jsp"/>

<script type="text/javascript" src="/js/userdata/userdata.js"></script>

<div class="hd">
    <h1 class="page_title">个人中心</h1>
</div>

<!-- 错误提示，显示时用$.show();隐藏时用$.hide(); -->
<div class="weui_toptips weui_warn js_tooltips"></div>

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="window.location.href='/avatar'">
        <div class="weui-cell__bd">
            <p>头像</p>
        </div>
        <div class="weui-cell__ft">
            <img id="avatar" style="border-radius: 50%;width: 50px;height: 50px;"
                 src="/img/avatar/default.png"/>
        </div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>用户名</p>
        </div>
        <div id="username" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:">
        <div class="weui-cell__bd">
            <p>用户组</p>
        </div>
        <div id="user_group" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showNicknameDialog()">
        <div class="weui-cell__bd">
            <p>昵称</p>
        </div>
        <div id="nickname_text" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeGender()">
        <div class="weui-cell__bd">
            <p>性别</p>
        </div>
        <div id="gender" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeBirthday()">
        <div class="weui-cell__bd">
            <p>年龄</p>
        </div>
        <div id="age" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeDegree()">
        <div class="weui-cell__bd">
            <p>学历</p>
        </div>
        <div id="degree" class="weui-cell__ft"></div>
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
    <div class="weui-cell" href="javascript:" onclick="showEnrollmentDialog()">
        <div class="weui-cell__bd">
            <p>入学年份</p>
        </div>
        <div id="enrollment_text" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showProfessionDialog()">
        <div class="weui-cell__bd">
            <p>职业</p>
        </div>
        <div id="profession_text" class="weui-cell__ft"></div>
    </div>
</div>

<input type="hidden" id="nickname_val" name="nickname_val">
<input type="hidden" id="gender_val" name="gender_val">
<input type="hidden" id="major_val" name="major_val">

<input type="hidden" id="school_index" name="school_index">
<input type="hidden" id="colleges_val" name="colleges_val">
<input type="hidden" id="high_school_val" name="high_school_val">
<input type="hidden" id="junior_high_school_val" name="junior_high_school_val">
<input type="hidden" id="primary_school_val" name="primary_school_val">

<div class="weui-cells">
    <div class="weui-cell" href="javascript:" onclick="changeLocation()">
        <div class="weui-cell__bd">
            <p>所在地</p>
        </div>
        <div id="location" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="changeHometown()">
        <div class="weui-cell__bd">
            <p>家乡</p>
        </div>
        <div id="hometown" class="weui-cell__ft"></div>
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
    <div class="weui-cell" href="javascript:" onclick="showSchoolDialog(0)">
        <div class="weui-cell__bd">
            <p>大专院校</p>
        </div>
        <div id="colleges" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showSchoolDialog(1)">
        <div class="weui-cell__bd">
            <p>高中/职中</p>
        </div>
        <div id="high_school" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showSchoolDialog(2)">
        <div class="weui-cell__bd">
            <p>初中</p>
        </div>
        <div id="junior_high_school" class="weui-cell__ft"></div>
    </div>
    <div class="weui-cell" href="javascript:" onclick="showSchoolDialog(3)">
        <div class="weui-cell__bd">
            <p>小学</p>
        </div>
        <div id="primary_school" class="weui-cell__ft"></div>
    </div>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/profile/user'">
        <div class="weui-cell__bd">
            <p>查看我的个人资料页</p>
        </div>
        <div class="weui-cell__ft">
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
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/function'">
        <div class="weui-cell__bd">
            <p>功能管理</p>
        </div>
        <div class="weui-cell__ft">
            <span id="functionBadge" class="weui-badge" style="display:none;margin-left: 5px;">New</span>
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/theme'">
        <div class="weui-cell__bd">
            <p>主题设置</p>
        </div>
        <div class="weui-cell__ft">
            <span id="themeBadge" class="weui-badge" style="display:none;margin-left: 5px;">New</span>
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/password'">
        <div class="weui-cell__bd">
            <p>修改密码</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/phone'">
        <div class="weui-cell__bd">
            <p>绑定手机</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
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
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/close'">
        <div class="weui-cell__bd">
            <p>删除账号</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/graduation'">
        <div class="weui-cell__bd">
            <p>毕业账号处理</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
    <a class="weui-cell weui-cell_access" href="javascript:" onclick="exportUserData()">
        <div class="weui-cell__bd">
            <p>下载用户数据</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/admin'">
        <div class="weui-cell__bd">
            <p>管理员选项</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="window.location.href = '/feedback'">
        <div class="weui-cell__bd">
            <p>帮助与反馈</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<div class="weui-cells">
    <a class="weui-cell weui-cell_access" href="javascript:"
       onclick="showLogoutConfirm()">
        <div class="weui-cell__bd">
            <p>退出账号</p>
        </div>
        <div class="weui-cell__ft">
        </div>
    </a>
</div>

<br>

<!-- 修改昵称窗口 -->
<div id="changeNickname" class="weui-popup__container">
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
                        <input id="nickname" class="weui-input" type="text" onkeyup="inputLengthCheck(this,32)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="changeNickname()">确认</a>
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

<!-- 修改学校信息弹窗 -->
<div id="changeSchool" class="weui-popup__container">
    <div class="weui-popup__overlay"></div>
    <div class="weui-popup__modal">
        <div class="toolbar">
            <div class="toolbar-inner">
                <a href="javascript:" style="left:0" class="picker-button close-popup">取消</a>
                <h1 class="title">修改学校</h1>
            </div>
        </div>
        <div class="modal-content">
            <div class="weui-cells">
                <div class="weui-cell">
                    <div class="weui-cell__hd">
                        <label class="weui-label">学校名称</label>
                    </div>
                    <div class="weui-cell__bd">
                        <input id="school" class="weui-input" type="text" onkeyup="inputLengthCheck(this,45)">
                    </div>
                </div>
            </div>
            <!-- 提交按钮 -->
            <div class="weui_btn_area">
                <a class="weui_btn weui_btn_primary" href="javascript:" onclick="changeSchool()">确认</a>
            </div>
        </div>
    </div>
</div>