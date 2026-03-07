package cn.gdeiassistant.common.pojo.Config;

import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

/**
 * Cloudflare R2 对象存储配置（S3 兼容 API）。
 * 从 application.yml 的 r2.* 读取配置，并按需创建 S3 Client / Presigner。
 */
@Configuration
public class R2Config {

    private String endpoint;
    private String accessKeyId;
    private String secretAccessKey;
    private String bucketName;
    private String customDomain;

    @Value("${r2.endpoint:}")
    public void setEndpoint(String endpoint) {
        this.endpoint = normalize(endpoint);
    }

    @Value("${r2.accessKeyId:}")
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = normalize(accessKeyId);
    }

    @Value("${r2.secretAccessKey:}")
    public void setSecretAccessKey(String secretAccessKey) {
        this.secretAccessKey = normalize(secretAccessKey);
    }

    @Value("${r2.bucketName:}")
    public void setBucketName(String bucketName) {
        this.bucketName = normalize(bucketName);
    }

    @Value("${r2.customDomain:}")
    public void setCustomDomain(String customDomain) {
        this.customDomain = normalize(customDomain);
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${r2.endpoint:}') and "
            + "T(org.springframework.util.StringUtils).hasText('${r2.accessKeyId:}') and "
            + "T(org.springframework.util.StringUtils).hasText('${r2.secretAccessKey:}') and "
            + "T(org.springframework.util.StringUtils).hasText('${r2.bucketName:}')")
    public S3Client r2S3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .chunkedEncodingEnabled(false)
                        .build())
                .build();
    }

    @Bean(destroyMethod = "close")
    @ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${r2.endpoint:}') and "
            + "T(org.springframework.util.StringUtils).hasText('${r2.accessKeyId:}') and "
            + "T(org.springframework.util.StringUtils).hasText('${r2.secretAccessKey:}') and "
            + "T(org.springframework.util.StringUtils).hasText('${r2.bucketName:}')")
    public S3Presigner r2S3Presigner() {
        return S3Presigner.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKeyId, secretAccessKey)))
                .region(Region.of("auto"))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .build();
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

    public boolean isEnabled() {
        return StringUtils.isNotBlank(endpoint)
                && StringUtils.isNotBlank(accessKeyId)
                && StringUtils.isNotBlank(secretAccessKey)
                && StringUtils.isNotBlank(bucketName);
    }

    private String normalize(String value) {
        return StringUtils.isNotBlank(value) ? value.trim() : null;
    }
}
