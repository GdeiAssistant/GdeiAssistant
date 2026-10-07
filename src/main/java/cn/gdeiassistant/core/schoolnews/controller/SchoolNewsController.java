package cn.gdeiassistant.core.schoolnews.controller;

import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.pojo.entity.NewsItem;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.common.tools.utils.PageUtils;
import cn.gdeiassistant.core.information.service.schoolnews.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 校园新闻/教务通知：从爬虫缓存读取，对所有用户完全开放。
 * 统一使用 /api/information/news，适配无状态 JWT 架构，不依赖 Session。
 */
@RestController
@RequestMapping("/api/information/news")
public class SchoolNewsController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    /**
     * 分页获取新闻列表。GET /api/information/news/type/{type}/start/{start}/size/{size}
     * type: 1学校要闻 2院部通知 3通知公告 4学术动态
     */
    @RequestMapping(value = "/type/{type}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<NewsItem>> queryNewsItems(@PathVariable("type") Integer type
            , @PathVariable("start") Integer start, @PathVariable("size") Integer size) {
        try {
            size = PageUtils.normalizePageSize(start, size);
            List<NewsItem> newInfoList = schoolNewsService.queryNewsItems(type, start, size);
            return new DataJsonResult<>(true, newInfoList);
        } catch (DataNotExistException e) {
            return new DataJsonResult<>(true, Collections.emptyList());
        }
    }

    /**
     * 获取新闻详情。GET /api/information/news/id/{id}
     */
    @RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<NewsItem> queryNewsItemDetail(@PathVariable("id") String id) {
        try {
            return new DataJsonResult<>(true, schoolNewsService.queryNewsDetail(id));
        } catch (DataNotExistException e) {
            return new DataJsonResult<>(new JsonResult(false, e.getMessage()));
        }
    }
}
