package cn.gdeiassistant.core.spare.repository;

import cn.gdeiassistant.common.pojo.Entity.SpareRoom;

import java.util.List;

/**
 * MongoDB 集合 "spare"：测试账号空课室模拟数据查询。
 */
public interface SpareDao {

    /**
     * 按用户名查询空课室列表 spareList，无则返回空列表。
     */
    List<SpareRoom> querySpareListByUsername(String username);
}
