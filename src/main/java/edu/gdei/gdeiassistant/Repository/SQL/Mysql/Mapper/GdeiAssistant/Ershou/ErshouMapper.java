package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Ershou;

import edu.gdei.gdeiassistant.Pojo.Entity.ErshouInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.ErshouItem;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface ErshouMapper {

    @Select("select * from ershou join profile using (username) where id=#{id} limit 1")
    @Results(id = "ErshouInfo", value = {
            @Result(property = "ershouItem.id", column = "id"),
            @Result(property = "ershouItem.username", column = "username"),
            @Result(property = "ershouItem.name", column = "name"),
            @Result(property = "ershouItem.description", column = "description"),
            @Result(property = "ershouItem.price", column = "price"),
            @Result(property = "ershouItem.location", column = "location"),
            @Result(property = "ershouItem.type", column = "type"),
            @Result(property = "ershouItem.qq", column = "qq"),
            @Result(property = "ershouItem.phone", column = "phone"),
            @Result(property = "ershouItem.state", column = "state"),
            @Result(property = "ershouItem.publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "profile.username", column = "username"),
            @Result(property = "profile.nickname", column = "nickname")
    })
    public ErshouInfo selectInfoByID(int id) throws Exception;

    @Select("select * from ershou where username=#{username} order by id desc")
    @Results(id = "ErshouItem", value = {
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
    public List<ErshouItem> selectItemsByUsername(String username) throws Exception;

    @Select("select * from ershou where state='1' order by id desc limit #{start},#{size}")
    @ResultMap("ErshouItem")
    public List<ErshouItem> selectAvailableItems(@Param("start") int start, @Param("size") int size) throws Exception;

    @Select("select * from ershou where type=#{type} and state='1' order by id desc limit #{start},#{size}")
    @ResultMap("ErshouItem")
    public List<ErshouItem> selectItemsByType(@Param("start") int start, @Param("size") int size, @Param("type") int type) throws Exception;

    @Select("select distinct * from ershou where state='1' and (name like '%${keyword}%' or description like '%${keyword}%' or location like '%${keyword}%') order by id desc limit #{start},#{size}")
    @ResultMap("ErshouItem")
    public List<ErshouItem> selectItemsWithKeyword(@Param("start") int start, @Param("size") int size, @Param("keyword") String keyword) throws Exception;

    @Insert("insert into ershou (username,name,description,price,location,type,qq,phone,state,publish_time) values (#{username},#{name},#{description},#{price},#{location},#{type},#{qq},#{phone},'1',#{publishTime})")
    @Options(useGeneratedKeys = true)
    public void insertItem(ErshouItem ershouItem) throws Exception;

    @Update("update ershou set name=#{name},description=#{description},price=#{price},location=#{location},type=#{type},qq=#{qq},phone=#{phone} where id=#{id}")
    public void updateItem(ErshouItem ershouItem) throws Exception;

    @Update("update ershou set state=#{state} where id=#{id}")
    public void updateItemState(@Param("id") int id, @Param("state") int state) throws Exception;
}
