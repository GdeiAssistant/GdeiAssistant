package cn.gdeiassistant.core.dataquery.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.YellowPage;
import cn.gdeiassistant.core.dataquery.pojo.YellowPageType;

import java.io.Serializable;
import java.util.List;

/**
 * 黄页查询结果 VO。
 */
public class YellowPageVO implements Serializable {

    private List<YellowPage> data;
    private List<YellowPageType> type;

    public List<YellowPage> getData() { return data; }
    public void setData(List<YellowPage> data) { this.data = data; }
    public List<YellowPageType> getType() { return type; }
    public void setType(List<YellowPageType> type) { this.type = type; }
}
