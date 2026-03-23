package cn.gdeiassistant.core.lostandfound.mapper;

import cn.gdeiassistant.core.profile.pojo.entity.ProfileEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundDetailEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

public interface LostAndFoundMapper {

    @Select("select * from lostandfound join profile using (username) where id=#{id} limit 1")
    @Results(id = "LostAndFoundDetail", value = {
            @Result(property = "item.id", column = "id"),
            @Result(property = "item.username", column = "username"),
            @Result(property = "item.name", column = "name"),
            @Result(property = "item.description", column = "description"),
            @Result(property = "item.location", column = "location"),
            @Result(property = "item.itemType", column = "item_type"),
            @Result(property = "item.lostType", column = "lost_type"),
            @Result(property = "item.qq", column = "qq"),
            @Result(property = "item.wechat", column = "wechat"),
            @Result(property = "item.phone", column = "phone"),
            @Result(property = "item.state", column = "state"),
            @Result(property = "item.publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "profile.username", column = "username"),
            @Result(property = "profile.nickname", column = "nickname"),
            @Result(property = "profile.birthday", column = "birthday"),
            @Result(property = "profile.enrollment", column = "degree"),
            @Result(property = "profile.faculty", column = "faculty"),
            @Result(property = "profile.major", column = "major"),
            @Result(property = "profile.locationRegion", column = "region"),
            @Result(property = "profile.locationState", column = "state"),
            @Result(property = "profile.locationCity", column = "city"),
    })
    LostAndFoundDetailEntity selectInfoByID(Integer id);

    @Select("select * from lostandfound where username=#{username} order by id desc limit 500")
    @Results(id = "LostAndFoundItem", value = {
            @Result(property = "id", column = "id"),
            @Result(property = "username", column = "username"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "location", column = "location"),
            @Result(property = "itemType", column = "item_type"),
            @Result(property = "lostType", column = "lost_type"),
            @Result(property = "qq", column = "qq"),
            @Result(property = "wechat", column = "wechat"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "state", column = "state"),
            @Result(property = "publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP)
    })
    List<LostAndFoundItemEntity> selectItemByUsername(String username);

    @Select("select * from lostandfound where lost_type=#{lostType} and state='0' order by id desc limit #{start},#{size}")
    @ResultMap("LostAndFoundItem")
    List<LostAndFoundItemEntity> selectAvailableItem(@Param("lostType") Integer lostType,
            @Param("start") Integer start, @Param("size") Integer size);

    @Select("select * from lostandfound where lost_type=#{lostType} and item_type=#{itemType} and state='0' order by id desc limit #{start},#{size}")
    @ResultMap("LostAndFoundItem")
    List<LostAndFoundItemEntity> selectItemByItemType(@Param("lostType") Integer lostType,
            @Param("itemType") Integer itemType, @Param("start") Integer start, @Param("size") Integer size);

    @Select("select distinct * from lostandfound where state='0' and lost_type=#{lostType} and (name like concat('%',#{keyword},'%') or description like concat('%',#{keyword},'%') " +
            "or location like concat('%',#{keyword},'%')) order by id desc limit #{start},#{size}")
    @ResultMap("LostAndFoundItem")
    List<LostAndFoundItemEntity> selectItemWithKeyword(@Param("lostType") Integer lostType,
            @Param("keyword") String keyword, @Param("start") Integer start, @Param("size") Integer size);

    @Insert("insert into lostandfound (username,name,description,location,item_type,lost_type,qq,wechat,phone,state,publish_time) " +
            "values (#{username},#{name},#{description},#{location},#{itemType},#{lostType},#{qq},#{wechat},#{phone},0,#{publishTime})")
    @Options(useGeneratedKeys = true)
    void insertItem(LostAndFoundItemEntity item);

    @Update("update lostandfound set name=#{name},description=#{description},location=#{location},item_type=#{itemType},lost_type=#{lostType},qq=#{qq},wechat=#{wechat},phone=#{phone} where id=#{id}")
    void updateItemItem(LostAndFoundItemEntity item);

    @Update("update lostandfound set state=#{state} where id=#{id}")
    void updateItemState(@Param("id") Integer id, @Param("state") Integer state);

    @Delete("delete from lostandfound where id=#{id}")
    void deleteItem(@Param("id") int id);
}
