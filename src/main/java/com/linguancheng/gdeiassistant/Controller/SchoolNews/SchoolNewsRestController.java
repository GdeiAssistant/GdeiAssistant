package com.linguancheng.gdeiassistant.Controller.SchoolNews;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Pojo.Entity.NewInfo;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.SchoolNews.SchoolNewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SchoolNewsRestController {

    @Autowired
    private SchoolNewsService schoolNewsService;

    @RequestMapping(value = "/rest/new/type/{type}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<NewInfo>> QueryNewInfoList(@PathVariable("type") Integer type
            , @PathVariable("start") Integer start, @PathVariable("size") Integer size) throws DataNotExistException {
        List<NewInfo> newInfoList = schoolNewsService.QueryNewInfoList(type, start, size);
        return new DataJsonResult<>(true, newInfoList);
    }
}
