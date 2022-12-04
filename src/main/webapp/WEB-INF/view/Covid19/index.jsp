<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>新型冠状病毒肺炎疫情实时动态</title>
    <meta charset="UTF-8">
    <meta content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0" name="viewport">
    <meta content="yes" name="apple-mobile-web-app-capable">
    <meta content="black" name="apple-mobile-web-app-status-bar-style">
    <meta content="telephone=no" name="format-detection">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <!-- 如果使用双核浏览器，强制使用webkit来进行页面渲染 -->
    <meta name="renderer" content="webkit" />
    <meta name="viewport" content="width=device-width,initial-scale=1,user-scalable=0">
    <link rel="icon" type="image/png" sizes="192x192" href="/img/covid19/logo.png">
    <link rel="shortcut icon" type="image/png" sizes="64x64" href="/img/covid19/logo.png">
    <link rel="stylesheet" type="text/css" href="/css/common/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="/css/covid19/index.css">
    <script type="text/javascript" src="/json/covid19/country.json"></script>
    <script type="text/javascript" src="/js/common/jquery-3.2.1.min.js"></script>
    <script type="text/javascript" src="/js/common/loader.js"></script>
    <script type="text/javascript" src="/js/covid19/index.js"></script>
</head>

<body>

<img width="100%" height="30%" src="/img/covid19/covid-2019.jpg">

<p id="china-tip" class="tip"><i></i>中国疫情数据</p>

<div id="china-data" class="data" style="margin: 0 auto">
    <small id="china-last-update" class="text-muted"></small>
    <table class="table ">
        <thead>
        <tr>
            <th>
                <small><span id="china-cumulative-confirmed-increment" class="text-muted">较昨日<span
                        class="text-danger"></span></span></small><br>
                <big id="china-cumulative-confirmed-count" class="text-danger">-</big>
                <p>累计确诊</p>
            </th>
            <th><small><span id="china-existing-confirmed-increment" class="text-muted">较昨日<span
                    class="text-info"></span></span></small><br>
                <big id="china-existing-confirmed-count" class="text-info">-</big>
                <p>现存确诊</p>
            </th>
            <th>
                <small><span id="china-existing-suspected-increment" class="text-muted">较昨日<span
                        class="text-warning"></span></span></small><br>
                <big id="china-existing-suspected-count" class="text-warning">-</big>
                <p>现存疑似</p>
            </th>
        </tr>
        <tr>
            <th><small><span id="china-dead-increment" class="text-muted">较昨日<span
                    class="text-primary"></span></span></small><br>
                <big id="china-dead-count" class="text-primary">-</big>
                <p>死亡</p>
            </th>
            <th><small><span id="china-cured-increment" class="text-muted">较昨日<span class="text-success"></span></span></small><br>
                <big id="china-cured-count" class="text-success">-</big>
                <p>治愈</p>
            </th>
            <th><small><span id="china-serious-increment" class="text-muted">较昨日<span class="text-capitalize"></span></span></small><br>
                <big id="china-serious-count" class="text-capitalize">-</big>
                <p>现存重症</p>
            </th>
        </tr>
        </thead>
    </table>
</div>

<p id="world-tip" class="tip"><i></i>全球疫情数据</p>

<div id="world-data" class="data" style="margin: 0 auto">
    <small id="world_last_update" class="text-muted"></small>
    <table class="table ">
        <thead>
        <tr>
            <th>
                <big id="world-confirmed-count" class="text-warning">-</big>
                <p>累计确诊</p>
            </th>
            <th>
                <big id="world-dead-count" class="text-primary">-</big>
                <p>累计死亡</p>
            </th>
            <th>
                <big id="world-cured-count" class="text-info">-</big>
                <p>累计治愈</p>
            </th>
        </tr>
        </thead>
    </table>
</div>

<p id="global_map" class="tip"><i></i>全球疫情地图</p>

<div id="map" style="width: 100%; height: auto;"></div>

<p id="global_map" class="tip"><i></i>疫情实时动态</p>

<div id="news">

</div>

<p id="service" class="tip"><i></i>公益抗疫服务</p>

