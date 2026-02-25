package cn.gdeiassistant.core.news.repository;

import cn.gdeiassistant.common.pojo.Entity.NewInfo;

import java.util.List;

public interface NewDao {

    void saveNewInfoList(List<NewInfo> newInfoList);

    NewInfo queryNewInfo(String id);

    List<NewInfo> queryNewInfoList(int type, int start, int size);
}
