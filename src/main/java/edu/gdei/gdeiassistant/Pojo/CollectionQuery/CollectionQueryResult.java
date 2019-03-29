package edu.gdei.gdeiassistant.Pojo.CollectionQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.gdei.gdeiassistant.Pojo.Entity.Collection;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionQueryResult implements Serializable {

    private Integer sumPage;

    private List<Collection> collectionList;

    public Integer getSumPage() {
        return sumPage;
    }

    public void setSumPage(Integer sumPage) {
        this.sumPage = sumPage;
    }

    public List<Collection> getCollectionList() {
        return collectionList;
    }

    public void setCollectionList(List<Collection> collectionList) {
        this.collectionList = collectionList;
    }
}
