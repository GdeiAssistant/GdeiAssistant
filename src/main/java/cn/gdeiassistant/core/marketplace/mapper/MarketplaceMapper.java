package cn.gdeiassistant.core.marketplace.mapper;

import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

/**
 * 二手商品 Mapper。表名、列名均不修改，仅 Java 类型与 property 映射。
 */
public interface MarketplaceMapper {

    @Select("select * from ershou join profile using (username) where id=#{id} limit 1")
    @Results(id = "MarketplaceItemVO", value = {
            @Result(property = "marketplaceItem.id", column = "id"),
            @Result(property = "marketplaceItem.username", column = "username"),
            @Result(property = "marketplaceItem.name", column = "name"),
            @Result(property = "marketplaceItem.description", column = "description"),
            @Result(property = "marketplaceItem.price", column = "price"),
            @Result(property = "marketplaceItem.location", column = "location"),
            @Result(property = "marketplaceItem.type", column = "type"),
            @Result(property = "marketplaceItem.qq", column = "qq"),
            @Result(property = "marketplaceItem.phone", column = "phone"),
            @Result(property = "marketplaceItem.state", column = "state"),
            @Result(property = "marketplaceItem.publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "profile.username", column = "username"),
            @Result(property = "profile.nickname", column = "nickname")
    })
    MarketplaceItemVO selectInfoByID(int id);

    @Select("select * from ershou where username=#{username} order by id desc")
    @Results(id = "MarketplaceItemEntity", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "price", column = "price"),
            @Result(property = "location", column = "location"),
            @Result(property = "type", column = "type"),
            @Result(property = "qq", column = "qq"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    List<MarketplaceItemEntity> selectItemsByUsername(String username);

    @Select("select * from ershou where state='1' order by id desc limit #{start},#{size}")
    @ResultMap("MarketplaceItemEntity")
    List<MarketplaceItemEntity> selectAvailableItems(@Param("start") int start, @Param("size") int size);

    @Select("select * from ershou where type=#{type} and state='1' order by id desc limit #{start},#{size}")
    @ResultMap("MarketplaceItemEntity")
    List<MarketplaceItemEntity> selectItemsByType(@Param("start") int start, @Param("size") int size, @Param("type") int type);

    @Select("select distinct * from ershou where state='1' and (name like concat('%',#{keyword},'%') or description like concat('%',#{keyword},'%') or location like concat('%',#{keyword},'%')) order by id desc limit #{start},#{size}")
    @ResultMap("MarketplaceItemEntity")
    List<MarketplaceItemEntity> selectItemsWithKeyword(@Param("start") int start, @Param("size") int size, @Param("keyword") String keyword);

    @Insert("insert into ershou (username,name,description,price,location,type,qq,phone,state,publish_time) values (#{username},#{name},#{description},#{price},#{location},#{type},#{qq},#{phone},'1',#{publishTime})")
    @Options(useGeneratedKeys = true)
    void insertItem(MarketplaceItemEntity entity);

    @Update("update ershou set name=#{name},description=#{description},price=#{price},location=#{location},type=#{type},qq=#{qq},phone=#{phone} where id=#{id}")
    void updateItem(MarketplaceItemEntity entity);

    @Update("update ershou set state=#{state} where id=#{id}")
    void updateItemState(@Param("id") int id, @Param("state") int state);
}
