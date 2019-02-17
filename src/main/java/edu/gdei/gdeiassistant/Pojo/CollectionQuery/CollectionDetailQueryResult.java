package edu.gdei.gdeiassistant.Pojo.CollectionQuery;

import edu.gdei.gdeiassistant.Enum.Base.ServiceResultEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.CollectionDetail;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class CollectionDetailQueryResult {

    private ServiceResultEnum collectionDetailQueryResultEnum;

    private CollectionDetail collectionDetail;

    public CollectionDetail getCollectionDetail() {
        return collectionDetail;
    }

    public void setCollectionDetail(CollectionDetail collectionDetail) {
        this.collectionDetail = collectionDetail;
    }

    public ServiceResultEnum getCollectionDetailQueryResultEnum() {
        return collectionDetailQueryResultEnum;
    }

    public void setCollectionDetailQueryResultEnum(ServiceResultEnum collectionDetailQueryResultEnum) {
        this.collectionDetailQueryResultEnum = collectionDetailQueryResultEnum;
    }
}
