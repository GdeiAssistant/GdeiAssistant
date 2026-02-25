package cn.gdeiassistant.core.objectStorage.controller;

import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObjectStorageController {

    /**
     * 兼容旧版 OSS STS 临时令牌接口，占位空壳。
     * 返回固定错误，提示客户端改用服务端统一上传接口。
     */
    @RequestMapping("/rest/osstoken")
    public JsonResult ObtainOssToken() {
        return new JsonResult(false, "存储架构已升级为R2，不再提供STS临时令牌，请走服务端统一上传接口");
    }

}
