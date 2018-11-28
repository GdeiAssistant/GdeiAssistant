package com.linguancheng.gdeiassistant.Controller.Secret;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Secret;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Secret.SecretService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/11/24
 */

@RestController
public class SecretRestController {

    @Autowired
    private SecretService secretService;

    /**
     * 获取更多的树洞消息
     *
     * @param start
     * @return
     */
    @RequestMapping(value = "/api/secret/info/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<Secret>> GetMoreSecret(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        List<Secret> secretList = secretService.GetSecretInfo(start, size, username);
        return new DataJsonResult<>(true, secretList);
    }

    /**
     * 发布树洞消息
     *
     * @param request
     * @param secret
     * @return
     */
    @RequestMapping(value = "/api/secret/info", method = RequestMethod.POST)
    public JsonResult AddSecretInfo(HttpServletRequest request, @Validated Secret secret) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        secretService.AddSecretInfo(username, secret);
        return new JsonResult(true);
    }

    /**
     * 添加树洞评论
     *
     * @param request
     * @param id
     * @param comment
     * @return
     */
    @RequestMapping(value = "/api/secret/id/{id}/comment", method = RequestMethod.POST)
    public JsonResult AddSecretComment(HttpServletRequest request, @PathVariable("id") int id
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        if (!secretService.CheckSecretInfoExist(id)) {
            throw new DataNotExistException("查询的校园树洞信息不存在");
        }
        secretService.AddSecretComment(id, username, comment);
        return new JsonResult(true);
    }

    /**
     * 更改点赞状态
     *
     * @param request
     * @param id
     * @param like
     * @return
     */
    @RequestMapping(value = "/api/secret/id/{id}/like", method = RequestMethod.POST)
    public JsonResult UpdateSecretLikeState(HttpServletRequest request
            , @PathVariable("id") int id, @Validated @Range(min = 0, max = 1) int like) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        if (secretService.CheckSecretInfoExist(id)) {
            switch (like) {
                case 0:
                    //取消点赞
                    secretService.ChangeUserLikeState(false, id, username);
                    return new JsonResult(true);

                case 1:
                    //点赞
                    secretService.ChangeUserLikeState(true, id, username);
                    return new JsonResult(true);

                default:
                    return new JsonResult(false, "请求参数不合法");
            }
        }
        throw new DataNotExistException("查询的校园树洞信息不存在");
    }
}
