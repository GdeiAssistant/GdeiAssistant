$(function () {
    let theme = localStorage.getItem("theme");
    if (theme) {
        let links = document.querySelectorAll('link[title]');
        [].slice.call(links).forEach(function (link) {
            link.disabled = false;
        });
        [].slice.call(links).forEach(function (link) {
            link.disabled = true;
        });
        [].slice.call(links).forEach(function (link) {
            if (link.getAttribute("title") == theme) {
                link.disabled = false;
            }
        });
    }
    document.getElementsByTagName("html")[0].style.visibility = "visible";
});