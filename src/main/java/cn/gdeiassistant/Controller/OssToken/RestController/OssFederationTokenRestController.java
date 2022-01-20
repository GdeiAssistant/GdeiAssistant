package cn.gdeiassistant.Controller.OssToken.RestController;

import cn.gdeiassistant.Pojo.Config.OssFederationTokenConfig;
import cn.gdeiassistant.Pojo.OssToken.OssFederationToken;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.OssToken.OssFederationTokenService;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OssFederationTokenRestController {

    @Autowired
    private OssFederationTokenConfig ossFederationTokenConfig;

    @Autowired
    private OssFederationTokenService ossFederationTokenService;

    @RequestMapping("/rest/osstoken")
    public DataJsonResult<OssFederationToken> ObtainOssFederationToken() throws ClientException {
        String roleSessionName = "GdeiAssistant";
        ProtocolType protocolType = ProtocolType.HTTPS;
        OssFederationToken ossFederationToken = new OssFederationToken();
        AssumeRoleResponse assumeRoleResponse = ossFederationTokenService.AssumeRole(ossFederationTokenConfig.getAccessKeyID()
                , ossFederationTokenConfig.getAccessKeySecret(), ossFederationTokenConfig.getRoleArn()
                , roleSessionName, ossFederationTokenConfig.getPolicy()
                , protocolType, ossFederationTokenConfig.getTokenExpireTime());
        ossFederationToken.setStatus("200");
        ossFederationToken.setAccessKeyId(assumeRoleResponse.getCredentials().getAccessKeyId());
        ossFederationToken.setAccessKeySecret(assumeRoleResponse.getCredentials().getAccessKeySecret());
        ossFederationToken.setSecurityToken(assumeRoleResponse.getCredentials().getSecurityToken());
        ossFederationToken.setExpiration(assumeRoleResponse.getCredentials().getExpiration());
        return new DataJsonResult<>(true, ossFederationToken);
    }

}
