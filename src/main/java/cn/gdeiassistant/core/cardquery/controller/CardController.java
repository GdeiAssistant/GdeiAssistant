package cn.gdeiassistant.core.cardquery.controller;

import cn.gdeiassistant.common.annotation.RestQueryLogPersistence;
import cn.gdeiassistant.common.annotation.TrialData;
import cn.gdeiassistant.core.cardquery.pojo.CardQuery;
import cn.gdeiassistant.core.cardquery.pojo.CardQueryResult;
import cn.gdeiassistant.common.pojo.Entity.CardInfo;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.cardquery.service.CardQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Pattern;

@RestController
public class CardController {

    @Autowired
    private CardQueryService cardQueryService;

    /**
     * 查询校园卡基本信息
     *
     * @param request
     * @param cardquery
     * @return
     */
    @PostMapping("/api/card/query")
    @RestQueryLogPersistence
    @TrialData(value = "card", responseTime = "cardquery")
    public DataJsonResult<CardQueryResult> cardQuery(HttpServletRequest request,
                                                     @Validated @RequestBody CardQuery cardQuery) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CardQueryResult cardQueryResult = cardQueryService.cardQuery(sessionId, cardQuery);
        return new DataJsonResult<>(true, cardQueryResult);
    }

    /**
     * 设置校园卡挂失。Web 端使用，依赖业务密码（cardPassword）核验，不走移动端设备校验。
     */
    @PostMapping("/api/card/lost")
    @TrialData(value = "cardlost")
    public JsonResult cardLost(HttpServletRequest request,
                               @Validated @Pattern(regexp = "^[0-9]*$") @RequestParam("cardPassword") String cardPassword) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        cardQueryService.cardLost(sessionId, cardPassword);
        return new JsonResult(true);
    }

    /**
     * 查询校园卡基本信息
     *
     * @param request
     * @return
     */
    @GetMapping("/api/card/info")
    @TrialData(value = "cardinfo")
    public DataJsonResult<CardInfo> cardInfoQuery(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        CardInfo cardInfo = cardQueryService.cardInfoQuery(sessionId);
        return new DataJsonResult<>(true, cardInfo);
    }
}
