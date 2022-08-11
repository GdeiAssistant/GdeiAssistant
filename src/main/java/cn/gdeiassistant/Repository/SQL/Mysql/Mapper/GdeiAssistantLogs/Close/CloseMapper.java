package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Close;

import cn.gdeiassistant.Pojo.Entity.CloseLog;
import org.apache.ibatis.annotations.Insert;

public interface CloseMapper {

    @Insert("insert into close_log (username,resetname,time) values(#{username},#{resetname},now())")
    void insertCloseLog(CloseLog closeLog);
}