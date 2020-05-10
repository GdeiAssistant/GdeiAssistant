package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Gender;

import org.apache.ibatis.annotations.*;

public interface GenderMapper {

    @Select("select gender from gender where username=#{username}")
    @ResultType(String.class)
    public String selectCustomGender(String username) throws Exception;

    @Insert("insert into gender (username,gender) values(#{username},#{customGender})")
    public void insertCustomGender(@Param("username") String username
            , @Param("customGender") String customGender) throws Exception;

    @Update("update gender set gender=#{customGender} where username=#{username}")
    public void updateCustomGender(@Param("username") String username
            , @Param("customGender") String customGender) throws Exception;

    @Delete("delete from gender where username=#{username}")
    public void deleteCustomGender(String username) throws Exception;
}
