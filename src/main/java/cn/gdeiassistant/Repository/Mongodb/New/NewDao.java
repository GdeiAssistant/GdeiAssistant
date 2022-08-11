package cn.gdeiassistant.Repository.Mongodb.New;

import cn.gdeiassistant.Pojo.Entity.NewInfo;

import java.util.List;

public interface NewDao {

    void saveNewInfoList(List<NewInfo> newInfoList);

    NewInfo queryNewInfo(String id);

    List<NewInfo> queryNewInfoList(int type, int start, int size);
}
