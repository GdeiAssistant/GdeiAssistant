package cn.gdeiassistant.common.tools.Utils;

import cn.gdeiassistant.common.pojo.Entity.Festival;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.text.SimpleDateFormat;
import java.util.*;

public class FestivalUtils {

    private static final Logger logger = LoggerFactory.getLogger(FestivalUtils.class);

    private static Map<String, Festival> festivalMap;

    /**
     * 获取节日信息
     *
     * @return
     */
    public static Festival getFestivalInfo() {
        return festivalMap.get(new SimpleDateFormat("MM-dd").format(new Date()));
    }

    static {
        //加载节日信息
        Resource resource = new ClassPathResource("festival.xml");
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(resource.getInputStream());
        } catch (Exception e) {
            logger.error("加载节日配置文件 festival.xml 失败", e);
        }
        Element root = document.getRootElement();
        List<Element> festivalElements = root.elements();
        FestivalUtils.festivalMap = new HashMap<>();
        for (Element festivalElement : festivalElements) {
            String date = festivalElement.attribute("Date").getValue();
            String name = festivalElement.attribute("Name").getValue();
            List<Element> descriptionList = festivalElement.elements();
            List<String> descriptions = new ArrayList<>();
            for (Element description : descriptionList) {
                descriptions.add(description.getStringValue());
            }
            Festival festival = new Festival();
            festival.setName(name);
            festival.setDescription(descriptions);
            festivalMap.put(date, festival);
        }
    }
}
