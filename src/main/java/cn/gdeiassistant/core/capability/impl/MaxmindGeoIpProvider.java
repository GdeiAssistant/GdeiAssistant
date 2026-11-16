package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ServiceProvider;
import com.maxmind.db.Reader;
import jakarta.annotation.PreDestroy;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.List;
import java.util.Map;

@Component
public class MaxmindGeoIpProvider implements ServiceProvider<String, IPAddressRecord>, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(MaxmindGeoIpProvider.class);

    private final Reader reader;

    public MaxmindGeoIpProvider(@Value("${MAXMIND_DATABASE_PATH:${maxmind.database.path:/data/GeoLite2-City.mmdb}}")
                                   String databasePath) {
        this.reader = openCityDatabase(new File(databasePath));
    }

    private static Reader openCityDatabase(File database) {
        if (!database.isFile()) {
            log.warn("MaxMind City database not found at {}", database);
            return null;
        }
        try {
            Reader reader = new Reader(database);
            String type = reader.getMetadata().databaseType();
            if (!"GeoLite2-City".equals(type) && !"GeoIP2-City".equals(type)) {
                reader.close();
                log.warn("Unsupported MaxMind database type: {}", type);
                return null;
            }
            log.info("Loaded local MaxMind City database from {}", database);
            return reader;
        } catch (IOException e) {
            log.warn("Unable to open MaxMind City database at {}: {}", database, e.getMessage());
            return null;
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
        String address = ip == null ? "" : ip.trim();
        if (reader == null || !(InetAddressUtils.isIPv4Address(address)
                || InetAddressUtils.isIPv6Address(address)
                || InetAddressUtils.isIPv4MappedIPv64Address(address))) {
            throw new ProviderException(Messages.get("capability.maxmindLookupFailed"));
        }
        try {
            Map<?, ?> result = reader.get(InetAddress.getByName(address), Map.class);
            if (result == null) {
                throw new ProviderException(Messages.get("capability.maxmindLookupFailed"));
            }
            Object country = result.get("country");
            Object subdivision = result.get("subdivisions") instanceof List<?> subdivisions && !subdivisions.isEmpty()
                    ? subdivisions.get(0) : null;

            IPAddressRecord record = new IPAddressRecord();
            record.setCountry(country instanceof Map<?, ?> fields && "CN".equals(fields.get("iso_code"))
                    ? "中国" : localizedName(country));
            record.setProvince(localizedName(subdivision));
            record.setCity(localizedName(result.get("city")));
            record.setNetwork("-");
            record.setArea("-");
            return record;
        } catch (IOException e) {
            throw new ProviderException(Messages.get("capability.maxmindLookupFailed"), e);
        }
    }

    private static String localizedName(Object location) {
        if (location instanceof Map<?, ?> fields && fields.get("names") instanceof Map<?, ?> names) {
            if (names.get("zh-CN") instanceof String chinese && !chinese.isBlank()) {
                return chinese;
            }
            if (names.get("en") instanceof String english && !english.isBlank()) {
                return english;
            }
        }
        return "-";
    }

    @Override
    public boolean isConfigured() {
        return reader != null;
    }

    @PreDestroy
    @Override
    public void close() throws IOException {
        if (reader != null) {
            reader.close();
        }
    }
}
