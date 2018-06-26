package com.linguancheng.gdeiassistant.Pojo.CollectionQuery;

import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.Collection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class CollectionQueryResult {

    private ServiceResultEnum collectionQueryResultEnum;

    private int sumPage;

    private List<Collection> collectionList;

    public List<Collection> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<Collection> collectionList) {
        this.collectionList = collectionList;
    }

    public ServiceResultEnum getCollectionQueryResultEnum() {
        return collectionQueryResultEnum;
    }

    public void setCollectionQueryResultEnum(ServiceResultEnum collectionQueryResultEnum) {
        this.collectionQueryResultEnum = collectionQueryResultEnum;
    }

    public int getSumPage() {
        return sumPage;
    }

    public void setSumPage(int sumPage) {
        this.sumPage = sumPage;
    }
}
