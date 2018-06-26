package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Ershou;

import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ErshouMapper {

    public ErshouItem selectItemByID(int id) throws Exception;

    public List<ErshouItem> selectItemByUsername(String username) throws Exception;

    public List<ErshouItem> selectAvailableItem(@Param("start") int start, @Param("size") int size) throws Exception;

    public List<ErshouItem> selectItemByType(@Param("start") int start, @Param("size") int size, @Param("type") int type) throws Exception;

    public List<ErshouItem> selectItemWithKeyword(@Param("start") int start, @Param("size") int size, @Param("keyword") String keyword) throws Exception;

    public void insertItem(ErshouItem ershouItem) throws Exception;

    public void updateItemInfo(ErshouItem ershouItem) throws Exception;

    public void updateItemState(@Param("id") int id, @Param("state") int state) throws Exception;
}
