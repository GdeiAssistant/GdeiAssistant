package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistantData.YellowPage;

import com.linguancheng.gdeiassistant.Pojo.DataQuery.YellowPageType;
import com.linguancheng.gdeiassistant.Pojo.Entity.YellowPage;

import java.util.List;

public interface YellowPageMapper {

    public List<YellowPage> selectAllYellowPageList();

    public List<YellowPageType> selectYellowPageType();

    public void insertYellowPageBatch(List<YellowPage> yellowPageList);
}
