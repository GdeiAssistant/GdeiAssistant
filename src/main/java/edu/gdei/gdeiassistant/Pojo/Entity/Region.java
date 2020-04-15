package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Region implements Serializable, Entity {

    private String code;

    private String name;

    private String aliasesName;

    private String iso;

    private Map<String, State> stateMap;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, State> getStateMap() {
        return stateMap;
    }

    public void setStateMap(Map<String, State> stateMap) {
        this.stateMap = stateMap;
    }

    public String getAliasesName() {
        return aliasesName;
    }

    public void setAliasesName(String aliasesName) {
        this.aliasesName = aliasesName;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }
}
