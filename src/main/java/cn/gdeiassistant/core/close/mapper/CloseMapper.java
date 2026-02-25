package cn.gdeiassistant.core.close.mapper;

import cn.gdeiassistant.common.pojo.Entity.CloseLog;
import org.apache.ibatis.annotations.Insert;

public interface CloseMapper {

    @Insert("insert into close_log (username,resetname,time) values(#{username},#{resetname},now())")
    void insertCloseLog(CloseLog closeLog);
}