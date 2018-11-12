package com.linguancheng.gdeiassistant.Controller.CardQuery;

import com.linguancheng.gdeiassistant.Annotation.QueryLog;
import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Annotation.RestQueryLog;
import com.linguancheng.gdeiassistant.Enum.Base.ServiceResultEnum;
import com.linguancheng.gdeiassistant.Pojo.CardQuery.*;
import com.linguancheng.gdeiassistant.Pojo.Entity.CardInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.CardQuery.CardQueryService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class CardController {

    @Autowired
    private CardQueryService cardQueryService;

    @RequestMapping(value = "/card", method = RequestMethod.GET)
    public ModelAndView ResolveCardPage() {
        return new ModelAndView("Card/card");
    }

    @RequestMapping(value = "/cardinfo", method = RequestMethod.GET)
    public ModelAndView ResolveCardInfoPage() {
        return new ModelAndView("Card/cardInfo");
    }

    @RequestMapping(value = "/cardlost", method = RequestMethod.GET)
    public ModelAndView ResolveCardLostPage() {
        return new ModelAndView("Card/cardLost");
    }

    /**
     * 查询消费记录
     *
     * @param request
     * @param token
     * @param cardQuery
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/cardquery", method = RequestMethod.POST)
    @RestAuthentication
    @RestQueryLog
    @ResponseBody
    public CardQueryJsonResult CardQuery(HttpServletRequest request
            , @RequestParam("token") String token, @Validated CardQuery cardQuery
            , BindingResult bindingResult) {
        CardQueryJsonResult cardQueryJsonResult = new CardQueryJsonResult();
        if (bindingResult.hasErrors()) {
            cardQueryJsonResult.setSuccess(false);
            cardQueryJsonResult.setMessage("请求参数不合法");
        } else {
            User user = (User) request.getAttribute("user");
            CardQueryResult cardQueryResult = cardQueryService.CardQuery(request, user.getUsername()
                    , user.getPassword(), cardQuery);
            switch (cardQueryResult.getCardServiceResultEnum()) {
                case PASSWORD_INCORRECT:
                    //用户名或密码错误
                    cardQueryJsonResult.setSuccess(false);
                    cardQueryJsonResult.setMessage("用户名或密码错误,请重新登录");
                    break;

                case TIME_OUT:
                    cardQueryJsonResult.setSuccess(false);
                    cardQueryJsonResult.setMessage("网络连接超时,请稍候再试");
                    break;

                case SERVER_ERROR:
                    cardQueryJsonResult.setSuccess(false);
                    cardQueryJsonResult.setMessage("校园支付管理平台维护中,请稍候再试");
                    break;

                case SUCCESS:
                    cardQueryJsonResult.setSuccess(true);
                    cardQueryJsonResult.setCardInfo(cardQueryResult.getCardInfo());
                    cardQueryJsonResult.setCardList(cardQueryResult.getCardList());
                    break;
            }
        }
        return cardQueryJsonResult;
    }

    @RequestMapping(value = "/cardquery", method = RequestMethod.POST)
    @QueryLog
    @ResponseBody
    public DataJsonResult<CardQueryResult> CardQuery(HttpServletRequest request, @Validated CardQuery cardQuery
            , BindingResult bindingResult) {
        DataJsonResult<CardQueryResult> result = new DataJsonResult<>();
        if (bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            String password = (String) request.getSession().getAttribute("password");
            CardQueryResult cardQueryResult = cardQueryService.CardQuery(request, username, password, cardQuery);
            switch (cardQueryResult.getCardServiceResultEnum()) {
                case SUCCESS:
                    //查询成功
                    result.setSuccess(true);
                    result.setData(cardQueryResult);
                    break;

                case TIME_OUT:
                    //查询超时
                    result.setSuccess(false);
                    result.setMessage("查询消费记录超时，请重试");
                    break;

                case PASSWORD_INCORRECT:
                    //身份凭证异常
                    result.setSuccess(false);
                    result.setMessage("你的密码已更新，请重新登录");
                    break;

                case SERVER_ERROR:
                    //服务器异常
                    result.setSuccess(false);
                    result.setMessage("支付管理平台维护中，请稍后再试");
                    break;
            }
        }
        return result;
    }

    /**
     * 查询校园卡基本信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/cardinfoquery", method = RequestMethod.POST)
    @QueryLog
    @ResponseBody
    public DataJsonResult<CardInfo> CardInfoQuery(HttpServletRequest request) {
        DataJsonResult<CardInfo> jsonResult = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        String password = (String) request.getSession().getAttribute("password");
        if (!StringUtils.isBlank(username) && !StringUtils.isBlank(password)) {
            BaseResult<CardInfo, ServiceResultEnum> queryResult = cardQueryService.CardInfoQuery(request, username, password);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(queryResult.getResultData());
                    break;

                case TIME_OUT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("网络连接超时，请重试");
                    break;

                case SERVER_ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("校园支付管理平台维护中，请稍后再试");
                    break;
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("用户身份凭证已过期，请稍后再试");
        }
        return jsonResult;
    }

    /**
     * 获取校园卡基本信息
     *
     * @param request
     * @param token
     * @return
     */
    @RequestMapping(value = "/rest/cardinfo", method = RequestMethod.POST)
    @RestAuthentication
    @ResponseBody
    public CardInfoQueryJsonResult CardInfoQuery(HttpServletRequest request
            , @RequestParam("token") String token) {
        CardInfoQueryJsonResult cardInfoQueryJsonResult = new CardInfoQueryJsonResult();
        User user = (User) request.getAttribute("user");
        BaseResult<CardInfo, ServiceResultEnum> result = cardQueryService.CardInfoQuery(request
                , user.getUsername(), user.getPassword());
        switch (result.getResultType()) {
            case PASSWORD_INCORRECT:
                //用户名或密码错误
                cardInfoQueryJsonResult.setSuccess(false);
                cardInfoQueryJsonResult.setMessage("用户名或密码错误,请重新登录");
                break;

            case TIME_OUT:
                //连接超时
                cardInfoQueryJsonResult.setSuccess(false);
                cardInfoQueryJsonResult.setMessage("网络连接超时,请稍候再试");
                break;

            case SERVER_ERROR:
                //服务器异常
                cardInfoQueryJsonResult.setSuccess(false);
                cardInfoQueryJsonResult.setMessage("校园支付管理平台维护中,请稍候再试");
                break;

            case SUCCESS:
                //连接成功
                cardInfoQueryJsonResult.setSuccess(true);
                cardInfoQueryJsonResult.setCardInfo(result.getResultData());
                break;
        }
        return cardInfoQueryJsonResult;
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
    @ResponseBody
    public JsonResult CardLost(HttpServletRequest request, @RequestParam("token") String token
            , String cardPassword) {
        JsonResult jsonResult = new JsonResult();
        User user = (User) request.getAttribute("user");
        if (StringUtils.isNotBlank(cardPassword) && cardPassword.length() == 6
                && cardPassword.matches("^[0-9]*$")) {
            BaseResult<String, ServiceResultEnum> cardLostResult = cardQueryService
                    .CardLost(request, user.getUsername(), user.getPassword(), cardPassword);
            switch (cardLostResult.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    break;

                case TIME_OUT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("网络连接超时，请重试");
                    break;

                case SERVER_ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("校园支付管理平台维护中，请稍后再试");
                    break;

                case ERROR_CONDITION:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage(cardLostResult.getResultData());
                    break;
            }
        } else {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
        }
        return jsonResult;
    }

    /**
     * 设置校园卡挂失
     *
     * @param request
     * @param cardPassword
     * @return
     */
    @RequestMapping(value = "/cardlost", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult CardLost(HttpServletRequest request, String cardPassword) {
        JsonResult jsonResult = new JsonResult();
        if (StringUtils.isBlank(cardPassword)) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            String password = (String) request.getSession().getAttribute("password");
            if (StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)
                    && StringUtils.isNotBlank(cardPassword) && cardPassword.length() == 6
                    && cardPassword.matches("^[0-9]*$")) {
                BaseResult<String, ServiceResultEnum> cardLostResult = cardQueryService.CardLost(request, username, password, cardPassword);
                switch (cardLostResult.getResultType()) {
                    case SUCCESS:
                        jsonResult.setSuccess(true);
                        break;

                    case TIME_OUT:
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("网络连接超时，请重试");
                        break;

                    case SERVER_ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("校园支付管理平台维护中，请稍后再试");
                        break;

                    case ERROR_CONDITION:
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage(cardLostResult.getResultData());
                        break;
                }
            } else {
                jsonResult.setSuccess(false);
                jsonResult.setMessage("用户身份凭证已过期，请稍后再试");
            }
        }
        return jsonResult;
    }
}
