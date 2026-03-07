package cn.gdeiassistant.core.capability.impl;

import cn.gdeiassistant.common.pojo.Entity.IPAddressRecord;
import cn.gdeiassistant.core.capability.ip.IPLocationResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class FreeIpLocationResolver implements IPLocationResolver {

    @Autowired
    private RestTemplate restTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public IPAddressRecord resolve(String ip) {
        // primary: ip-api（免费，无需实名认证）
        IPAddressRecord record = queryIpApi(ip);
        if (record != null) {
            return record;
        }
        // fallback: ipwho.is（免费，无需实名认证）
        record = queryIpwhois(ip);
        if (record != null) {
            return record;
        }
        return fallbackRecord();
    }

    private IPAddressRecord queryIpApi(String ip) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    "http://ip-api.com/json/" + ip + "?fields=status,country,regionName,city,isp", Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && "success".equals(String.valueOf(body.get("status")))) {
                IPAddressRecord r = new IPAddressRecord();
                r.setCountry(valueOf(body.get("country")));
                r.setProvince(valueOf(body.get("regionName")));
                r.setCity(valueOf(body.get("city")));
                r.setNetwork(valueOf(body.get("isp")));
                r.setArea("-");
                return normalize(r);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private IPAddressRecord queryIpwhois(String ip) {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity("https://ipwho.is/" + ip, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && Boolean.TRUE.equals(body.get("success"))) {
                IPAddressRecord r = new IPAddressRecord();
                r.setCountry(valueOf(body.get("country")));
                r.setProvince(valueOf(body.get("region")));
                r.setCity(valueOf(body.get("city")));
                r.setNetwork(valueOf(body.get("connection")));
                r.setArea("-");
                return normalize(r);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private IPAddressRecord normalize(IPAddressRecord r) {
        if (r.getCountry() == null || r.getCountry().isEmpty()) r.setCountry("-");
        if (r.getProvince() == null || r.getProvince().isEmpty()) r.setProvince("-");
        if (r.getCity() == null || r.getCity().isEmpty()) r.setCity("-");
        if (r.getNetwork() == null || r.getNetwork().isEmpty()) r.setNetwork("-");
        if (r.getArea() == null || r.getArea().isEmpty()) r.setArea("-");
        return r;
    }

    private String valueOf(Object o) {
        if (o == null) return "-";
        if (o instanceof Map) return "-";
        return String.valueOf(o);
    }

    private IPAddressRecord fallbackRecord() {
        IPAddressRecord record = new IPAddressRecord();
        record.setNetwork("-");
        record.setCountry("-");
        record.setProvince("-");
        record.setCity("-");
        record.setArea("-");
        return record;
    }
}
