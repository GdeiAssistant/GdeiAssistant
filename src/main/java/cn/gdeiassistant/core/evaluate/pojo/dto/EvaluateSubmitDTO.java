package cn.gdeiassistant.core.evaluate.pojo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 教学评价提交入参 DTO。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EvaluateSubmitDTO implements Serializable {

    /** 是否直接提交（保存后立即提交） */
    private Boolean directSubmit;

    public Boolean getDirectSubmit() { return directSubmit; }
    public void setDirectSubmit(Boolean directSubmit) { this.directSubmit = directSubmit; }
}
