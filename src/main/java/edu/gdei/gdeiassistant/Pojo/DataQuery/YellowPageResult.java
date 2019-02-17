package edu.gdei.gdeiassistant.Pojo.DataQuery;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.gdei.gdeiassistant.Pojo.Entity.YellowPage;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class YellowPageResult implements Serializable {

    private List<YellowPage> data;

    private List<YellowPageType> type;

    public List<YellowPage> getData() {
        return data;
    }

    public void setData(List<YellowPage> data) {
        this.data = data;
    }

    public List<YellowPageType> getType() {
        return type;
    }

    public void setType(List<YellowPageType> type) {
        this.type = type;
    }
}
