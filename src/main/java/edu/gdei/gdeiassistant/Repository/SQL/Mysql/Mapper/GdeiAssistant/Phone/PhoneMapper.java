package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Phone;

import edu.gdei.gdeiassistant.Pojo.Entity.Phone;
import org.apache.ibatis.annotations.*;

public interface PhoneMapper {

    @Select("select * from phone where username=#{username}")
    public Phone selectPhone(String username);

    @Insert("insert into phone (username,code,phone,gmt_create,gmt_modified) values(#{username},#{code},#{phone},now(),now())")
    public void insertPhone(@Param("username") String username, @Param("code") Integer code, @Param("phone") String phone);

    @Update("update phone set code=#{code},phone=#{phone},gmt_modified=now() where username=#{username}")
    public void updatePhone(Phone phone);

    @Delete("delete from phone where username=#{username}")
    public void deletePhone(String username);
}
