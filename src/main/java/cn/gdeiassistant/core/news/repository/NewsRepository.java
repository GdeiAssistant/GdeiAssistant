package cn.gdeiassistant.core.news.repository;

import cn.gdeiassistant.common.pojo.entity.NewsItem;

import java.util.List;

public interface NewsRepository {

    void saveNewsItems(List<NewsItem> newsItems);

    NewsItem queryNewsItem(String id);

    List<NewsItem> queryNewsItems(int type, int start, int size);
}
