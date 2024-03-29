package cn.gdeiassistant.Pojo.Redirect;

public class RedirectInfo {

    private String redirect_url;

    public String getRedirect_url() {
        return redirect_url;
    }

    public void setRedirect_url(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    public RedirectInfo() {
        super();
    }

    public RedirectInfo(String redirect_url) {
        this.redirect_url = redirect_url;
    }

    /**
     * 是否需要进行URL重定向
     *
     * @return
     */
    public boolean needToRedirect() {
        return redirect_url != null && !redirect_url.trim().isEmpty();
    }
}
