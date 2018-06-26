<%--
  Created by IntelliJ IDEA.
  User: linguancheng
  Date: 2017/11/18
  Time: 00:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>易班帐号授权</title>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/authiframe.js"></script>
    <script type="application/javascript" src="/js/common/fastclick.js"></script>
    <script>
        //消除iOS点击延迟
        $(function () {
            FastClick.attach(document.body);
        });
    </script>
</head>
<body>

<script type="text/javascript">
    $(function () {
        (function () {
            App.AuthDialog.show({
                client_id: '${ClientID}',
                redirect_uri: '${RedirectURL}'
            });
        })();
    });
</script>

</body>
</html>
