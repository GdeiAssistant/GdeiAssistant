package edu.gdei.gdeiassistant.Pojo.CollectionQuery;

import edu.gdei.gdeiassistant.Enum.Base.ServiceResultEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Collection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
public class CollectionQueryResult {

    private ServiceResultEnum collectionQueryResultEnum;

    private Integer sumPage;

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

    public Integer getSumPage() {
        return sumPage;
    }

    public void setSumPage(Integer sumPage) {
        this.sumPage = sumPage;
    }
}
