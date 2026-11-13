package cn.gdeiassistant.core.spareroom.controller;

import cn.gdeiassistant.common.annotation.QueryLogPersistence;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.common.i18n.MessageCatalog;
import cn.gdeiassistant.core.spareroom.pojo.dto.EmptyClassroomQueryDTO;
import cn.gdeiassistant.core.spareroom.pojo.vo.SpareRoomVO;
import cn.gdeiassistant.core.spareroom.service.SpareRoomService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import cn.gdeiassistant.common.i18n.Messages;

@RestController
public class SpareRoomController {

    private final SpareRoomService spareRoomService;

    public SpareRoomController(SpareRoomService spareRoomService) {
        this.spareRoomService = spareRoomService;
    }

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
                    Messages.forLanguage("spare.empty", request.getHeader("Accept-Language"))));
        }
        return new DataJsonResult<>(true, list);
    }
}
