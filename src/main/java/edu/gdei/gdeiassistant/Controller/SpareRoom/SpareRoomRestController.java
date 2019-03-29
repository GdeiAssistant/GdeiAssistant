package edu.gdei.gdeiassistant.Controller.SpareRoom;

import edu.gdei.gdeiassistant.Annotation.QueryLogPersistence;
import edu.gdei.gdeiassistant.Pojo.Entity.SpareRoom;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Pojo.SpareRoomQuery.SpareRoomQuery;
import edu.gdei.gdeiassistant.Service.SpareRoom.SpareRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SpareRoomRestController {

    @Autowired
    private SpareRoomService spareRoomService;

    /**
     * 查询空课表信息
     *
     * @param request
     * @param spareRoomQuery
     * @return
     */
    @RequestMapping(value = "/api/sparequery", method = RequestMethod.POST)
    @QueryLogPersistence
    public DataJsonResult<List<SpareRoom>> QuerySpareRoomList(HttpServletRequest request
            , @Validated SpareRoomQuery spareRoomQuery) throws Exception {
        String username = (String) WebUtils.getSessionAttribute(request, "username");
        String password = (String) WebUtils.getSessionAttribute(request, "password");
        List<SpareRoom> spareRoomList = spareRoomService.SyncSessionAndQuerySpareRoom(request.getSession().getId()
                , new User(username, password), spareRoomQuery);
        if (spareRoomList == null || spareRoomList.isEmpty()) {
            return new DataJsonResult<>(new JsonResult(false, "没有空闲的课室"));
        }
        return new DataJsonResult<>(true, spareRoomList);
    }
}
