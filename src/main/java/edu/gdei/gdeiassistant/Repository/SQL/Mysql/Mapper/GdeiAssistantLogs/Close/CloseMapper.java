package edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistantLogs.Close;

import edu.gdei.gdeiassistant.Pojo.Entity.CloseLog;
import org.apache.ibatis.annotations.Insert;

public interface CloseMapper {

    @Insert("insert into close_log (username,resetname,time) values(#{username},#{resetname},now())")
    public void insertCloseLog(CloseLog closeLog);
}