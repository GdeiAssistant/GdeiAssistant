package cn.gdeiassistant.Service.OpenAPI.ObjectStorage;

import cn.gdeiassistant.Pojo.OssToken.OssFederationToken;
import cn.gdeiassistant.Tools.SpringUtils.AliyunOSSUtils;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ObjectStorageService {

    @Autowired
    private AliyunOSSUtils aliyunOssUtils;

    public OssFederationToken ObtainOssFederationToken() throws ClientException {
        return aliyunOssUtils.ObtainOssFederationToken();
    }
}
