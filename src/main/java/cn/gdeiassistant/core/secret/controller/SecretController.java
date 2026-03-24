package cn.gdeiassistant.core.secret.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.secret.pojo.dto.SecretPublishDTO;
import cn.gdeiassistant.core.secret.pojo.vo.SecretCommentVO;
import cn.gdeiassistant.core.secret.pojo.vo.SecretVO;
import cn.gdeiassistant.core.secret.service.SecretService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.List;

@RestController
public class SecretController {

    @Autowired
    private SecretService secretService;

    @RequestMapping(value = "/api/secret/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<SecretVO> getSecretDetail(HttpServletRequest request, @PathVariable("id") int id) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        SecretVO vo = secretService.getSecretDetailInfo(id, sessionId);
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/secret/id/{id}/comments", method = RequestMethod.GET)
    public DataJsonResult<List<SecretCommentVO>> getSecretComments(@PathVariable("id") int id) throws Exception {
        if (!secretService.checkSecretInfoExist(id)) {
            throw new DataNotExistException("查询的校园树洞信息不存在");
        }
        List<SecretCommentVO> list = secretService.getSecretComments(id);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/secret/info/start/{start}/size/{size}", method = RequestMethod.GET)
    public DataJsonResult<List<SecretVO>> getMoreSecret(HttpServletRequest request
            , @PathVariable("start") int start, @PathVariable("size") int size) throws Exception {
        if (size > 50) size = 50; // Cap page size
        String sessionId = (String) request.getAttribute("sessionId");
        List<SecretVO> list = secretService.getSecretInfo(start, size, sessionId);
        return new DataJsonResult<>(true, list);
    }

    // TODO(perf): unpaged — see docs/ops/unbounded-endpoints-inventory
    @RequestMapping(value = "/api/secret/profile", method = RequestMethod.GET)
    public DataJsonResult<List<SecretVO>> getMySecrets(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        List<SecretVO> list = secretService.getSecretInfo(sessionId);
        return new DataJsonResult<>(true, list);
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/secret/info", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addSecretInfo(HttpServletRequest request, @Validated SecretPublishDTO dto
            , @RequestParam(value = "voice", required = false) MultipartFile file
            , @RequestParam(value = "voiceKey", required = false) String voiceKey) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        if (dto.getType() != null && dto.getType() == 0 && StringUtils.isBlank(dto.getContent())) {
            return new JsonResult(false, "树洞信息不能为空");
        }
        if (dto.getType() != null && dto.getType().equals(0)) {
            secretService.addSecretInfo(sessionId, dto);
            return new JsonResult(true);
        } else if (dto.getType() != null && dto.getType().equals(1)) {
            if ((file == null || file.isEmpty() || file.getSize() == 0) && StringUtils.isBlank(voiceKey)) {
                return new JsonResult(false, "语音内容不能为空");
            } else if (file != null && file.getSize() > ValueConstantUtils.MAX_VOICE_SIZE) {
                return new JsonResult(false, "语音文件大小过大");
            } else {
                Integer id = secretService.addSecretInfo(sessionId, dto);
                try {
                    if (file != null && !file.isEmpty() && file.getSize() > 0) {
                        secretService.uploadVoiceSecret(id, file.getInputStream());
                    } else {
                        secretService.moveVoiceSecretFromTempObject(id, voiceKey);
                    }
                } catch (Exception e) {
                    secretService.deleteSecretVoice(id);
                    secretService.deleteSecretById(id);
                    return new JsonResult(false, "语音上传失败");
                }
                return new JsonResult(true);
            }
        } else {
            return new JsonResult(false, "树洞信息类型不合法");
        }
    }

    @RequestMapping(value = "/api/secret/id/{id}/comment", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addSecretComment(HttpServletRequest request, @PathVariable("id") int id
            , @Validated @NotBlank @Length(min = 1, max = 50) String comment) throws Exception {
        if (!secretService.checkSecretInfoExist(id)) {
            throw new DataNotExistException("查询的校园树洞信息不存在");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        secretService.addSecretComment(id, sessionId, comment);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/secret/id/{id}/like", method = RequestMethod.POST)
    public JsonResult updateSecretLikeState(HttpServletRequest request
            , @PathVariable("id") int id, @Validated @Range(min = 0, max = 1) int like) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        if (secretService.checkSecretInfoExist(id)) {
            switch (like) {
                case 0:
                    secretService.changeUserLikeState(false, id, sessionId);
                    return new JsonResult(true);
                case 1:
                    secretService.changeUserLikeState(true, id, sessionId);
                    return new JsonResult(true);
                default:
                    return new JsonResult(false, "请求参数不合法");
            }
        }
        throw new DataNotExistException("查询的校园树洞信息不存在");
    }
}
