package cn.gdeiassistant.Controller.Reading.RestController;

import cn.gdeiassistant.Pojo.Entity.Reading;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.Information.Reading.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReadingRestController {

    @Autowired
    private ReadingService readingService;

    /**
     * 加载专题阅读列表信息
     *
     * @return
     */
    @RequestMapping(value = "/api/reading", method = RequestMethod.GET)
    public DataJsonResult<List<Reading>> LoadReadingList() {
        List<Reading> readingList = readingService.LoadingLatestReadingList();
        return new DataJsonResult<>(true, readingList);
    }
}
