$(function() {
    //加载中国疫情数据
    loadChinaInfectionData();
    //加载全球疫情数据
    loadGlobalInfectionData();
    //加载疫情实时动态
    loadRealtimeNews();
    //加载全球疫情地图
    loadGlobalInfectionMap();
});

//加载中国疫情数据
function loadChinaInfectionData() {
    //中国疫情数据
    $.ajax({
        url: 'https://lab.isaaclin.cn/nCoV/api/overall',
        method: 'get',
        success: function(result) {
            if (result.success) {
                //中国疫情数据更新时间
                let date = new Date(result.results[0].updateTime);
                $("#china-last-update").text("截止北京时间 " + date.toLocaleDateString() + " " + date.toLocaleTimeString());
                //变化数量
                let confirmIncrement = result.results[0].confirmedIncr;
                let currentConfirmIncrement = result.results[0].currentConfirmedIncr;
                let suspectedIncrement = result.results[0].suspectedIncr;
                let curedIncrement = result.results[0].curedIncr;
                let deadIncrement = result.results[0].deadIncr;
                let seriousIncrement = result.results[0].seriousIncr;
                if (confirmIncrement) {
                    $("#china-cumulative-confirmed-increment span").text(confirmIncrement >= 0 ? "+" + confirmIncrement : confirmIncrement);
                } else {
                    $("#china-cumulative-confirmed-increment span").text(" 待公布");
                }
                if (currentConfirmIncrement) {
                    $("#china-existing-confirmed-increment span").text(currentConfirmIncrement >= 0 ? "+" + currentConfirmIncrement : currentConfirmIncrement);
                } else {
                    $("#china-existing-confirmed-increment span").text(" 待公布");
                }
                if (suspectedIncrement) {
                    $("#china-existing-suspected-increment span").text(suspectedIncrement >= 0 ? "+" + suspectedIncrement : suspectedIncrement);
                } else {
                    $("#china-existing-suspected-increment span").text(" 待公布");
                }
                if (curedIncrement) {
                    $("#china-cured-increment span").text(curedIncrement >= 0 ? "+" + curedIncrement : curedIncrement);
                } else {
                    $("#china-cured-increment span").text(" 待公布");
                }
                if (deadIncrement) {
                    $("#china-dead-increment span").text(deadIncrement >= 0 ? "+" + deadIncrement : deadIncrement);
                } else {
                    $("#china-dead-increment span").text(" 待公布");
                }
                if (seriousIncrement) {
                    $("#china-serious-increment span").text(seriousIncrement >= 0 ? "+" + seriousIncrement : seriousIncrement);
                } else {
                    $("#china-serious-increment span").text(" 待公布");
                }
                //总数量
                let confirmedCount = result.results[0].confirmedCount;
                let currentConfirmedCount = result.results[0].currentConfirmedCount;
                let suspectedCount = result.results[0].suspectedCount;
                let curedCount = result.results[0].curedCount;
                let deadCount = result.results[0].deadCount;
                let seriousCount = result.results[0].seriousCount;
                $("#china-cumulative-confirmed-count").text(confirmedCount);
                $("#china-existing-confirmed-count").text(currentConfirmedCount);
                $("#china-existing-suspected-count").text(suspectedCount);
                $("#china-dead-count").text(deadCount);
                $("#china-cured-count").text(curedCount);
                $("#china-serious-count").text(seriousCount);
            }
        }
    });
}

//转换ISO Country Code为Unicode Flag Emoji
function convertISOCountryCodeToUnicodeFlageEmoji(code) {
    return code ? code.replace(/./g, char => String.fromCodePoint(char.charCodeAt(0) + 127397)) : "";
}

//加载全球疫情数据
function loadGlobalInfectionData() {
    $.ajax({
        url: 'https://corona.lmao.ninja/v2/all',
        method: 'get',
        success: function(result) {
            //全球疫情数据更新时间
            let date = new Date(result.updated);
            $("#world_last_update").text("截止北京时间 " + date.toLocaleDateString() + " " + date.toLocaleTimeString());
            //总数量
            let confirmedCount = result.cases;
            let deadCount = result.deaths;
            let curedCount = result.recovered;
            $("#world-confirmed-count").text(confirmedCount);
            $("#world-dead-count").text(deadCount);
            $("#world-cured-count").text(curedCount);
        }
    })
}

//加载疫情实时动态
function loadRealtimeNews() {
    $.ajax({
        url: 'https://api.tianapi.com/txapi/ncov/index?key=82f43960febef1cc639889b9140025de',
        method: 'get',
        success: function(result) {
            if (result.code == 200) {
                let data = result.newslist[0].news;
                for (let i = 0; i < data.length; i++) {
                    $("<div id='new-" + data[i].id + "' class='new'></div>").appendTo("#news");
                    $("<a class='new-title' href='" + data[i].sourceUrl + "'>" + data[i].title + "</a>").appendTo("#new-" + data[i].id);
                    $("<div class='new-date'>" + new Date(data[i].pubDate).toLocaleDateString() + " " + new Date(data[i].pubDate).toLocaleTimeString() + "</div>").appendTo("#new-" + data[i].id);
                    $("<div class='new-summary'>" + data[i].summary + "</div>").appendTo("#new-" + data[i].id);
                }
            }
        }
    })
}

//加载全球疫情地图
function loadGlobalInfectionMap() {
    $.ajax({
        url: 'https://corona.lmao.ninja/countries',
        method: 'get',
        success: function(result) {
            let datas = [
                ['Domain', 'Country', '确诊病例数', '每100万人中的病例数']
            ];
            for (let i = 0; i < result.length; i++) {
                let iso = result[i].countryInfo.iso2;
                let country = json[iso];
                let cases = result[i].cases;
                let casesPerOneMillion = result[i].casesPerOneMillion;
                let data = [iso, convertISOCountryCodeToUnicodeFlageEmoji(iso) + country, cases, casesPerOneMillion];
                // let data = [iso,country,cases];
                datas.push(data);
            }
            // Load the Visualization API and the corechart package.
            google.charts.load('current', {
                'packages': ['geochart'],
                'mapsApiKey': 'AIzaSyDwCueKrWO0ypWe27OwfU4N30xu-KSqOFE'
            });

            // Set a callback to run when the Google Visualization API is loaded.
            google.charts.setOnLoadCallback(drawRegionsMap);

            function drawRegionsMap() {

                var data = google.visualization.arrayToDataTable(datas);

                var options = {
                    colorAxis: {
                        minValue: 0,
                        colors: ['D2E3FC', '8AB4F8', '4285F4', '1967D2', '174EA6']
                    },
                };

                var chart = new google.visualization.GeoChart(document.getElementById('map'));

                chart.draw(data, options);
            }
        }
    })
}