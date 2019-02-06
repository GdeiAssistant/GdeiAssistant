package com.gdeiassistant.gdeiassistant.Repository.Mongodb.New;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.NewInfo;

import java.util.List;

public interface NewDao {

    public void saveNewInfoList(List<NewInfo> newInfoList);

    public NewInfo queryNewInfo(String id);

    public List<NewInfo> queryNewInfoList(int type, int start, int size);
}
