package cn.gdeiassistant.Service.OpenAPI.ObjectStorage;

import cn.gdeiassistant.Pojo.OssToken.OssFederationToken;
import cn.gdeiassistant.Tools.SpringUtils.AliYunOSSUtils;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageService {

    @Autowired
    private AliYunOSSUtils aliyunOssUtils;

    public OssFederationToken ObtainOssFederationToken() throws ClientException {
        return aliyunOssUtils.ObtainOssFederationToken();
    }
}
