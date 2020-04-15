package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class State implements Serializable, Entity {

    private String code;

    private String name;

    private String aliasesName;

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

    public String getAliasesName() {
        return aliasesName;
    }

    public void setAliasesName(String aliasesName) {
        this.aliasesName = aliasesName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
