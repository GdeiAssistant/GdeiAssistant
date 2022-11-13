package cn.gdeiassistant.Controller.CardQuery.RestController;

import cn.gdeiassistant.Annotation.QueryLogPersistence;
import cn.gdeiassistant.Annotation.RestAuthentication;
import cn.gdeiassistant.Annotation.RestQueryLogPersistence;
import cn.gdeiassistant.Annotation.TrialData;
import cn.gdeiassistant.Pojo.CardQuery.CardQuery;
import cn.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import cn.gdeiassistant.Pojo.Entity.CardInfo;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.CardQuery.CardQueryService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Pattern;

@RestController
public class CardRestController {

    @Autowired
    private CardQueryService cardQueryService;

    /**
     * 查询消费记录
     *
     * @param request
     * @param cardQuery
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/api/cardquery", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "card", rest = false, responseTime = "cardQuery")
    public DataJsonResult<CardQueryResult> CardQuery(HttpServletRequest request, @Validated CardQuery cardQuery) throws Exception {
        CardQueryResult cardQueryResult = cardQueryService.CardQuery(request.getSession().getId(), cardQuery);
        return new DataJsonResult<>(true, cardQueryResult);
    }

    /**
     * 查询校园卡基本信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/cardinfoquery", method = RequestMethod.POST)
    @QueryLogPersistence
    @TrialData(value = "cardinfo", rest = false)
    public DataJsonResult<CardInfo> CardInfoQuery(HttpServletRequest request) throws Exception {
        CardInfo cardInfo = cardQueryService.CardInfoQuery(request.getSession().getId());
        return new DataJsonResult<>(true, cardInfo);
    }

    /**
     * 设置校园卡挂失
     *
     * @param request
     * @param cardPassword
     * @return
     */
    @RequestMapping(value = "/api/cardlost", method = RequestMethod.POST)
    @TrialData(value = "cardlost", rest = false)
    public JsonResult CardLost(HttpServletRequest request, @Validated @NotBlank @Pattern(regexp = "^[0-9]*$") String cardPassword) throws Exception {
        cardQueryService.CardLost(request.getSession().getId(), cardPassword);
        return new JsonResult(true);
    }

    /**
     * 查询消费记录
     *
     * @param request
     * @param token
     * @param cardQuery
     * @return
     */
    @RequestMapping(value = "/rest/cardquery", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLogPersistence
    @TrialData(value = "card", rest = true, responseTime = "cardQuery")
    public DataJsonResult<CardQueryResult> CardQuery(HttpServletRequest request
            , @RequestParam("token") String token, @Validated CardQuery cardQuery) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CardQueryResult cardQueryResult = cardQueryService.CardQuery(sessionId, cardQuery);
        return new DataJsonResult<>(true, cardQueryResult);
    }

    /**
     * 设置校园卡挂失
     *
     * @param request
     * @param token
     * @param cardPassword
     * @return
     */
    @RequestMapping(value = "/rest/cardlost", method = RequestMethod.POST)
    @RestAuthentication
    @TrialData(value = "cardlost", rest = true)
    public JsonResult CardLost(HttpServletRequest request, @RequestParam("token") String token
            , @Validated @NotBlank @Pattern(regexp = "^[0-9]*$") @RequestParam("cardPassword") String cardPassword) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        cardQueryService.CardLost(sessionId, cardPassword);
        return new JsonResult(true);
    }

    /**
     * 查询校园卡基本信息
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/rest/cardinfo", method = RequestMethod.POST)
    @RestAuthentication
    @TrialData(value = "cardinfo", rest = true)
    public DataJsonResult<CardInfo> CardInfoQuery(HttpServletRequest request
            , @RequestParam("token") String token) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CardInfo cardInfo = cardQueryService.CardInfoQuery(sessionId);
        return new DataJsonResult<>(true, cardInfo);
    }
}
