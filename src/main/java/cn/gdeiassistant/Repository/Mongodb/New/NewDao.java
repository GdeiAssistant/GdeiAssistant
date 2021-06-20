package cn.gdeiassistant.Repository.Mongodb.New;

import cn.gdeiassistant.Pojo.Entity.NewInfo;

import java.util.List;

public interface NewDao {

    public void saveNewInfoList(List<NewInfo> newInfoList);

    public NewInfo queryNewInfo(String id);

    public List<NewInfo> queryNewInfoList(int type, int start, int size);
}
