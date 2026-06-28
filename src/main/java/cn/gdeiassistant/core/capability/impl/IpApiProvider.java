package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.exception.ProviderException;
import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class IpApiProvider implements ServiceProvider<String, IPAddressRecord> {

    private static final Logger log = LoggerFactory.getLogger(IpApiProvider.class);

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public String providerName() {
        return "ip-api";
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public IPAddressRecord execute(String ip) throws ProviderException {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "http://ip-api.com/json/" + ip + "?fields=status,country,regionName,city,isp",
                    Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && "success".equals(String.valueOf(body.get("status")))) {
                IPAddressRecord record = new IPAddressRecord();
                record.setCountry(nullSafe(body.get("country")));
                record.setProvince(nullSafe(body.get("regionName")));
                record.setCity(nullSafe(body.get("city")));
                record.setNetwork(nullSafe(body.get("isp")));
                record.setArea("-");
                normalize(record);
                return record;
            }
            throw new ProviderException("ip-api.com 查询失败：status 不为 success");
        } catch (ProviderException e) {
            throw e;
        } catch (Exception e) {
            throw new ProviderException("ip-api.com 查询异常：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    private String nullSafe(Object value) {
        if (value == null) return "-";
        if (value instanceof Map) return "-";
        return String.valueOf(value);
    }

    private void normalize(IPAddressRecord record) {
        if (record.getCountry() == null || record.getCountry().isEmpty()) record.setCountry("-");
        if (record.getProvince() == null || record.getProvince().isEmpty()) record.setProvince("-");
        if (record.getCity() == null || record.getCity().isEmpty()) record.setCity("-");
        if (record.getNetwork() == null || record.getNetwork().isEmpty()) record.setNetwork("-");
        if (record.getArea() == null || record.getArea().isEmpty()) record.setArea("-");
    }
}
