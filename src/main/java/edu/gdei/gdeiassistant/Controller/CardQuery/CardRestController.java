package edu.gdei.gdeiassistant.Controller.CardQuery;

import edu.gdei.gdeiassistant.Annotation.QueryLogPersistence;
import edu.gdei.gdeiassistant.Annotation.RestAuthentication;
import edu.gdei.gdeiassistant.Annotation.RestQueryLogPersistence;
import edu.gdei.gdeiassistant.Pojo.CardQuery.CardQuery;
import edu.gdei.gdeiassistant.Pojo.CardQuery.CardQueryResult;
import edu.gdei.gdeiassistant.Pojo.Entity.CardInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.CardQuery.CardQueryService;
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
     * @param token
     * @param cardQuery
     * @return
     */
    @RequestMapping(value = "/rest/cardquery", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLogPersistence
    public DataJsonResult<CardQueryResult> CardQuery(HttpServletRequest request
            , @RequestParam("token") String token, @Validated CardQuery cardQuery) throws Exception {
        User user = (User) request.getAttribute("user");
        CardQueryResult cardQueryResult = cardQueryService.CardQuery(request.getSession().getId()
                , user.getUsername(), user.getPassword(), cardQuery);
        return new DataJsonResult<>(true, cardQueryResult);
    }

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
    public DataJsonResult<CardQueryResult> CardQuery(HttpServletRequest request, @Validated CardQuery cardQuery) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        CardQueryResult cardQueryResult = cardQueryService.CardQuery(request.getSession().getId()
                , username, password, cardQuery);
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
    public JsonResult CardLost(HttpServletRequest request, @RequestParam("token") String token
            , @Validated @NotBlank @Pattern(regexp = "^[0-9]*$") String cardPassword) throws Exception {
        User user = (User) request.getAttribute("user");
        cardQueryService.CardLost(request.getSession().getId(), user.getUsername()
                , user.getPassword(), cardPassword);
        return new JsonResult(true);
    }

    /**
     * 查询校园卡基本信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/api/cardinfoquery", method = RequestMethod.POST)
    @QueryLogPersistence
    public DataJsonResult<CardInfo> CardInfoQuery(HttpServletRequest request) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        CardInfo cardInfo = cardQueryService.CardInfoQuery(request.getSession().getId(), username, password);
        return new DataJsonResult<>(true, cardInfo);
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
    public DataJsonResult<CardInfo> CardInfoQuery(HttpServletRequest request, @RequestParam("token") String token) throws Exception {
        User user = (User) request.getAttribute("user");
        CardInfo cardInfo = cardQueryService.CardInfoQuery(request.getSession().getId(), user.getUsername(), user.getPassword());
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
    public JsonResult CardLost(HttpServletRequest request, @Validated @NotBlank @Pattern(regexp = "^[0-9]*$") String cardPassword) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        cardQueryService.CardLost(request.getSession().getId(), username, password, cardPassword);
        return new JsonResult(true);
    }
}
