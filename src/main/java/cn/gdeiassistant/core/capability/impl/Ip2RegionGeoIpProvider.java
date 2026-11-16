package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;
import jakarta.annotation.PreDestroy;
import org.apache.http.conn.util.InetAddressUtils;
import org.lionsoul.ip2region.service.Config;
import org.lionsoul.ip2region.service.InvalidConfigException;
import org.lionsoul.ip2region.service.Ip2Region;
import org.lionsoul.ip2region.xdb.InetAddressException;
import org.lionsoul.ip2region.xdb.Searcher;
import org.lionsoul.ip2region.xdb.XdbException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

@Component
public class Ip2RegionGeoIpProvider implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(Ip2RegionGeoIpProvider.class);

    private final Ip2Region searcher;

    @Autowired
    public Ip2RegionGeoIpProvider(@Value("${IP2REGION_IPV4_PATH:/data/ip2region_v4.xdb}") String ipv4Path,
                                 @Value("${IP2REGION_IPV6_PATH:/data/ip2region_v6.xdb}") String ipv6Path) {
        this(openDatabases(ipv4Path, ipv6Path));
    }

    Ip2RegionGeoIpProvider(Ip2Region searcher) {
        this.searcher = searcher;
    }

    private static Ip2Region openDatabases(String ipv4Path, String ipv6Path) {
        Config ipv4 = loadConfig(ipv4Path, false);
        Config ipv6 = loadConfig(ipv6Path, true);
        if (ipv4 == null && ipv6 == null) {
            return null;
        }
        try {
            Ip2Region result = Ip2Region.create(ipv4, ipv6);
            log.info("Loaded local ip2region databases (IPv4={}, IPv6={})", ipv4 != null, ipv6 != null);
            return result;
        } catch (IOException e) {
            log.warn("Unable to start local ip2region search: {}", e.getClass().getSimpleName());
            return null;
        }
    }

    private static Config loadConfig(String path, boolean ipv6) {
        File database = new File(path);
        if (!database.isFile()) {
            return null;
        }
        try {
            try (var file = new RandomAccessFile(database, "r")) {
                var header = Searcher.loadHeader(file);
                Searcher.verify(header, file.length());
                int family = header.version == Searcher.STRUCTURE_20 ? 4 : header.ipVersion;
                if (family != (ipv6 ? 6 : 4)) {
                    log.warn("Wrong IP family in local ip2region database {}", database);
                    return null;
                }
            }
            var config = Config.custom().setXdbFile(database).setCachePolicy(Config.VIndexCache).setSearchers(4);
            return ipv6 ? config.asV6() : config.asV4();
        } catch (IOException | XdbException | InvalidConfigException e) {
            log.warn("Unable to load local ip2region database {}: {}", database, e.getClass().getSimpleName());
            return null;
        }
    }

    public IPAddressRecord lookup(String ip) throws ProviderException {
        String address = ip == null ? "" : ip.trim();
        if (searcher == null || !(InetAddressUtils.isIPv4Address(address)
                || InetAddressUtils.isIPv6Address(address)
                || InetAddressUtils.isIPv4MappedIPv64Address(address))) {
            throw new ProviderException(Messages.get("capability.localIpLookupFailed"));
        }

        String region;
        try {
            region = searcher.search(address);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ProviderException(Messages.get("capability.localIpLookupFailed"), e);
        } catch (IOException | InetAddressException e) {
            throw new ProviderException(Messages.get("capability.localIpLookupFailed"), e);
        }

        String[] fields = region == null ? new String[0] : region.split("\\|", -1);
        if (fields.length != 5 || "-".equals(locationField(fields[0]))) {
            throw new ProviderException(Messages.get("capability.localIpLookupFailed"));
        }

        IPAddressRecord record = new IPAddressRecord();
        record.setCountry("CN".equals(fields[4]) ? "中国" : fields[0]);
        record.setProvince(locationField(fields[1]));
        record.setCity(locationField(fields[2]));
        record.setNetwork(locationField(fields[3]));
        record.setArea("-");
        return record;
    }

    private static String locationField(String value) {
        return value.isBlank() || "0".equals(value) ? "-" : value;
    }

    public boolean isConfigured() {
        return searcher != null;
    }

    @PreDestroy
    @Override
    public void close() throws InterruptedException {
        if (searcher != null) {
            searcher.close();
        }
    }
}
