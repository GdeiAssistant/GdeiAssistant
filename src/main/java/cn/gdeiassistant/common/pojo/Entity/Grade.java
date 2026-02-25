package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Grade implements Serializable, Entity {

    @JsonProperty("gradeYear")
    private String gradeYear;

    @JsonProperty("gradeTerm")
    private String gradeTerm;

    @JsonProperty("gradeId")
    private String gradeId;

    @JsonProperty("gradeName")
    private String gradeName;

    @JsonProperty("gradeCredit")
    private String gradeCredit;

    @JsonProperty("gradeType")
    private String gradeType;

    @JsonProperty("gradeGpa")
    private String gradeGpa;

    @JsonProperty("gradeScore")
    private String gradeScore;

    public String getGradeYear() {
        return gradeYear;
    }

    public void setGradeYear(String gradeYear) {
        this.gradeYear = gradeYear;
    }

    public String getGradeTerm() {
        return gradeTerm;
    }

    public void setGradeTerm(String gradeTerm) {
        this.gradeTerm = gradeTerm;
    }

    public String getGradeId() {
        return gradeId;
    }

    public void setGradeId(String gradeId) {
        this.gradeId = gradeId;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getGradeCredit() {
        return gradeCredit;
    }

    public void setGradeCredit(String gradeCredit) {
        this.gradeCredit = gradeCredit;
    }

    public String getGradeType() {
        return gradeType;
    }

    public void setGradeType(String gradeType) {
        this.gradeType = gradeType;
    }

    public String getGradeGpa() {
        return gradeGpa;
    }

    public void setGradeGpa(String gradeGpa) {
        this.gradeGpa = gradeGpa;
    }

    public String getGradeScore() {
        return gradeScore;
    }

    public void setGradeScore(String gradeScore) {
        this.gradeScore = gradeScore;
    }
}
