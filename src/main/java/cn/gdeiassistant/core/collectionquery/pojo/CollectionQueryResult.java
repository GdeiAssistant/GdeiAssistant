package cn.gdeiassistant.core.collectionquery.pojo;

import cn.gdeiassistant.common.pojo.Entity.Collection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionQueryResult implements Serializable {

    @JsonProperty("sumPage")
    private Integer sumPage;

    @JsonProperty("collectionList")
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