<div class="service-item">
    <div class="service-item-box" onclick="window.location.href='https://ncov.html5.qq.com/communityRenmin'">
        <img class="service-item-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFgAAABYCAYAAABxlTA0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAGmGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDggNzkuMTY0MDM2LCAyMDE5LzA4LzEzLTAxOjA2OjU3ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RSZWY9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZVJlZiMiIHhtbG5zOnN0RXZ0PSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VFdmVudCMiIHhtbG5zOnhtcD0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wLyIgeG1sbnM6ZGM9Imh0dHA6Ly9wdXJsLm9yZy9kYy9lbGVtZW50cy8xLjEvIiB4bWxuczpwaG90b3Nob3A9Imh0dHA6Ly9ucy5hZG9iZS5jb20vcGhvdG9zaG9wLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo1NTZmNzlmNC0yM2M3LTRlNDctYmZhMi00ZWY3NmM2ZmEyMWUiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6ODlGNDRFMjM0RDlBMTFFQTlDMkNDQkFEOUEyQTcyMjAiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6NDg4YWFjY2ItM2FjMi00M2VjLThkY2MtNWIwNmRiYjc3YjQ5IiB4bXA6Q3JlYXRvclRvb2w9IkFkb2JlIFBob3Rvc2hvcCBDQyAyMDE5IChNYWNpbnRvc2gpIiB4bXA6Q3JlYXRlRGF0ZT0iMjAyMC0wMi0yNVQxNToxMDoxMCswODowMCIgeG1wOk1vZGlmeURhdGU9IjIwMjAtMDItMjVUMTU6Mjk6MTQrMDg6MDAiIHhtcDpNZXRhZGF0YURhdGU9IjIwMjAtMDItMjVUMTU6Mjk6MTQrMDg6MDAiIGRjOmZvcm1hdD0iaW1hZ2UvcG5nIiBwaG90b3Nob3A6Q29sb3JNb2RlPSIzIiBwaG90b3Nob3A6SUNDUHJvZmlsZT0ic1JHQiBJRUM2MTk2Ni0yLjEiIHBob3Rvc2hvcDpIaXN0b3J5PSIyMDIwLTAyLTI1VDE1OjI5OjA3KzA4OjAwJiN4OTvmlofku7Yg55ar5oOF5Zyw5Zu+LnBuZyDlt7LmiZPlvIAmI3hBOzIwMjAtMDItMjVUMTU6Mjk6MTQrMDg6MDAmI3g5O+aWh+S7tiDnlqvmg4XlnLDlm74ucG5nIOW3suWtmOWCqCYjeEE7Ij4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6YmI4MmQ4ZjEtYTk3MS00YzAzLTg4YWUtMGY3NDg1N2QwMDMwIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjU1NmY3OWY0LTIzYzctNGU0Ny1iZmEyLTRlZjc2YzZmYTIxZSIvPiA8eG1wTU06SGlzdG9yeT4gPHJkZjpTZXE+IDxyZGY6bGkgc3RFdnQ6YWN0aW9uPSJzYXZlZCIgc3RFdnQ6aW5zdGFuY2VJRD0ieG1wLmlpZDo0ODhhYWNjYi0zYWMyLTQzZWMtOGRjYy01YjA2ZGJiNzdiNDkiIHN0RXZ0OndoZW49IjIwMjAtMDItMjVUMTU6Mjk6MTQrMDg6MDAiIHN0RXZ0OnNvZnR3YXJlQWdlbnQ9IkFkb2JlIFBob3Rvc2hvcCAyMS4wIChNYWNpbnRvc2gpIiBzdEV2dDpjaGFuZ2VkPSIvIi8+IDwvcmRmOlNlcT4gPC94bXBNTTpIaXN0b3J5PiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Pilx7wMAABQXSURBVHic7Zx5jBxZfcc/r6rv7jk8p6+Zscc3PtY2XmA3azaEYwkEkQQE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">
        <div>疫情地图</div>
    </div>
    <div class="service-item-box" onclick="window.location.href='https://ask.dxy.com/index#/explore/album/doctor'">
        <img class="service-item-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFgAAABYCAYAAABxlTA0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFqGlUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDggNzkuMTY0MDM2LCAyMDE5LzA4LzEzLTAxOjA2OjU3ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgMjEuMCAoTWFjaW50b3NoKSIgeG1wOkNyZWF0ZURhdGU9IjIwMjAtMDItMTNUMDY6MDQ6NDQrMDg6MDAiIHhtcDpNb2RpZnlEYXRlPSIyMDIwLTAyLTEzVDE0OjA3OjA1KzA4OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDIwLTAyLTEzVDE0OjA3OjA1KzA4OjAwIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9InNSR0IgSUVDNjE5NjYtMi4xIiBwaG90b3Nob3A6SGlzdG9yeT0iMjAyMC0wMi0xM1QxNDowNyswODow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">
        <div>在线义诊</div>
    </div>
    <div class="service-item-box" onclick="window.location.href='http://2019ncov.nosugartech.com'">
        <img class="service-item-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFgAAABYCAYAAABxlTA0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFq2lUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDggNzkuMTY0MDM2LCAyMDE5LzA4LzEzLTAxOjA2OjU3ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgMjEuMCAoTWFjaW50b3NoKSIgeG1wOkNyZWF0ZURhdGU9IjIwMjAtMDItMTNUMDY6MDQ6NDQrMDg6MDAiIHhtcDpNb2RpZnlEYXRlPSIyMDIwLTAyLTEzVDE0OjA2OjU1KzA4OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDIwLTAyLTEzVDE0OjA2OjU1KzA4OjAwIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9InNSR0IgSUVDNjE5NjYtMi4xIiBwaG90b3Nob3A6SGlzdG9yeT0iMjAyMC0wMi0xM1QxNDowNjo1MCsw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">
        <div>同乘查询</div>
    </div>
    <div class="service-item-box" onclick="window.location.href='https://ncov.dxy.cn/ncovh5/view/pneumonia_rumors'">
        <img class="service-item-icon" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAFgAAABYCAYAAABxlTA0AAAACXBIWXMAAAsTAAALEwEAmpwYAAAFq2lUWHRYTUw6Y29tLmFkb2JlLnhtcAAAAAAAPD94cGFja2V0IGJlZ2luPSLvu78iIGlkPSJXNU0wTXBDZWhpSHpyZVN6TlRjemtjOWQiPz4gPHg6eG1wbWV0YSB4bWxuczp4PSJhZG9iZTpuczptZXRhLyIgeDp4bXB0az0iQWRvYmUgWE1QIENvcmUgNS42LWMxNDggNzkuMTY0MDM2LCAyMDE5LzA4LzEzLTAxOjA2OjU3ICAgICAgICAiPiA8cmRmOlJERiB4bWxuczpyZGY9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkvMDIvMjItcmRmLXN5bnRheC1ucyMiPiA8cmRmOkRlc2NyaXB0aW9uIHJkZjphYm91dD0iIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtbG5zOmRjPSJodHRwOi8vcHVybC5vcmcvZGMvZWxlbWVudHMvMS4xLyIgeG1sbnM6cGhvdG9zaG9wPSJodHRwOi8vbnMuYWRvYmUuY29tL3Bob3Rvc2hvcC8xLjAvIiB4bWxuczp4bXBNTT0iaHR0cDovL25zLmFkb2JlLmNvbS94YXAvMS4wL21tLyIgeG1sbnM6c3RFdnQ9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9zVHlwZS9SZXNvdXJjZUV2ZW50IyIgeG1wOkNyZWF0b3JUb29sPSJBZG9iZSBQaG90b3Nob3AgMjEuMCAoTWFjaW50b3NoKSIgeG1wOkNyZWF0ZURhdGU9IjIwMjAtMDItMTNUMDY6MDQ6NDQrMDg6MDAiIHhtcDpNb2RpZnlEYXRlPSIyMDIwLTAyLTEzVDE0OjA2OjM4KzA4OjAwIiB4bXA6TWV0YWRhdGFEYXRlPSIyMDIwLTAyLTEzVDE0OjA2OjM4KzA4OjAwIiBkYzpmb3JtYXQ9ImltYWdlL3BuZyIgcGhvdG9zaG9wOkNvbG9yTW9kZT0iMyIgcGhvdG9zaG9wOklDQ1Byb2ZpbGU9InNSR0IgSUVDNjE5NjYtMi4xIiBwaG90b3Nob3A6SGlzdG9yeT0iMjAyMC0wMi0xM1QxNDowNjozMysw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">
        <div>疫情辟谣</div>
    </div>
