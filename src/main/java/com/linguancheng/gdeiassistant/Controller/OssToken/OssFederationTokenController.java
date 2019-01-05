package com.linguancheng.gdeiassistant.Controller.OssToken;

import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import com.linguancheng.gdeiassistant.Pojo.OssToken.OssFederationToken;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.OssToken.OssFederationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Controller
public class OssFederationTokenController {

    @Autowired
    private OssFederationTokenService ossFederationTokenService;

    private String accessKeyID;
    private String accessKeySecret;
    private String roleArn;
    private long tokenExpireTime;

    private String policy;

    @Value("#{propertiesReader['token.accessKeyID']}")
    public void setAccessKeyID(String accessKeyID) {
        this.accessKeyID = accessKeyID;
    }

    @Value("#{propertiesReader['token.accessKeySecret']}")
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    @Value("#{propertiesReader['token.roleArn']}")
    public void setRoleArn(String roleArn) {
        this.roleArn = roleArn;
    }

    @Value("#{propertiesReader['token.tokenExpireTime']}")
    public void setTokenExpireTime(String tokenExpireTime) {
        this.tokenExpireTime = Long.valueOf(tokenExpireTime);
    }

    @Value("#{propertiesReader['token.policyFile']}")
    public void setPolicy(String policyFileLocation) {
        InputStream inputStream = null;
        BufferedReader bufferedReader = null;
        try {
            inputStream = new ClassPathResource(policyFileLocation).getInputStream();
            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String temp;
            while ((temp = bufferedReader.readLine()) != null) {
                stringBuilder.append(temp);
            }
            policy = stringBuilder.toString();
        } catch (IOException e) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @RequestMapping("/rest/osstoken")
    @ResponseBody
    public DataJsonResult<OssFederationToken> ObtainOssFederationToken() throws ClientException {
        String roleSessionName = "GdeiAssistant";
        ProtocolType protocolType = ProtocolType.HTTPS;
        OssFederationToken ossFederationToken = new OssFederationToken();
        AssumeRoleResponse assumeRoleResponse = ossFederationTokenService.AssumeRole(accessKeyID
                , accessKeySecret, roleArn, roleSessionName, policy
                , protocolType, tokenExpireTime);
        ossFederationToken.setStatus("200");
        ossFederationToken.setAccessKeyId(assumeRoleResponse.getCredentials().getAccessKeyId());
        ossFederationToken.setAccessKeySecret(assumeRoleResponse.getCredentials().getAccessKeySecret());
        ossFederationToken.setSecurityToken(assumeRoleResponse.getCredentials().getSecurityToken());
        ossFederationToken.setExpiration(assumeRoleResponse.getCredentials().getExpiration());
        return new DataJsonResult<>(true, ossFederationToken);
    }
}
