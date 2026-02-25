package cn.gdeiassistant.core.schoolNews.controller;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Entity.NewInfo;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.information.service.SchoolNews.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

/**
 * 校园新闻/教务通知：从爬虫缓存读取，对真实用户与测试用户完全开放，无 @TrialData 拦截。
 * 统一使用 /api/news，适配无状态 JWT 架构，不依赖 Session。
 */
@RestController
@RequestMapping("/api/news")
public class SchoolNewsController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    /**
     * 分页获取新闻列表。GET /api/news/type/{type}/start/{start}/size/{size}
     * type: 1教学信息 2考试信息 3教务信息 4行政通知 5综合信息
     */
    @RequestMapping(value = "/type/{type}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<NewInfo>> queryNewInfoList(@PathVariable("type") Integer type
            , @PathVariable("start") Integer start, @PathVariable("size") Integer size) {
        try {
            List<NewInfo> newInfoList = schoolNewsService.queryNewInfoList(type, start, size);
            return new DataJsonResult<>(true, newInfoList);
        } catch (DataNotExistException e) {
            return new DataJsonResult<>(true, Collections.emptyList());
        }
    }
}
