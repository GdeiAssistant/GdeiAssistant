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
        return "<p style=\"margin:0pt;line-height:150%;text-indent:0pt;-ms-text-autospace:ideograph-numeric;" +
                "mso-para-margin-right:0.0000gd;mso-para-margin-left:0.0000gd;mso-char-indent-count:0.0000;" +
                "mso-pagination:none;\"><span style=\"color:#000000;line-height:150%;font-family:宋体;" +
                "font-size:12pt;mso-spacerun:\"yes\";mso-font-kerning:1.0000pt;\">" +
                "<a href=\"http://web.gdei.edu.cn/" + href + "\" target=\"_blank\">" +
                title + ".pdf</a>&nbsp;</span></p>";
    }
}
