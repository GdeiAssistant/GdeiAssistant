package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionDetail implements Serializable, Entity {

    @JsonProperty("collectionDistributionList")
    private List<CollectionDistribution> collectionDistributionList;

    @JsonProperty("bookname")
    private String bookname;

    @JsonProperty("author")
    private String author;

    @JsonProperty("principal")
    private String principal;

    @JsonProperty("publishingHouse")
    private String publishingHouse;

    @JsonProperty("price")
    private String price;

    @JsonProperty("physicalDescriptionArea")
    private String physicalDescriptionArea;

    @JsonProperty("personalPrincipal")
    private String personalPrincipal;

    @JsonProperty("subjectTheme")
    private String subjectTheme;

    @JsonProperty("chineseLibraryClassification")
    private String chineseLibraryClassification;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getPublishingHouse() {
        return publishingHouse;
    }

    public void setPublishingHouse(String publishingHouse) {
        this.publishingHouse = publishingHouse;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getPhysicalDescriptionArea() {
        return physicalDescriptionArea;
    }

    public void setPhysicalDescriptionArea(String physicalDescriptionArea) {
        this.physicalDescriptionArea = physicalDescriptionArea;
    }

    public String getPersonalPrincipal() {
        return personalPrincipal;
    }

    public void setPersonalPrincipal(String personalPrincipal) {
        this.personalPrincipal = personalPrincipal;
    }

    public String getSubjectTheme() {
        return subjectTheme;
    }

    public void setSubjectTheme(String subjectTheme) {
        this.subjectTheme = subjectTheme;
    }

    public String getChineseLibraryClassification() {
        return chineseLibraryClassification;
    }

    public void setChineseLibraryClassification(String chineseLibraryClassification) {
        this.chineseLibraryClassification = chineseLibraryClassification;
    }

    public List<CollectionDistribution> getCollectionDistributionList() {
        return collectionDistributionList;
    }

    public void setCollectionDistributionList(List<CollectionDistribution> collectionDistributionList) {
        this.collectionDistributionList = collectionDistributionList;
    }

    public String getBookname() {
        return bookname;
    }

    public void setBookname(String bookname) {
        this.bookname = bookname;
    }
}
