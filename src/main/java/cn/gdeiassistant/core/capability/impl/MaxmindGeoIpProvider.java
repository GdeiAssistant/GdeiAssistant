package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.InetAddress;

/**
 * MaxMind GeoLite2 本地 IP 地理定位提供商。
 * 使用本地数据库文件实现零外部依赖的 IP 定位。
 *
 * 首次使用前需下载 GeoLite2-City.mmdb：
 *   wget https://git.io/GeoLite2-City.mmdb -O /data/GeoLite2-City.mmdb
 *
 * 免费注册获取 License Key：
 *   https://dev.maxmind.com/geoip/geolite2-free-geolocation-data
 */
@Component
public class MaxmindGeoIpProvider implements ServiceProvider<String, IPAddressRecord> {

    private static final Logger log = LoggerFactory.getLogger(MaxmindGeoIpProvider.class);

    private String databasePath;

    private boolean initialized = false;
    private boolean dbAvailable = false;

    @Value("${maxmind.database.path:/data/GeoLite2-City.mmdb}")
    public void setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
        initDatabase();
    }

    private synchronized void initDatabase() {
        if (initialized) return;
        initialized = true;
        try {
            File dbFile = new File(databasePath);
            if (dbFile.exists() && dbFile.isFile()) {
                dbAvailable = true;
                log.info("MaxMind GeoLite2 数据库已加载：{}", databasePath);
            } else {
                log.warn("MaxMind GeoLite2 数据库未找到：{}（IP 定位将跳过此提供商）", databasePath);
            }
        } catch (Exception e) {
            log.warn("MaxMind GeoLite2 初始化失败：{}", e.getMessage());
        }
    }

    @Override
    public String providerName() {
        return "maxmind-geolite2";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public IPAddressRecord execute(String ip) throws ProviderException {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            // 使用 com.maxmind.geoip2 库查询
            // 注意：需要在 build.gradle 中添加依赖：
            // implementation 'com.maxmind.geoip2:geoip2:4.2.0'
            //
            // 示例代码（取消注释后使用）：
            // File database = new File(databasePath);
            // DatabaseReader reader = new DatabaseReader.Builder(database).build();
            // CityResponse response = reader.city(addr);
            //
            // IPAddressRecord record = new IPAddressRecord();
            // record.setCountry(response.getCountry().getName() != null
            //         ? response.getCountry().getName() : "-");
            // record.setProvince(response.getMostSpecificSubdivision().getName() != null
            //         ? response.getMostSpecificSubdivision().getName() : "-");
            // record.setCity(response.getCity().getName() != null
            //         ? response.getCity().getName() : "-");
            // record.setNetwork("-");
            // record.setArea(response.getContinent().getName() != null
            //         ? response.getContinent().getName() : "-");
            // record.setIp(ip);
            // return record;

            throw new ProviderException("MaxMind GeoLite2 尚未集成：需添加 com.maxmind.geoip2:geoip2 依赖");
        } catch (Exception e) {
            throw new ProviderException("MaxMind GeoLite2 查询失败", e);
        }
    }

    @Override
    public boolean isConfigured() {
        return dbAvailable;
    }

    @Override
    public boolean isHealthy() {
        return dbAvailable;
    }
}
