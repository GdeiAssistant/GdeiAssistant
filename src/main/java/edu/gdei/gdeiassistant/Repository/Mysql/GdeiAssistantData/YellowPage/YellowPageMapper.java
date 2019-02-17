package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistantData.YellowPage;

import edu.gdei.gdeiassistant.Pojo.DataQuery.YellowPageType;
import edu.gdei.gdeiassistant.Pojo.Entity.YellowPage;

import java.util.List;

public interface YellowPageMapper {

    public List<YellowPage> selectAllYellowPageList();

    public List<YellowPageType> selectYellowPageType();

    public void insertYellowPageBatch(List<YellowPage> yellowPageList);
}
