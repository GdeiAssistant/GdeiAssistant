package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.New;

import edu.gdei.gdeiassistant.Pojo.Entity.NewInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface NewMapper {
    
    public NewInfo selectNewInfo(@Param("id") int id) throws Exception;

    public List<NewInfo> selectNewInfoList(@Param("start") int start, @Param("size") int size) throws Exception;

    public String selectNewInfoByTitle(String title) throws Exception;

    public void insertNewInfo(NewInfo newInfo) throws Exception;
}
