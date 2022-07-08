package cn.gdeiassistant.Tools.SpringUtils;

import cn.gdeiassistant.Enum.Module.ModuleEnum;
import cn.gdeiassistant.Pojo.Config.OSSConfig;
import cn.gdeiassistant.Pojo.Config.OssFederationTokenConfig;
import cn.gdeiassistant.Pojo.OssToken.OssFederationToken;
import com.aliyun.oss.OSSClient;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.http.ProtocolType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sts.model.v20150401.AssumeRoleRequest;
import com.aliyuncs.sts.model.v20150401.AssumeRoleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class AliyunOSSUtils {

    @Autowired
    private ModuleUtils moduleUtils;

    @Autowired
    private OSSConfig ossConfig;

    @Autowired
    private OssFederationTokenConfig ossFederationTokenConfig;

    /**
     * 生成OSS对象临时访问URL
     *
     * @param bucket
     * @param key
     * @param expire
     * @param unit
     * @return
     */
    public String GeneratePresignedUrl(String bucket, String key, long expire, TimeUnit unit) {
        if (moduleUtils.CheckModuleState(ModuleEnum.OSS)) {
            // 创建OSSClient实例
            OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyID()
                    , ossConfig.getAccessKeySecret());
            String url = "";
            //检查自定义头像图片是否存在
            if (ossClient.doesObjectExist(bucket, key)) {
                //设置过期时间
                Date expiration = new Date(new Date().getTime() + unit.convert(expire, TimeUnit.MICROSECONDS));
                // 生成URL
                url = ossClient.generatePresignedUrl(bucket, key, expiration).toString()
                        .replace("http", "https");
            }
            ossClient.shutdown();
            return url;
        }
        return "";
    }

    /**
     * 上传OSS对象文件
     *
     * @param bucket
     * @param key
     * @param inputStream
     */
    public void UploadOSSObject(String bucket, String key, InputStream inputStream) {
        if (moduleUtils.CheckModuleState(ModuleEnum.OSS)) {
            if (inputStream != null) {
                //创建OSSClient实例
                OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyID()
                        , ossConfig.getAccessKeySecret());
                //上传文件
                ossClient.putObject(bucket, key, inputStream);
                ossClient.shutdown();
            }
        }
    }

    /**
     * 下载OSS对象文件
     *
     * @param bucket
     * @param key
     * @return
     */
    public InputStream DownloadOSSObject(String bucket, String key) {
        //创建OSSClient实例
        OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyID()
                , ossConfig.getAccessKeySecret());
        if (ossClient.doesObjectExist(bucket, key)) {
            InputStream inputStream = ossClient.getObject(bucket, key).getObjectContent();
            ossClient.shutdown();
            return inputStream;
        }
        return null;
    }

    /**
     * 删除OSS对象文件
     *
     * @param bucket
     * @param key
     */
    public void DeleteOSSObject(String bucket, String key) {
        if (moduleUtils.CheckModuleState(ModuleEnum.OSS)) {
            //创建OSSClient实例
            OSSClient ossClient = new OSSClient(ossConfig.getEndpoint(), ossConfig.getAccessKeyID()
                    , ossConfig.getAccessKeySecret());
            if (ossClient.doesObjectExist(bucket, key)) {
                //删除文件
                ossClient.deleteObject(bucket, key);
            }
            ossClient.shutdown();
        }
    }

    public AssumeRoleResponse AssumeRole(String accessKeyId, String accessKeySecret, String
            roleArn, String roleSessionName, String policy, ProtocolType protocolType, long durationSeconds) throws ClientException {

        //创建一个AliyunAcsClient,用于发起OpenAPI请求
        IClientProfile profile = DefaultProfile.getProfile(ossConfig.getRegion(), accessKeyId, accessKeySecret);
        DefaultAcsClient client = new DefaultAcsClient(profile);

        //创建一个AssumeRoleRequest并设置请求参数
        AssumeRoleRequest request = new AssumeRoleRequest();
        request.setVersion(ossConfig.getVersion());
        request.setMethod(MethodType.POST);
        request.setProtocol(protocolType);
        request.setRoleArn(roleArn);
        request.setRoleSessionName(roleSessionName);
        request.setPolicy(policy);
        request.setDurationSeconds(durationSeconds);

        //发起请求并得到response,返回response
        return client.getAcsResponse(request);
    }

    public OssFederationToken ObtainOssFederationToken() throws ClientException {
        String roleSessionName = "GdeiAssistant";
        ProtocolType protocolType = ProtocolType.HTTPS;
        OssFederationToken ossFederationToken = new OssFederationToken();
        AssumeRoleResponse assumeRoleResponse = AssumeRole(ossFederationTokenConfig.getAccessKeyID()
                , ossFederationTokenConfig.getAccessKeySecret(), ossFederationTokenConfig.getRoleArn()
                , roleSessionName, ossFederationTokenConfig.getPolicy()
                , protocolType, ossFederationTokenConfig.getTokenExpireTime());
        ossFederationToken.setStatus("200");
        ossFederationToken.setAccessKeyId(assumeRoleResponse.getCredentials().getAccessKeyId());
        ossFederationToken.setAccessKeySecret(assumeRoleResponse.getCredentials().getAccessKeySecret());
        ossFederationToken.setSecurityToken(assumeRoleResponse.getCredentials().getSecurityToken());
        ossFederationToken.setExpiration(assumeRoleResponse.getCredentials().getExpiration());
        return ossFederationToken;
    }

}
