package cn.gdeiassistant.core.objectStorage.controller;

import cn.gdeiassistant.common.annotation.RestAuthentication;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
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
