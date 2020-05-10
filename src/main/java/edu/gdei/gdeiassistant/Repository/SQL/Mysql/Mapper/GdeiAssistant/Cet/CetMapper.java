package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Cet;

import edu.gdei.gdeiassistant.Pojo.Entity.CetNumber;
import org.apache.ibatis.annotations.*;

public interface CetMapper {

    @Select("select * from cet where username=#{username}")
    @ResultType(CetNumber.class)
    public CetNumber selectNumber(String username) throws Exception;

    @Insert("insert into cet (username,number) values (#{username},#{number})")
    public void insertNumber(@Param("username") String username, @Param("number") Long number) throws Exception;

    @Update("update cet set number=#{number} where username=#{username}")
    public void updateNumber(@Param("username") String username, @Param("number") Long number) throws Exception;
}