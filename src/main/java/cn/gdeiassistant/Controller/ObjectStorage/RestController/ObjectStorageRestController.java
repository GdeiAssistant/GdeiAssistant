package cn.gdeiassistant.Controller.ObjectStorage.RestController;

import cn.gdeiassistant.Pojo.OssToken.OssFederationToken;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.OpenAPI.ObjectStorage.ObjectStorageService;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ObjectStorageRestController {

    @Autowired
    private ObjectStorageService objectStorageService;

    @RequestMapping("/rest/osstoken")
    public DataJsonResult<OssFederationToken> ObtainOssFederationToken() throws ClientException {
        OssFederationToken ossFederationToken = objectStorageService.ObtainOssFederationToken();
        return new DataJsonResult<>(true, ossFederationToken);
    }

}
