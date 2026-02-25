package cn.gdeiassistant.core.express.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.ExpressException.CorrectRecordException;
import cn.gdeiassistant.common.exception.ExpressException.NoRealNameException;
import cn.gdeiassistant.common.pojo.Entity.ExpressComment;
import cn.gdeiassistant.core.express.pojo.dto.ExpressPublishDTO;
import cn.gdeiassistant.core.express.pojo.vo.ExpressVO;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.express.service.ExpressService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ExpressController {

    @Autowired
    private ExpressService expressService;

    /**
     * 我的表白列表。GET /api/express/profile/start/{start}/size/{size}
     */
    @RequestMapping(value = "/api/express/profile/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<ExpressVO>> getMyExpressList(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<ExpressVO> list = expressService.queryMyExpressList(sessionId, start, size);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/express/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<ExpressVO> getExpressDetail(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        ExpressVO vo = expressService.queryExpressById(id, sessionId);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/express/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<ExpressVO>> queryExpressPage(HttpServletRequest request, @PathVariable("start") int start
            , @PathVariable("size") int size) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<ExpressVO> list = expressService.queryExpressPage(start, size, sessionId);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/express/keyword/{keyword}/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<ExpressVO>> queryExpressPageByKeyWord(HttpServletRequest request, @PathVariable("keyword") String keyword
            , @PathVariable("start") int start, @PathVariable("size") int size) {
        String sessionId = (String) request.getAttribute("sessionId");
        List<ExpressVO> list = expressService.queryExpressPageByKeyword(sessionId, start, size, keyword);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/express", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addExpress(HttpServletRequest request, @Validated ExpressPublishDTO dto) {
        String sessionId = (String) request.getAttribute("sessionId");
        expressService.addExpress(dto, sessionId);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/express/id/{id}/comment", method = RequestMethod.GET)
    public DataJsonResult<List<ExpressComment>> queryExpressComment(HttpServletRequest request, @PathVariable("id") Integer id) {
        List<ExpressComment> list = expressService.queryExpressComment(id);
        return new DataJsonResult<>(true, list);
    }

    /**
     * 添加表白评论
     *
     * @param request
     * @param id
     * @param comment
     * @return
     * @throws DataNotExistException
     */
    @RequestMapping(value = "/api/express/id/{id}/comment", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addExpressComment(HttpServletRequest request, @PathVariable("id") Integer id
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        expressService.addExpressComment(id, sessionId, comment);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/express/id/{id}/guess", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public DataJsonResult<Boolean> guessExpress(HttpServletRequest request, @PathVariable("id") int id
            , @Validated @NotBlank @Length(min = 1, max = 10) String name) throws DataNotExistException, NoRealNameException, CorrectRecordException {
        String sessionId = (String) request.getAttribute("sessionId");
        boolean result = expressService.guessExpress(id, sessionId, name);
        return new DataJsonResult<>(true, result);
    }

    @RequestMapping(value = "/api/express/id/{id}/like", method = RequestMethod.POST)
    public JsonResult likeExpress(HttpServletRequest request, @PathVariable("id") int id) throws DataNotExistException {
        String sessionId = (String) request.getAttribute("sessionId");
        expressService.likeExpress(id, sessionId);
        return new JsonResult(true);
    }
}
