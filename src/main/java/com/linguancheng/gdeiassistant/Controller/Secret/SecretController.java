package com.linguancheng.gdeiassistant.Controller.Secret;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.Secret;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.Secret.SecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class SecretController {

    @Autowired
    private SecretService secretService;

    /**
     * 进入校园树洞应用首页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/secret"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretIndexPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<List<Secret>, DataBaseResultEnum> result = secretService.GetSecretInfo(0, 10, username);
        switch (result.getResultType()) {
            case SUCCESS:
                modelAndView.addObject("SecretList", result.getResultData());
                modelAndView.setViewName("Secret/secretIndex");
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("Secret/secretIndex");
                break;

            case ERROR:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院树洞-错误");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再试");
                break;
        }
        return modelAndView;
    }

    /**
     * 进入树洞信息发布界面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/secret/publish"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretPublishPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Secret/secretPublish");
        return modelAndView;
    }

    /**
     * 进入树洞个人页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/secret/profile"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretProfilePage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<List<Secret>, DataBaseResultEnum> result = secretService.GetSecretInfo(username);
        switch (result.getResultType()) {
            case SUCCESS:
                modelAndView.addObject("SecretList", result.getResultData());
                modelAndView.setViewName("Secret/secretProfile");
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("Secret/secretProfile");
                break;

            case ERROR:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院树洞-错误");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再试");
                break;
        }
        return modelAndView;
    }

    /**
     * 进入树洞信息详细信息页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/secret/detail/id/{id}"}, method = RequestMethod.GET)
    public ModelAndView ResolveSecretDetailPage(HttpServletRequest request, @PathVariable("id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<Secret, DataBaseResultEnum> result = secretService.GetSecretDetailInfo(id, username);
        switch (result.getResultType()) {
            case SUCCESS:
                modelAndView.setViewName("Secret/secretDetail");
                modelAndView.addObject("Secret", result.getResultData());
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院树洞-错误");
                modelAndView.addObject("ErrorMessage", "树洞信息不存在");
                break;

            case ERROR:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院树洞-错误");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再试");
                break;
        }
        return modelAndView;
    }

    /**
     * 获取更多的树洞消息
     *
     * @param start
     * @return
     */
    @RequestMapping(value = "/rest/secret/info/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<Secret>> GetMoreSecret(HttpServletRequest request, @PathVariable("start") int start) {
        DataJsonResult<List<Secret>> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("用户凭证已过期，请重新登录");
        } else {
            BaseResult<List<Secret>, DataBaseResultEnum> queryResult = secretService.GetSecretInfo(start, 10, username);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    result.setSuccess(true);
                    result.setData(queryResult.getResultData());
                    break;

                case EMPTY_RESULT:
                    result.setSuccess(true);
                    break;

                case ERROR:
                    result.setSuccess(false);
                    result.setMessage("服务器异常，请稍候再试");
                    break;
            }
        }
        return result;
    }

    /**
     * 发布树洞消息
     *
     * @param request
     * @param secret
     * @return
     */
    @RequestMapping(value = "/rest/secret/info", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult AddSecretInfo(HttpServletRequest request, @Validated Secret secret
            , BindingResult bindingResult) {
        JsonResult result = new JsonResult();
        if (bindingResult.hasErrors()) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
            return result;
        }
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("用户凭证已过期，请重新登录");
        } else {
            boolean addSecretResult = secretService.AddSecretInfo(username, secret);
            if (addSecretResult) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                result.setMessage("服务器异常，请稍候再试");
            }
        }
        return result;
    }

    /**
     * 添加树洞评论
     *
     * @param request
     * @param id
     * @param comment
     * @return
     */
    @RequestMapping(value = "/rest/secret/id/{id}/comment", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult AddSecretComment(HttpServletRequest request, @PathVariable("id") int id, String comment) {
        JsonResult result = new JsonResult();
        if (comment == null || comment.trim().isEmpty() || comment.length() > 50) {
            result.setSuccess(false);
            result.setMessage("请求参数不合法");
            return result;
        }
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("用户凭证已过期，请重新登录");
        } else {
            if (!secretService.CheckSecretInfoExist(id)) {
                result.setSuccess(false);
                result.setMessage("该树洞消息不存在");
            } else {
                if (secretService.AddSecretComment(id, username, comment)) {
                    result.setSuccess(true);
                } else {
                    result.setSuccess(false);
                    result.setMessage("服务器异常，请稍候再试");
                }
            }
        }
        return result;
    }

    /**
     * 更改点赞状态
     *
     * @param request
     * @param id
     * @param like
     * @return
     */
    @RequestMapping(value = "/rest/secret/id/{id}/like", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult UpdateSecretLikeState(HttpServletRequest request
            , @PathVariable("id") int id, int like) {
        JsonResult result = new JsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (username == null || username.trim().isEmpty()) {
            result.setSuccess(false);
            result.setMessage("用户凭证已过期，请重新登录");
        } else {
            if (secretService.CheckSecretInfoExist(id)) {
                switch (like) {
                    case 0:
                        //取消点赞
                        if (secretService.ChangeUserLikeState(false, id, username)) {
                            result.setSuccess(true);
                        } else {
                            result.setSuccess(false);
                            result.setMessage("服务器异常，请稍候再试");
                        }
                        break;

                    case 1:
                        //点赞
                        if (secretService.ChangeUserLikeState(true, id, username)) {
                            result.setSuccess(true);
                        } else {
                            result.setSuccess(false);
                            result.setMessage("服务器异常，请稍候再试");
                        }
                        break;

                    default:
                        result.setSuccess(false);
                        result.setMessage("请求参数不合法");
                        break;
                }
            } else {
                result.setSuccess(false);
                result.setMessage("该树洞信息不存在");
            }
        }
        return result;
    }
}
