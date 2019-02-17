package edu.gdei.gdeiassistant.Service.OssToken;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OssFederationTokenService {

    private String region;
    private String version;

    @Value("#{propertiesReader['api.region']}")
    public void setRegion(String region) {
        this.region = region;
    }

    @Value("#{propertiesReader['api.version']}")
    public void setVersion(String version) {
        this.version = version;
    }

    public AssumeRoleResponse AssumeRole(String accessKeyId, String accessKeySecret, String
            roleArn, String roleSessionName, String policy, ProtocolType protocolType, long durationSeconds) throws ClientException {

        //创建一个AliyunAcsClient,用于发起OpenAPI请求
        IClientProfile profile = DefaultProfile.getProfile(region, accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        //创建一个AssumeRoleRequest并设置请求参数
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setVersion(version);
        request.setMethod(MethodType.POST);
        request.setProtocol(protocolType);
        request.setRoleArn(roleArn);
        request.setRoleSessionName(roleSessionName);
        request.setPolicy(policy);
        request.setDurationSeconds(durationSeconds);

        //发起请求并得到response,返回response
        return client.getAcsResponse(request);
    }
}
