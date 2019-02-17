package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Ershou;

import edu.gdei.gdeiassistant.Pojo.Entity.ErshouInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.ErshouItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ErshouMapper {

    public ErshouInfo selectInfoByID(int id) throws Exception;

    public List<ErshouItem> selectItemsByUsername(String username) throws Exception;

    public List<ErshouItem> selectAvailableItems(@Param("start") int start, @Param("size") int size) throws Exception;

    public List<ErshouItem> selectItemsByType(@Param("start") int start, @Param("size") int size, @Param("type") int type) throws Exception;

    public List<ErshouItem> selectItemsWithKeyword(@Param("start") int start, @Param("size") int size, @Param("keyword") String keyword) throws Exception;

    public void insertItem(ErshouItem ershouItem) throws Exception;

    public void updateItem(ErshouItem ershouItem) throws Exception;

    public void updateItemState(@Param("id") int id, @Param("state") int state) throws Exception;
}
