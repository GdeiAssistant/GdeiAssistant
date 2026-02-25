package cn.gdeiassistant.core.secondhand.mapper;

import cn.gdeiassistant.core.secondhand.pojo.entity.SecondhandItemEntity;
import cn.gdeiassistant.core.secondhand.pojo.vo.SecondhandItemVO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.Date;
import java.util.List;

/**
 * 二手商品 Mapper。表名、列名均不修改，仅 Java 类型与 property 映射。
 */
public interface SecondhandMapper {

    @Select("select * from ershou join profile using (username) where id=#{id} limit 1")
    @Results(id = "SecondhandItemVO", value = {
            @Result(property = "secondhandItem.id", column = "id"),
            @Result(property = "secondhandItem.username", column = "username"),
            @Result(property = "secondhandItem.name", column = "name"),
            @Result(property = "secondhandItem.description", column = "description"),
            @Result(property = "secondhandItem.price", column = "price"),
            @Result(property = "secondhandItem.location", column = "location"),
            @Result(property = "secondhandItem.type", column = "type"),
            @Result(property = "secondhandItem.qq", column = "qq"),
            @Result(property = "secondhandItem.phone", column = "phone"),
            @Result(property = "secondhandItem.state", column = "state"),
            @Result(property = "secondhandItem.publishTime", column = "publish_time", javaType = Date.class, jdbcType = JdbcType.TIMESTAMP),
            @Result(property = "profile.username", column = "username"),
            @Result(property = "profile.nickname", column = "nickname")
    })
    SecondhandItemVO selectInfoByID(int id);

    @Select("select * from ershou where username=#{username} order by id desc")
    @Results(id = "SecondhandItemEntity", value = {
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
    List<SecondhandItemEntity> selectItemsByUsername(String username);

    @Select("select * from ershou where state='1' order by id desc limit #{start},#{size}")
    @ResultMap("SecondhandItemEntity")
    List<SecondhandItemEntity> selectAvailableItems(@Param("start") int start, @Param("size") int size);

    @Select("select * from ershou where type=#{type} and state='1' order by id desc limit #{start},#{size}")
    @ResultMap("SecondhandItemEntity")
    List<SecondhandItemEntity> selectItemsByType(@Param("start") int start, @Param("size") int size, @Param("type") int type);

    @Select("select distinct * from ershou where state='1' and (name like '%${keyword}%' or description like '%${keyword}%' or location like '%${keyword}%') order by id desc limit #{start},#{size}")
    @ResultMap("SecondhandItemEntity")
    List<SecondhandItemEntity> selectItemsWithKeyword(@Param("start") int start, @Param("size") int size, @Param("keyword") String keyword);

    @Insert("insert into ershou (username,name,description,price,location,type,qq,phone,state,publish_time) values (#{username},#{name},#{description},#{price},#{location},#{type},#{qq},#{phone},'1',#{publishTime})")
    @Options(useGeneratedKeys = true)
    void insertItem(SecondhandItemEntity entity);

    @Update("update ershou set name=#{name},description=#{description},price=#{price},location=#{location},type=#{type},qq=#{qq},phone=#{phone} where id=#{id}")
    void updateItem(SecondhandItemEntity entity);

    @Update("update ershou set state=#{state} where id=#{id}")
    void updateItemState(@Param("id") int id, @Param("state") int state);
}
