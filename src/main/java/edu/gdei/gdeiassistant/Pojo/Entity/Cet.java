package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Cet implements Serializable {

    private String name;
    private String school;
    private String type;
    private String admissionCard;
    private String totalScore;
    private String listeningScore;
    private String readingScore;
    private String writingAndTranslatingScore;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSchool() {
        return school;
    }

    public void setSchool(String school) {
        this.school = school;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAdmissionCard() {
        return admissionCard;
    }

    public void setAdmissionCard(String admissionCard) {
        this.admissionCard = admissionCard;
    }

    public String getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(String totalScore) {
        this.totalScore = totalScore;
    }

    public String getListeningScore() {
        return listeningScore;
    }

    public void setListeningScore(String listeningScore) {
        this.listeningScore = listeningScore;
    }

    public String getReadingScore() {
        return readingScore;
    }

    public void setReadingScore(String readingScore) {
        this.readingScore = readingScore;
    }

    public String getWritingAndTranslatingScore() {
        return writingAndTranslatingScore;
    }

    public void setWritingAndTranslatingScore(String writingAndTranslatingScore) {
        this.writingAndTranslatingScore = writingAndTranslatingScore;
    }
}
