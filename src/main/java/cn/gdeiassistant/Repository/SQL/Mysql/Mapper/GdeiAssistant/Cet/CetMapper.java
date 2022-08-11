package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Cet;

import cn.gdeiassistant.Pojo.Entity.CetNumber;
import org.apache.ibatis.annotations.*;

public interface CetMapper {

    @Select("select * from cet where username=#{username}")
    @ResultType(CetNumber.class)
    CetNumber selectNumber(String username);

    @Insert("insert into cet (username,number) values (#{username},#{number})")
    void insertNumber(@Param("username") String username, @Param("number") Long number);

    @Update("update cet set number=#{number} where username=#{username}")
    void updateNumber(@Param("username") String username, @Param("number") Long number);
}