package cn.gdeiassistant.core.spareRoom.controller;

import cn.gdeiassistant.common.annotation.QueryLogPersistence;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.spareRoom.pojo.dto.EmptyClassroomQueryDTO;
import cn.gdeiassistant.core.spareRoom.pojo.vo.SpareRoomVO;
import cn.gdeiassistant.core.spareRoom.service.SpareRoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SpareRoomController {

    @Autowired
    private SpareRoomService spareRoomService;

    /**
     * 查询空课室信息。鉴权：请求头 token（或 Authorization: Bearer）由 JwtSessionIdFilter 注入 session。
     * POST /api/spare/query，body: SpareRoomQuery（zone, type, minSeating, maxSeating, startTime, endTime, minWeek, maxWeek, weekType, classNumber）
     */
    @RequestMapping(value = "/api/spare/query", method = RequestMethod.POST)
    @QueryLogPersistence
    public DataJsonResult<List<SpareRoomVO>> querySpareRoomList(HttpServletRequest request,
                                                                @RequestBody @Validated EmptyClassroomQueryDTO query) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        List<SpareRoomVO> list = spareRoomService.querySpareRoom(sessionId, query);
        if (list == null || list.isEmpty()) {
            return new DataJsonResult<>(new JsonResult(false,
                    BackendTextLocalizer.localizeMessage("没有空闲的课室", request.getHeader("Accept-Language"))));
        }
        return new DataJsonResult<>(true, list);
    }
}
