package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Graduation;

import edu.gdei.gdeiassistant.Pojo.Entity.Graduation;
import org.apache.ibatis.annotations.*;

public interface GraduationMapper {

    @Select("select * from graduation where username=#{username}")
    @Results(id = "Graduation", value = {
            @Result(property = "username", column = "username"),
            @Result(property = "program", column = "program")
    })
    public Graduation selectGraduation(String username);

    @Insert("insert into graduation (username,program) values(#{username},#{program})")
    public void insertGraduation(Graduation graduation);

    @Update("update graduation set program=#{program} where username=#{username}")
    public void updateGraduation(Graduation graduation);

    @Update("update graduation set program=null where username=#{username}")
    public void resetGraduation(String username);
}
