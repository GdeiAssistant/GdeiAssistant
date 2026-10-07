package cn.gdeiassistant.core.information.service.schoolnews;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.pojo.entity.NewsItem;
import cn.gdeiassistant.core.news.repository.NewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchoolNewsService {

    @Autowired
    private NewsRepository newsRepository;

    /**
     * 查找新闻通知信息列表
     *
     * @param type
     * @param start
     * @param size
     * @return
     */
    public List<NewsItem> queryNewsItems(int type, int start, int size) throws DataNotExistException {
        List<NewsItem> newInfoList = newsRepository.queryNewsItems(type, start, size);
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
    public NewsItem queryNewsDetail(String id) throws DataNotExistException {
        NewsItem newInfo = newsRepository.queryNewsItem(id);
        if (newInfo != null) {
            return newInfo;
        }
        throw new DataNotExistException("没有对应的新闻通知信息");
    }
}
