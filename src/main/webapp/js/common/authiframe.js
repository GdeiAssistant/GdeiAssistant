(function () {
    var h = (function () {
        var q = window.localStorage,
            r, s;
        if (q) {
            return {
                get: function (u) {
                    return unescape(q.getItem(u))
                },
                set: function (u, v) {
                    q.setItem(u, escape(v))
                }
            }
        } else {
            if (window.ActiveXObject) {
                r = document.documentElement;
                s = "localstorage";
                try {
                    r.addBehavior("#default#userdata");
                    r.save("localstorage")
                } catch (t) {
                }
                return {
                    set: function (u, v) {
                        try {
                            r.setAttribute(u, v);
                            r.save(s)
                        } catch (w) {
                        }
                    },
                    get: function (u) {
                        try {
                            r.load(s);
                            return r.getAttribute(u)
                        } catch (v) {
                        }
                    }
                }
            } else {
                return {
                    get: getCookie,
                    set: setCookie
                }
            }
        }
    })();
    var j = navigator.userAgent,
        d = /msie/i.test(j);
    var p = function () {
    };

    function o(s, r, q) {
        var t, u = {};
        r = r || {};
        for (t in s) {
            u[t] = s[t];
            if (r[t] != null) {
                if (q) {
                    if (s.hasOwnProperty[t]) {
                        u[t] = r[t]
                    }
                } else {
                    u[t] = r[t]
                }
            }
        }
        return u
    }

    function b(q) {
        q = document.getElementById(q) || q;
        try {
            q.parentNode.removeChild(q)
        } catch (r) {
        }
    }

    function m(r) {
        if (r) {
            var q = [];
            for (var s in r) {
                q.push(s + "=" + encodeURIComponent(r[s]))
            }
            if (q.length) {
                return q.join("&")
            } else {
                return ""
            }
        }
    }

    function i(y, w) {
        var u = {};
        var v = {
            url: "",
            charset: "UTF-8",
            timeout: 30 * 1000,
            args: {},
            onComplete: p,
            onTimeout: p,
            isEncode: false,
            uniqueID: null
        };
        var t, s;
        var r = o(v, y);
        if (r.url == "") {
            throw new Error("scriptLoader: url is null")
        }
        var q = r.uniqueID || (new Date().getTime().toString());
        t = u[q];
        if (t != null && d != true) {
            b(t);
            t = null
        }
        if (t == null) {
            u[q] = document.createElement("script");
            t = u[q]
        }
        t.charset = r.charset;
        t.type = "text/javascript";
        if (r.onComplete != null) {
            if (d) {
                t.onreadystatechange = function () {
                    if (t.readyState.toLowerCase() == "loaded" || t.readyState.toLowerCase() == "complete") {
                        try {
                            clearTimeout(s);
                            document.getElementsByTagName("head")[0].removeChild(t);
                            t.onreadystatechange = null
                        } catch (z) {
                        }
                        r.onComplete()
                    }
                }
            } else {
                t.onload = function () {
                    try {
                        clearTimeout(s);
                        b(t);
                        t.onload = null
                    } catch (z) {
                    }
                    r.onComplete()
                }
            }
        }
        var x = m(r.args);
        if (r.url.indexOf("?") == -1) {
            if (x != "") {
                x = "?" + x
            }
        } else {
            if (x != "") {
                x = "&" + x
            }
        }
        t.src = r.url + x;
        document.getElementsByTagName("head")[0].appendChild(t);
        if (r.timeout > 0 && r.onTimeout != null) {
            s = setTimeout(function () {
                    try {
                        document.getElementsByTagName("head")[0].removeChild(t)
                    } catch (z) {
                    }
                    r.onTimeout()
                },
                r.timeout)
        }
        return t
    }

    function n() {
        i({
            url: "http://f.yiban.cn/apps/js/version.js?" + (new Date()).getTime(),
            onComplete: function () {
                var q = new Date();
                g = q.getFullYear() + "/" + (q.getMonth() + 1) + "/" + q.getDate() + ":";
                try {
                    g = g + $CONFIG.version.client.split(":")[1]
                } catch (r) {
                    g = g + q.getTime()
                }
                l(g);
                h.set("client", g)
            }
        })
    }

    function l(q) {
        var t = q.split(":");
        var s = new Date();
        var r = s.getFullYear() + "/" + (s.getMonth() + 1) + "/" + s.getDate();
        if (r != t[0]) {
            n()
        } else {
            f = t[1];
            e()
        }
    }

    var c;
    var a;
    var k = false;
    var g = h.get("client"),
        f;
    if (g == null || g == "null" || g.indexOf(":") == -1) {
        n()
    } else {
        l(g)
    }

    function e() {
        i({
            url: "http://f.yiban.cn/apps/js/authapp.js?" + f,
            onComplete: function () {
                if (c != null) {
                    App.AuthDialog.show(c)
                }
                if (a && isNaN(a) == false) {
                    App.setPageHeight(a)
                }
                if (k != false) {
                    App.scrollToTop(k)
                }
            }
        })
    }

    if (window.App == null) {
        App = {
            AuthDialog: {
                show: function (q) {
                    c = q
                }
            },
            setPageHeight: function (q) {
                if (q == null) {
                    a = document.body.clientHeight + 40
                } else {
                    if (!isNaN(q)) {
                        a = q
                    }
                }
            },
            scrollToTop: function (q) {
                k = q || true
            }
        }
    }
})();