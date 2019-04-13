package edu.gdei.gdeiassistant.Controller.Reading;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Pojo.Entity.Reading;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Reading.ReadingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
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

    /**
     * 保存专题阅读信息
     *
     * @param request
     * @param reading
     * @return
     */
    @RequestMapping(value = "/api/reading", method = RequestMethod.POST)
    @UserGroupAccess(group = 0)
    public JsonResult SaveReadingInfo(HttpServletRequest request, @Validated Reading reading) {
        readingService.SaveReadingInfo(reading);
        return new JsonResult(true);
    }
}
