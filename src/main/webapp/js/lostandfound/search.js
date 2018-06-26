//消除iOS点击延迟
$(function () {
    FastClick.attach(document.body);
});

//选择寻物
function selectLost() {
    $(".radio1").find("img").attr("src", "/img/lostandfound/lost.png");
    $(".radio2").find("img").attr("src", "/img/lostandfound/unfound.png");
}

//选择寻主
function selectFound() {
    $(".radio1").find("img").attr("src", "/img/lostandfound/unlost.png");
    $(".radio2").find("img").attr("src", "/img/lostandfound/found.png");
}

//检查是否已选择寻物还是寻主
function checkLostTypeCheckedState() {
    if (!$("input[name='type']:checked").val()) {
        weui.alert('请选择寻物还是寻主', {
            title: '未选择查询类型',
            buttons: [{
                label: '确定',
                type: 'primary'
            }]
        });
        return false;
    }
    else if ($("input[name='keywords']").val().length > 50 || $("input[name='keywords']").val().length <= 0) {
        weui.alert('关键词限制为1到50字', {
            title: '关键词长度不合法',
            buttons: [{
                label: '确定',
                type: 'primary'
            }]
        });
        return false;
    }
}