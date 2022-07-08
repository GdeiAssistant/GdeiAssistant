package cn.gdeiassistant.Service.Information.SchoolNews;

import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Pojo.Entity.NewInfo;
import cn.gdeiassistant.Repository.Mongodb.New.NewDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolNewsService {

    @Autowired
    private NewDao newDao;

    /**
     * 查找新闻通知信息列表
     *
     * @param type
     * @param start
     * @param size
     * @return
     */
    public List<NewInfo> QueryNewInfoList(int type, int start, int size) throws DataNotExistException {
        List<NewInfo> newInfoList = newDao.queryNewInfoList(type, start, size);
        if (newInfoList != null && !newInfoList.isEmpty()) {
            return newInfoList;
        }
        throw new DataNotExistException("没有更多的新闻通知信息");
    }

    /**
     * 查询新闻通知详细信息
     *
     * @param id
     * @return
     */
    public NewInfo QueryNewDetailInfo(String id) throws DataNotExistException {
        NewInfo newInfo = newDao.queryNewInfo(id);
        if (newInfo != null) {
            return newInfo;
        }
        throw new DataNotExistException("没有对应的新闻通知信息");
    }
}