</div>

<p id="website" class="tip"><i></i>相关资讯网站</p>

<div>
    <div class="info" onclick="window.location.href='https://www.cdc.gov/coronavirus/2019-ncov/index-Chinese.html'">
        <div class="info-right">
            <div class="info-image">
                <img src="/img/covid19/cdc-us.jpg" style="border-radius: 0">
            </div>
        </div>
        <div class="info-left">
            <div class="info-text">美国疾病控制与预防中心</div>
            <div class="info-description">查看来自美国疾病管制与预防中心的最新资讯，协助预防2019冠状病毒病（COVID-19）传播</div>
        </div>
    </div>
    <div class="info" onclick="window.location.href='https://www.who.int/zh/emergencies/diseases/novel-coronavirus-2019'">
        <div class="info-right">
            <div class="info-image">
                <img src="/img/covid19/who.jpg" style="border-radius: 0">
            </div>
        </div>
        <div class="info-left">
            <div class="info-text">世界卫生组织</div>
            <div class="info-description">查阅世卫组织关于2019冠状病毒病（COVID-19）当前疫情的信息和指导意见，了解每日最新情况</div>
        </div>
    </div>
    <div class="info" onclick="window.location.href='http://www.chinacdc.cn/jkzt/crb/zl/szkb_11803'">
        <div class="info-right">
            <div class="info-image">
                <img src="/img/covid19/cdc-cn.jpg" style="border-radius: 0">
            </div>
        </div>
        <div class="info-left">
            <div class="info-text">中国疾病预防控制中心</div>
            <div class="info-description">通过中国疾病预防控制中心的资讯，了解最新动态和如何采取措施保护健康并防止疫情蔓延</div>
        </div>
    </div>
    <div class="info" onclick="window.location.href='https://ncov.dxy.cn/ncovh5/view/pneumonia'">
        <div class="info-right">
            <div class="info-image">
                <img src="/img/covid19/dxy.jpg" style="border-radius: 0">
            </div>
        </div>
        <div class="info-left">
            <div class="info-text">丁香园</div>
            <div class="info-description">整合各权威渠道发布的官方数据，持续更新最新的新型冠状病毒肺炎的实时疫情动态和疫情地图直观</div>
        </div>
    </div>
</div>

<small class="text-muted">国内疫情数据来源于医学知识分享网站丁香园</small>
<small class="text-muted">全球疫情数据来源于Dadax旗下网站Worldometer</small>
<small class="text-muted"></small>

</body>

</html>