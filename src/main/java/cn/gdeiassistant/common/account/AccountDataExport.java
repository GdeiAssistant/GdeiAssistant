package cn.gdeiassistant.common.account;

import cn.gdeiassistant.common.pojo.entity.City;
import cn.gdeiassistant.common.pojo.entity.Region;
import cn.gdeiassistant.common.pojo.entity.State;
import cn.gdeiassistant.common.tools.utils.LocationUtils;

import java.util.List;
import java.util.Map;

/**
 * The assembled, whitelisted data snapshot for one account export.
 *
 * <p>Collecting rows and choosing which fields a user may see stays in the export service: the
 * projection is a privacy boundary. Interpreting those fields is domain knowledge, so each
 * {@link AccountLifecycleContributor} decorates its own section here rather than teaching the
 * orchestrator which status array applies to which column.
 */
public final class AccountDataExport {

    private final Map<String, Object> data;

    public AccountDataExport(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> data() {
        return data;
    }

    /** Replaces integer status codes with their display labels for one exported section. */
    @SuppressWarnings("unchecked")
    public void relabel(String section, String field, String[] labels) {
        Object rows = data.get(section);
        if (!(rows instanceof List<?> list)) {
            return;
        }
        for (Object row : list) {
            if (row instanceof Map<?, ?> map) {
                replaceWithLabel((Map<String, Object>) map, field, labels);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> object(String section) {
        Object value = data.get(section);
        return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
    }

    public static void replaceWithLabel(Map<String, Object> object, String key, String[] labels) {
        Object value = object.get(key);
        if (value instanceof Integer) {
            int index = (int) value;
            if (index >= 0 && index < labels.length) {
                object.put(key, labels[index]);
            }
        }
    }

    public static void resolveLocation(Map<String, Object> object, String regionKey,
                                       String stateKey, String cityKey) {
        try {
            Map<String, Region> regionMap = LocationUtils.getRegionMap();
            String regionCode = (String) object.get(regionKey);
            String stateCode = (String) object.get(stateKey);
            String cityCode = (String) object.get(cityKey);
            Region region = regionCode != null ? regionMap.get(regionCode) : null;
            if (region != null) {
                object.put(regionKey, region.getName());
                State state = stateCode != null ? region.getStateMap().get(stateCode) : null;
                if (state != null) {
                    object.put(stateKey, state.getName());
                    City city = cityCode != null ? state.getCityMap().get(cityCode) : null;
                    if (city != null) {
                        object.put(cityKey, city.getName());
                    }
                }
            }
        } catch (Exception ignored) {
            // A missing dictionary must not abort the export.
        }
    }
}
