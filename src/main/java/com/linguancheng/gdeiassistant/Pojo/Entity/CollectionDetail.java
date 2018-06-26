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

    private String detailContent;

    public String getDetailContent() {
        return detailContent;
    }

    public void setDetailContent(String detailContent) {
        this.detailContent = detailContent;
    }

    public List<CollectionDistribution> getCollectionDistributionList() {
        return collectionDistributionList;
    }

    public void setCollectionDistributionList(List<CollectionDistribution> collectionDistributionList) {
        this.collectionDistributionList = collectionDistributionList;
    }
}
