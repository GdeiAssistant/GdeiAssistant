package edu.gdei.gdeiassistant.Tools;

public class SchoolNewsUtils {

    /**
     * 构造需要下载或在线查看的文件下载标签
     *
     * @param title
     * @param href
     * @return
     */
    public static String CreateDownloadTag(String title, String href) {
        if (href.split("\\.")[1].equals("pdf")) {
            //PDF文件
            return "<p onclick=\"downloadFile('http://web.gdei.edu.cn" + href + "')\" style=\"text-align:center;margin-top:1rem;color:#bfbfbf;padding-left:1rem;padding-right:1rem\">点击查看：" + title + "." + href.split("\\.")[1] + "</p>";
        }
        return "<p onclick=\"downloadFile('http://web.gdei.edu.cn" + href + "')\" style=\"text-align:center;margin-top:1rem;color:#bfbfbf;padding-left:1rem;padding-right:1rem\">点击下载：" + title + "." + href.split("\\.")[1] + "</p>";
    }
}
