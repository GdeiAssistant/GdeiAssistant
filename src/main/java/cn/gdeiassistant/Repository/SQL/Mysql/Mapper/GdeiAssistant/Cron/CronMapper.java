package cn.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Cron;

import cn.gdeiassistant.Pojo.Entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CronMapper {

    @Select("select u.username,u.password from user u inner join p on u.username=p.username where p.is_caceh_allow=1")
    public List<User> selectCacheAllowUsers();
}
