package cn.gdeiassistant.core.cron.service;

import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.spi.CacheAllowUserProvider;
import cn.gdeiassistant.core.cron.mapper.CronMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CronCacheUserService implements CacheAllowUserProvider {

    private final CronMapper cronMapper;

    public CronCacheUserService(CronMapper cronMapper) {
        this.cronMapper = cronMapper;
    }

    @Override
    public List<User> listCacheAllowUsers() {
        return cronMapper.selectCacheAllowUsers();
    }
}
