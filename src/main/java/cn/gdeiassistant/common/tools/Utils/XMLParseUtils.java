package cn.gdeiassistant.common.tools.Utils;

import cn.gdeiassistant.common.pojo.Entity.RSSNewInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class XMLParseUtils {

    private static final List<DateTimeFormatter> RSS_DATE_FORMATTERS = List.of(
            DateTimeFormatter.RFC_1123_DATE_TIME,
            DateTimeFormatter.ofPattern("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.ENGLISH),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
    );

    private XMLParseUtils() {
    }

    public static List<RSSNewInfo> parseRssNewsXml(String xml) {
        if (StringUtils.isBlank(xml)) {
            return new ArrayList<>();
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setExpandEntityReferences(false);

            DocumentBuilder builder = factory.newDocumentBuilder();
            org.w3c.dom.Document document = builder.parse(new InputSource(new StringReader(xml)));
            NodeList itemNodes = document.getElementsByTagName("item");
            List<RSSNewInfo> result = new ArrayList<>(itemNodes.getLength());
            for (int index = 0; index < itemNodes.getLength(); index++) {
                org.w3c.dom.Element item = (org.w3c.dom.Element) itemNodes.item(index);
                RSSNewInfo info = new RSSNewInfo();
                info.setTitle(nodeText(item, "title"));
                info.setLink(nodeText(item, "link"));
                info.setDescription(nodeText(item, "description"));
                info.setAuthor(firstNonBlank(nodeText(item, "author"), nodeText(item, "dc:creator")));
                info.setPublishDate(parsePublishDate(firstNonBlank(
                        nodeText(item, "pubDate"),
                        nodeText(item, "published"),
                        nodeText(item, "updated")
                )));
                info.setTitleImage(resolveTitleImage(item, info.getDescription()));
                result.add(info);
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private static String nodeText(org.w3c.dom.Element element, String tagName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        String value = nodes.item(0).getTextContent();
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static String resolveTitleImage(org.w3c.dom.Element item, String description) {
        String imageUrl = attributeValue(item, "enclosure", "url");
        if (StringUtils.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        imageUrl = attributeValue(item, "media:content", "url");
        if (StringUtils.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        imageUrl = attributeValue(item, "media:thumbnail", "url");
        if (StringUtils.isNotBlank(imageUrl)) {
            return imageUrl;
        }
        if (StringUtils.isBlank(description)) {
            return null;
        }
        Document html = Jsoup.parse(description, "", Parser.xmlParser());
        Element image = html.selectFirst("img[src]");
        return image == null ? null : image.attr("src");
    }

    private static String attributeValue(org.w3c.dom.Element element, String tagName, String attributeName) {
        NodeList nodes = element.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) {
            return null;
        }
        org.w3c.dom.Node node = nodes.item(0);
        if (!(node instanceof org.w3c.dom.Element child)) {
            return null;
        }
        String value = child.getAttribute(attributeName);
        return StringUtils.isBlank(value) ? null : value.trim();
    }

    private static Date parsePublishDate(String rawValue) {
        if (StringUtils.isBlank(rawValue)) {
            return null;
        }
        for (DateTimeFormatter formatter : RSS_DATE_FORMATTERS) {
            try {
                if (formatter == DateTimeFormatter.RFC_1123_DATE_TIME) {
                    ZonedDateTime zonedDateTime = ZonedDateTime.parse(rawValue, formatter);
                    return Date.from(zonedDateTime.toInstant());
                }
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(rawValue, formatter);
                return Date.from(offsetDateTime.atZoneSameInstant(ZoneId.systemDefault()).toInstant());
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
