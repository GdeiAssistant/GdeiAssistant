package com.linguancheng.gdeiassistant.Controller.Secret;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import com.linguancheng.gdeiassistant.Pojo.Entity.Secret;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Secret.SecretService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SecretRestController {

    private final int MAX_VOICE_SIZE = 1024 * 1024 * 1;

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
     * @param file
     * @return
     */
    @RequestMapping(value = "/api/secret/info", method = RequestMethod.POST)
    public JsonResult AddSecretInfo(HttpServletRequest request, @Validated Secret secret
            , @RequestParam(value = "voice", required = false) MultipartFile file) throws Exception {
        if (secret.getType() == 0 && StringUtils.isBlank(secret.getContent())) {
            return new JsonResult(false, "树洞信息不能为空");
        }
        String username = (String) request.getSession().getAttribute("username");
        if (secret.getType() == 0) {
            //文字树洞信息
            secretService.AddSecretInfo(username, secret);
            return new JsonResult(true);
        } else if (secret.getType() == 1) {
            //语音树洞信息
            if (file == null || file.isEmpty() || file.getSize() == 0) {
                return new JsonResult(false, "语音内容不能为空");
            } else if (file.getSize() > MAX_VOICE_SIZE) {
                return new JsonResult(false, "语音文件大小过大");
            } else {
                //插入树洞信息记录
                Integer id = secretService.AddSecretInfo(username, secret);
                //上传录音文件
                secretService.UploadVoiceSecret(id, file.getInputStream());
                return new JsonResult(true);
            }
        } else {
            return new JsonResult(false, "树洞信息类型不合法");
        }
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
