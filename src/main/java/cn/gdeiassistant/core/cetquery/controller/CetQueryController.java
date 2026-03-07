package cn.gdeiassistant.core.cetquery.controller;

import cn.gdeiassistant.common.pojo.Entity.Cet;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.cetquery.pojo.dto.CetNumberSaveDTO;
import cn.gdeiassistant.core.cetquery.pojo.dto.CetQueryDTO;
import cn.gdeiassistant.core.cetquery.pojo.vo.CetNumberVO;
import cn.gdeiassistant.core.cetquery.service.CetQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class CetQueryController {

    @Autowired
    private CetQueryService cetQueryService;

    @RequestMapping(value = "/api/cet/number", method = RequestMethod.GET)
    public DataJsonResult<CetNumberVO> getCetNumber(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CetNumberVO vo = cetQueryService.getCetNumber(sessionId);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/cet/number", method = RequestMethod.POST)
    public JsonResult SaveCetNumber(HttpServletRequest request, @RequestBody CetNumberSaveDTO body) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        if (body == null || body.getNumber() == null || body.getNumber().trim().isEmpty()) {
            return new JsonResult(false, "准考证号不能为空");
        }
        String numStr = body.getNumber().trim();
        if (numStr.length() != 15) {
            return new JsonResult(false, "准考证号必须为15位");
        }
        long number;
        try {
            number = Long.parseLong(numStr);
        } catch (NumberFormatException e) {
            return new JsonResult(false, "准考证号必须为15位数字");
        }
        String name = (body.getName() != null && !body.getName().trim().isEmpty()) ? body.getName().trim() : null;
        cetQueryService.saveCetNumber(sessionId, number, name);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/cet/query", method = RequestMethod.GET)
    public DataJsonResult<Cet> cetQuery(HttpServletRequest request,
                                        @RequestParam("ticketNumber") String ticketNumber,
                                        @RequestParam("name") String name,
                                        @RequestParam(value = "checkcode", required = false) String checkcode) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        Cet cet = cetQueryService.queryCetScore(sessionId, ticketNumber, name, checkcode != null ? checkcode : "");
        return new DataJsonResult<>(true, cet);
    }

    @RequestMapping(value = "/api/cet/checkcode", method = RequestMethod.GET)
    public DataJsonResult<String> getCheckCodeImage(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        String checkCode = cetQueryService.cetIndex(sessionId);
        return new DataJsonResult<>(true, checkCode);
    }
}
