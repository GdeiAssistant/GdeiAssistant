package com.linguancheng.gdeiassistant.Tools;

public class SchoolNewsUtils {

    /**
     * 构造新闻内容页面的PDF文件下载标签
     *
     * @param title
     * @param href
     * @return
     */
    public static String CreatePdfDownloadTag(String title, String href) {
        return "<p style=\"text-align:center;margin-top:1rem;color:#bfbfbf;padding-left:1rem;padding-right:1rem\" onclick=\"window.location.href='http://web.gdei.edu.cn" + href + "'\">点击查看：" + title + ".pdf</p>";
    }

    /**
     * 构造需要下载查看的Office文件下载标签
     *
     * @param title
     * @param href
     * @return
     */
    public static String CreateOfficeDownloadTag(String title, String href) {
        return "<p onclick=\"downloadFile('http://web.gdei.edu.cn" + href + "')\" style=\"text-align:center;margin-top:1rem;color:#bfbfbf;padding-left:1rem;padding-right:1rem\" onclick=\"window.location.href='" + href + "'\">点击查看：" + title + "." + href.split("\\.")[1] + "</p>";
    }
}
