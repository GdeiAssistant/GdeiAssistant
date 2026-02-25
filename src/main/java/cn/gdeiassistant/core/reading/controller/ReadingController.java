package cn.gdeiassistant.core.reading.controller;

import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.information.pojo.vo.ReadingVO;
import cn.gdeiassistant.core.information.service.Reading.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReadingController {

    @Autowired
    private ReadingService readingService;

    /**
     * 加载专题阅读列表信息
     *
     * @return
     */
    @RequestMapping(value = "/api/reading", method = RequestMethod.GET)
    public DataJsonResult<List<ReadingVO>> loadReadingList() {
        List<ReadingVO> readingList = readingService.loadLatestReadingList();
        return new DataJsonResult<>(true, readingList);
    }
}
