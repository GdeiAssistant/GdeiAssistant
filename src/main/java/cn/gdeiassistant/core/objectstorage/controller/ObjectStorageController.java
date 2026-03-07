package cn.gdeiassistant.core.objectStorage.controller;

import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.objectStorage.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
public class ObjectStorageController {

    @Autowired
    private UploadService uploadService;

    /**
     * 兼容旧版 OSS STS 临时令牌接口，占位空壳。
     * 返回固定错误，提示客户端改用服务端统一上传接口。
     */
    @RequestMapping("/api/osstoken")
    public JsonResult obtainOssToken() {
        return new JsonResult(false, "存储架构已升级为 R2，不再提供 OSS STS 临时令牌，请改用 /api/upload/presignedUrl");
    }

    /**
     * 生成前端直传 Cloudflare R2 的预签名 PUT URL。
     */
    @RequestMapping(value = "/api/upload/presignedUrl", method = RequestMethod.GET)
    @RestAuthentication
    public DataJsonResult<Map<String, String>> getPresignedUploadUrl(HttpServletRequest request,
                                                                     @RequestParam("fileName") String fileName,
                                                                     @RequestParam("contentType") String contentType) {
        if (StringUtils.isBlank(fileName)) {
            return new DataJsonResult<>(new JsonResult(false, "fileName 不能为空"));
        }
        if (StringUtils.isBlank(contentType) || !contentType.contains("/")) {
            return new DataJsonResult<>(new JsonResult(false, "contentType 不合法"));
        }
        return new DataJsonResult<>(true, uploadService.createPresignedUpload(fileName, contentType));
    }

}
