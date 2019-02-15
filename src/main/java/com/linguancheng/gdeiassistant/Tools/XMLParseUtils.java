package com.linguancheng.gdeiassistant.Tools;

import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatArticle;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatImageTextMessage;
import com.linguancheng.gdeiassistant.Pojo.Wechat.WechatTextMessage;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class XMLParseUtils {

    /**
     * 解析请求的XML数据转换为Map
     *
     * @param request
     * @return
     */
    public static Map<String, String> ParseRequestXMLToMap(HttpServletRequest request) {
        Map<String, String> result = new HashMap<>();
        try (InputStream inputStream = request.getInputStream()) {
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(inputStream);
            Element rootElement = document.getRootElement();
            List<Element> elementList = rootElement.elements();
            for (Element element : elementList) {
                result.put(element.getName(), Pattern.compile("\\s*|\t|\r|\n").matcher(element.getText()).replaceAll("").trim());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解析微信文本信息转换为XML
     *
     * @param wechatTextMessage
     * @return
     */
    public static String ParseWechatTextMessageToXML(WechatTextMessage wechatTextMessage) {
        xStream.alias("xml", wechatTextMessage.getClass());
        return xStream.toXML(wechatTextMessage);
    }

    /**
     * 解析微信图文信息转换为XML
     *
     * @param wechatImageTextMessage
     * @return
     */
    public static String ParseWechatImageTextMessageToXML(WechatImageTextMessage wechatImageTextMessage) {
        xStream.alias("xml", wechatImageTextMessage.getClass());
        xStream.alias("item", WechatArticle.class);
        return xStream.toXML(wechatImageTextMessage);
    }

    /**
     * 扩展xStream，使其支持CDATA块
     */
    private static XStream xStream = new XStream(new XppDriver() {
        public HierarchicalStreamWriter createWriter(Writer out) {
            return new PrettyPrintWriter(out) {
                // 对所有xml节点的转换都增加CDATA标记
                boolean cdata = true;

                @SuppressWarnings("unchecked")
                public void startNode(String name, Class clazz) {
                    super.startNode(name, clazz);
                }

                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write("<![CDATA[");
                        writer.write(text);
                        writer.write("]]>");
                    } else {
                        writer.write(text);
                    }
                }
            };
        }
    });
}
