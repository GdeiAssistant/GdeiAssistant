package cn.gdeiassistant.core.cron.mapper;

import cn.gdeiassistant.common.pojo.Entity.User;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CronMapper {

    @Select("select u.username,u.password from user u inner join p on u.username=p.username where p.is_caceh_allow=1")
    List<User> selectCacheAllowUsers();
}
