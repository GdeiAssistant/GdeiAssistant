package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.LostAndFound;

import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LostAndFoundMapper {

    public LostAndFoundItem selectItemByID(Integer id) throws Exception;

    public List<LostAndFoundItem> selectItemByUsername(String username) throws Exception;

    public List<LostAndFoundItem> selectAvailableItem(@Param("lostType") Integer lostType
            , @Param("start") Integer start, @Param("size") Integer size) throws Exception;

    public List<LostAndFoundItem> selectItemByItemType(@Param("lostType") Integer lostType
            , @Param("itemType") Integer itemType, @Param("start") Integer start
            , @Param("size") Integer size) throws Exception;

    public List<LostAndFoundItem> selectItemWithKeyword(@Param("lostType") Integer lostType
            , @Param("keyword") String keyword, @Param("start") Integer start
            , @Param("size") Integer size) throws Exception;

    public void insertItem(LostAndFoundItem lostAndFoundItem) throws Exception;

    public void updateItemInfo(LostAndFoundItem lostAndFoundItem) throws Exception;

    public void updateItemState(@Param("id") Integer id, @Param("state") Integer state) throws Exception;
}
