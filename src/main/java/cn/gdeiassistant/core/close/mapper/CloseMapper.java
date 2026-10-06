package cn.gdeiassistant.core.close.mapper;

import cn.gdeiassistant.common.pojo.entity.CloseLog;
import org.apache.ibatis.annotations.Insert;

public interface CloseMapper {

    @Insert("insert ignore into close_log (username,resetname,time) values(#{username},#{resetname},now())")
    void insertCloseLog(CloseLog closeLog);
}