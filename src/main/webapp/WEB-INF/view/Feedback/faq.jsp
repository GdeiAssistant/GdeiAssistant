<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0">
    <meta content="telephone=no" name="format-detection">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/favicon/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/favicon/logo.png">
    <title>常见问题解答</title>
    <c:if test="${applicationScope.get('grayscale')}">
        <link rel="stylesheet" href="/css/common/grayscale.min.css">
    </c:if>
</head>
<body>

<div>
    <p style="text-align:center">《广东二师助手常见问题解答》</p>
    <p style="text-align:center">更新日期：2020年1月2日</p>
    <p style="text-align:right"><br></p>
    <p style="text-indent:14px;line-height:150%">
    <p><strong>用户登录</strong></p>
    <p style="text-indent:28px">
        Q：登录应用的用户名和密码是什么？
    </p>
    <p style="text-indent:28px">
        A：用户名和密码是校园网上网和登录学校教务系统的账号用户名和密码。若不了解自己的用户名和密码，请阅读<a href="/about/account">《广东二师助手校园网络账号说明》</a>。
    </p>
    <p style="text-indent:28px">
        Q：应用账号是否支持修改密码？
    </p>
    <p style="text-indent:28px">
        A：若用户为毕业学生用户，可以在应用内的个人中心修改密码。若用户非毕业学生用户，则系统会采取“被动更新”的密码同步策略，即用户在学校教务系统中修改了密码，只有在此应用中使用新密码成功登录，才会同步修改此系统中的用户账户密码。
    </p>
    <p style="text-indent:28px">
        Q：忘记了账户密码怎么办？
    </p>
    <p style="text-indent:28px">
        A：若用户为毕业学生用户，可以通过邮箱support@gdeiassistant.cn联系客服找回密码。若用户非毕业学生用户，则可以直接在学校网络中心申请重置密码，并使用新的密码重新登录本系统。
    </p>
    <p>&nbsp;</p>
    <p><strong>教务查询</strong></p>
    <p style="text-indent:28px">
        Q：为什么使用成绩/课表查询等功能会提示系统异常？
    </p>
    <p style="text-indent:28px">
        A：本应用部分的教务查询功能模块的正常运作一定程度上依赖于源站（广东第二师范学院网站）的稳定性和可用性。当源站进行维护或更新时，将可能导致应用的教务查询功能不可用。一般情况下，教务查询功能可以在24小时内恢复正常服务。若超过一定时间服务仍不可用，建议你在帮助和反馈页面提交故障工单或通过电子邮件联系support@gdeiassistant.cn。
    </p>
    <p style="text-indent:28px">
        Q：隐私设置中的教务数据缓存是什么？
    </p>
    <p style="text-indent:28px">
        A：该设置是用于控制是否授权本系统使用你的账户信息，自动、定期地从学校教务系统中获取、更新并缓存你的成绩和课表信息，以便提升用户使用教务查询功能时的性能和稳定性。默认情况下，该项设置值为关闭。
    </p>
    <p style="text-indent:28px">
        Q：保存四六级准考证号的功能是什么？每个账户只能保存一个准考证号吗？保存到应用中是否安全？
    </p>
    <p style="text-indent:28px">
        A：为了方便考生能及时查询到四六级考试成绩，避免遗忘准考证号的情况，应用推出了保存四六级准考证号的功能，考生可以在报名四六级考试打印准考证后，在此应用中保存自己的准考证号，待成绩公布需要查询考试成绩时，直接从应用中导出考号进行查询。每个账户只能保存一个准考证号，建议仅保存自己的准考证号。应用仅保存准考证号，不保存考生的姓名等其他信息。且设置了较严格的权限控制，只有账号所有者可以查询到自己账户下保存的准考证号，因此保存四六级准考证号功能具有较高的安全性。
    </p>
    <p style="text-indent:28px">
        Q：体测查询功能为何不能在Safari/Chrome等浏览器中使用？
    </p>
    <p style="text-indent:28px">
        A：体测查询功能由微信公众号“体适能”提供，该功能仅支持在微信中使用。
    </p>
    <p style="text-indent:28px">
        Q：教学评价功能如何使用？
    </p>
    <p style="text-indent:28px">
        A：用户进入教学评价功能页面后，可以选择是否直接提交教学评价信息。若选择直接提交，则点击提交信息按钮后，则已完成了教学评价工作。若不选择直接提交，则用户仍需要自行登录学校教务系统，进行最终确认并提交信息。
    </p>
    <p style="text-indent:28px">
        Q：校园卡充值功能如何使用？
    </p>
    <p style="text-indent:28px">
        A：校园卡充值功能目前仅支持APP客户端使用。用户在APP客户端登录账户后，进入校园卡充值功能，输入需要充值的金额，等待系统校验和订单提交后，APP客户端将跳转到第三方支付应用（如支付宝、微信支付等），用户在第三方支付应用完成支付后，即可充值成功。值得注意的是，由于夜间学校充值和支付平台系统维护，容易存在充值延时和失败的问题，因此目前仅在每天6：00至23：00之间提供校园卡充值服务。
    </p>
    <p>&nbsp;</p>
    <p><strong>账户管理</strong></p>
    <p style="text-indent:28px">
        Q：如何注销账户？
    </p>
    <p style="text-indent:28px">
        A：用户可以在个人中心中选择删除账户，输入账户密码进行验证，验证通过且符合注销条件则可以删除账户。若你无法自助注销账户，可以在帮助与反馈页提交故障工单或通过电子邮件联系support@gdeiassistant.cn。
    </p>
    <p style="text-indent:28px">
        Q：如何完成实名认证？
    </p>
    <p style="text-indent:28px">
        A：用户可以在个人中心中进入实名认证页面，选择任意一种实名认证方式完成认证。目前系统支持两种快捷认证方式：你可以授权应用使用你的账号信息登录学校教务系统同步实名认证信息，也可以通过绑定和验证手机号完成实名认证。若快捷认证方式不可用，可以通过上传身份证/护照等方式进行认证。除上传中华人民共和国第二代居民身份证证件照的认证方式可以自助完成认证外，目前其他证件类型（如护照、港澳居民身份证、台胞证等）的认证方式需要人工进行审核。
    </p>
</div>

</body>
</html>
