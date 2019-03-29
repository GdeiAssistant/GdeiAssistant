package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.LostAndFound;

import edu.gdei.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface LostAndFoundMapper {

    @Select("select * from lostandfound join profile using (username) where id=#{id} limit 1")
    @Results(id = "LostAndFoundItem", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "itemType", column = "item_type"),
            @Result(property = "lostType", column = "lost_type"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    public LostAndFoundInfo selectInfoByID(Integer id) throws Exception;

    @Select("select * from lostandfound where username=#{username} order by id desc")
    @ResultMap("LostAndFoundItem")
    public List<LostAndFoundItem> selectItemByUsername(String username) throws Exception;

    @Select("select * from lostandfound where lost_type=#{lostType} and state='0' order by id desc limit #{start},#{size}")
    @ResultMap("LostAndFoundItem")
    public List<LostAndFoundItem> selectAvailableItem(@Param("lostType") Integer lostType
            , @Param("start") Integer start, @Param("size") Integer size) throws Exception;

    @Select("select * from lostandfound where lost_type=#{lostType} and item_type=#{itemType} and state='0' order by id desc limit #{start},#{size}")
    @ResultMap("LostAndFoundItem")
    public List<LostAndFoundItem> selectItemByItemType(@Param("lostType") Integer lostType
            , @Param("itemType") Integer itemType, @Param("start") Integer start
            , @Param("size") Integer size) throws Exception;

    @Select("select distinct * from lostandfound where state='0' and lost_type=#{lostType} and (name like '%${keyword}%' or description like '%${keyword}%' " +
            "or location like '%${keyword}%') order by id desc limit #{start},#{size}")
    @ResultMap("LostAndFoundItem")
    public List<LostAndFoundItem> selectItemWithKeyword(@Param("lostType") Integer lostType
            , @Param("keyword") String keyword, @Param("start") Integer start
            , @Param("size") Integer size) throws Exception;

    @Insert("insert into lostandfound (username,name,description,location,item_type,lost_type,qq,wechat,phone,state,publish_time) " +
            "values (#{username},#{name},#{description},#{location},#{itemType},#{lostType},#{qq},#{wechat},#{phone},'0',#{publishTime})")
    @Options(useGeneratedKeys = true)
    public void insertItem(LostAndFoundItem lostAndFoundItem) throws Exception;

    @Update("update lostandfound set name=#{name},description=#{description},location=#{location},item_type=#{itemType},lost_type=#{lostType},qq=#{qq},wechat=#{wechat},phone=#{phone} where id=#{id}")
    public void updateItemItem(LostAndFoundItem lostAndFoundItem) throws Exception;

    @Update("update lostandfound set state=#{state} where id=#{id}")
    public void updateItemState(@Param("id") Integer id, @Param("state") Integer state) throws Exception;
}
