<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script>

    //消除iOS点击延迟
    $(function () {
        FastClick.attach(document.body);
    });

    $(function () {
        (function () {
            App.AuthDialog.show({
                client_id: '${ClientID}',
                redirect_uri: '${RedirectURL}'
            });
        })();
    });

</script>