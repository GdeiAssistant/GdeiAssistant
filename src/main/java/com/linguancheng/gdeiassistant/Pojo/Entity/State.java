package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class State implements Serializable {

    private String name;

    private Map<String, City> cityMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, City> getCityMap() {
        return cityMap;
    }

    public void setCityMap(Map<String, City> cityMap) {
        this.cityMap = cityMap;
    }
}
