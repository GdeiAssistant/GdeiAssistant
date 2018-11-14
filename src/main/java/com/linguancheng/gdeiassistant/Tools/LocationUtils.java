package com.linguancheng.gdeiassistant.Tools;

import com.linguancheng.gdeiassistant.Pojo.Entity.City;
import com.linguancheng.gdeiassistant.Pojo.Entity.Region;
import com.linguancheng.gdeiassistant.Pojo.Entity.State;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationUtils {

    private static Map<String, Region> regionMap;

    public static Map<String, Region> getRegionMap() {
        return regionMap;
    }

    static {
        //加载地区信息
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
            Region region = new Region();
            region.setName(regionName);
            region.setCode(regionCode);
            Map<String, State> stateMap = new HashMap<>();
            List<Element> stateElements = regionElement.elements();
            for (Element stateElement : stateElements) {
                String stateName = stateElement.attribute("Name").getValue();
                String stateCode = stateElement.attribute("Code").getValue();
                State state = new State();
                state.setName(stateName);
                Map<String, City> cityMap = new HashMap<>();
                List<Element> cityElements = stateElement.elements();
                for (Element cityElement : cityElements) {
                    String cityName = cityElement.attribute("Name").getValue();
                    String cityCode = cityElement.attribute("Code").getValue();
                    City city = new City();
                    city.setName(cityName);
                    cityMap.put(cityCode, city);
                }
                state.setCityMap(cityMap);
                stateMap.put(stateCode, state);
            }
            region.setStateMap(stateMap);
            LocationUtils.regionMap.put(regionCode, region);
        }
    }
}
