package cn.gdeiassistant.common.pojo.Config;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Cloudflare R2 对象存储配置（S3 兼容 API）。
 * 配置文件：resources 根目录 r2.properties 或 application.yml 中 r2.* 配置
 */
@Component
public class R2Config {

    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String customDomain;

    @Value("${r2.endpoint:}")
    public void setEndpoint(String endpoint) {
        this.endpoint = StringUtils.isNotBlank(endpoint) ? endpoint.trim() : null;
    }

    @Value("${r2.accessKeyId:}")
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = StringUtils.isNotBlank(accessKeyId) ? accessKeyId.trim() : null;
    }

    @Value("${r2.secretAccessKey:}")
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = StringUtils.isNotBlank(secretAccessKey) ? secretAccessKey.trim() : null;
    }

    @Value("${r2.bucketName:}")
    public void setBucketName(String bucketName) {
        this.bucketName = StringUtils.isNotBlank(bucketName) ? bucketName.trim() : null;
    }

    @Value("${r2.customDomain:}")
    public void setCustomDomain(String customDomain) {
        this.customDomain = StringUtils.isNotBlank(customDomain) ? customDomain.trim() : null;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public String getSecretAccessKey() {
        return secretAccessKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getCustomDomain() {
        return customDomain;
    }

    /** 配置是否完整可用 */
    public boolean isEnabled() {
        return StringUtils.isNotBlank(endpoint)
                && StringUtils.isNotBlank(accessKeyId)
                && StringUtils.isNotBlank(secretAccessKey)
                && StringUtils.isNotBlank(bucketName);
    }
}
