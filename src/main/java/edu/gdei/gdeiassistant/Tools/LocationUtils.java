package edu.gdei.gdeiassistant.Tools;

import edu.gdei.gdeiassistant.Pojo.Entity.Attribution;
import edu.gdei.gdeiassistant.Pojo.Entity.City;
import edu.gdei.gdeiassistant.Pojo.Entity.Region;
import edu.gdei.gdeiassistant.Pojo.Entity.State;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationUtils {

    private static Map<String, Region> regionMap;

    public static Map<String, Region> getRegionMap() {
        return regionMap;
    }

    private static Map<Integer, Attribution> attributionMap;

    static {
        //加载地区信息
        LoadLocationData();
        //加载手机号归属地信息
        LoadPhoneAttributionData();
    }

    public static Map<Integer, Attribution> getAttributionMap() {
        return attributionMap;
    }

    /**
     * 转换ISO 3166-1 alpha-2代码为国家/地区Flag Emoji
     * @param code
     * @return
     */
    public static String convertCountryCodeToEmoji(String code){

        // offset between uppercase ascii and regional indicator symbols
        int OFFSET = 127397;

        // validate code
        if(code == null || code.length() != 2) {
            return "";
        }

        //fix for uk -> gb
        if (code.equalsIgnoreCase("uk")) {
            code = "gb";
        }

        // convert code to uppercase
        code = code.toUpperCase();

        StringBuilder emojiStr = new StringBuilder();

        //loop all characters
        for (int i = 0; i < code.length(); i++) {
            emojiStr.appendCodePoint(code.charAt(i) + OFFSET);
        }

        // return emoji
        return emojiStr.toString();
    }

    private static void LoadLocationData() {
        Resource resource = new ClassPathResource("/config/location/location.xml");
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(resource.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        List<Element> regionElements = root.elements();
        LocationUtils.regionMap = new HashMap<>();
        for (Element regionElement : regionElements) {
            String regionName = regionElement.attribute("Name").getValue();
            String regionCode = regionElement.attribute("Code").getValue();
            String regionISO = regionElement.attribute("ISO").getValue();
            String regionAliasesName = regionElement.attribute("AliasesName") == null ?
                    null : regionElement.attribute("AliasesName").getValue();
            Region region = new Region();
            region.setName(regionName);
            region.setCode(regionCode);
            region.setIso(regionISO);
            region.setAliasesName(regionAliasesName);
            Map<String, State> stateMap = new HashMap<>();
            List<Element> stateElements = regionElement.elements();
            for (Element stateElement : stateElements) {
                String stateName = stateElement.attribute("Name").getValue();
                String stateCode = stateElement.attribute("Code").getValue();
                String stateAliasesName = stateElement.attribute("AliasesName") == null ?
                        null : stateElement.attribute("AliasesName").getValue();
                State state = new State();
                state.setCode(stateCode);
                state.setName(stateName);
                state.setAliasesName(stateAliasesName);
                Map<String, City> cityMap = new HashMap<>();
                List<Element> cityElements = stateElement.elements();
                for (Element cityElement : cityElements) {
                    String cityName = cityElement.attribute("Name").getValue();
                    String cityCode = cityElement.attribute("Code").getValue();
                    String cityAliasesName = cityElement.attribute("AliasesName") == null ?
                            null : cityElement.attribute("AliasesName").getValue();
                    City city = new City();
                    city.setCode(cityCode);
                    city.setName(cityName);
                    city.setAliasesName(cityAliasesName);
                    cityMap.put(cityCode, city);
                }
                state.setCityMap(cityMap);
                stateMap.put(stateCode, state);
            }
            region.setStateMap(stateMap);
            LocationUtils.regionMap.put(regionCode, region);
        }
    }

    private static void LoadPhoneAttributionData() {
        Resource resource = new ClassPathResource("/config/location/phone.xml");
        SAXReader saxReader = new SAXReader();
        Document document = null;
        try {
            document = saxReader.read(resource.getInputStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        List<Element> attributionElements = root.elements();
        LocationUtils.attributionMap = new HashMap<>();
        for (Element attributionElement : attributionElements) {
            String name = attributionElement.attribute("Name").getValue();
            Integer code = Integer.valueOf(attributionElement.attribute("Code").getValue());
            String flag = attributionElement.attribute("Flag").getValue();
            Attribution attribution = new Attribution();
            attribution.setCode(code);
            attribution.setFlag(flag);
            attribution.setName(name);
            LocationUtils.attributionMap.put(code, attribution);
        }
    }
}
