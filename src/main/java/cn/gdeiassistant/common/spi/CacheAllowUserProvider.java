package cn.gdeiassistant.common.spi;

import cn.gdeiassistant.common.pojo.entity.User;

import java.util.List;

/**
 * Contract providing users opted into background academic cache synchronization.
 */
public interface CacheAllowUserProvider {

    List<User> listCacheAllowUsers();
}
