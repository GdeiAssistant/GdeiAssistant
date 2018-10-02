package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionDetail implements Serializable {

    private List<CollectionDistribution> collectionDistributionList;

    //书名
    private String bookname;

    //作者
    private String author;

    //题名/责任者
    private String principal;

    //出版社/出版年
    private String publishingHouse;

    //ISBN/定价
    private String price;

    //载体形态项
    private String physicalDescriptionArea;

    //个人责任者
    private String personalPrincipal;

    //学科主题
    private String subjectTheme;

    //中图法分类号
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
