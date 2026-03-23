package cn.gdeiassistant.core.userProfile.controller.support;

import cn.gdeiassistant.common.pojo.Entity.City;
import cn.gdeiassistant.common.pojo.Entity.Region;
import cn.gdeiassistant.common.pojo.Entity.State;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import org.springframework.stereotype.Component;

@Component
public class ProfileLocationFormatter {

    public String buildLocationDisplayString(ProfileVO profile) {
        if (profile == null || StringUtils.isBlank(profile.getLocationRegion())) return "";
        return buildDisplayString(
                profile.getLocationRegion(),
                profile.getLocationState(),
                profile.getLocationCity()
        );
    }

    public String buildHometownDisplayString(ProfileVO profile) {
        if (profile == null || StringUtils.isBlank(profile.getHometownRegion())) return "";
        return buildDisplayString(
                profile.getHometownRegion(),
                profile.getHometownState(),
                profile.getHometownCity()
        );
    }

    private String buildDisplayString(String regionCode, String stateCode, String cityCode) {
        Region region = LocationUtils.getRegionMap().get(regionCode);
        if (region == null) return "";
        StringBuilder sb = new StringBuilder();
        String name = StringUtils.isBlank(region.getAliasesName()) ? region.getName() : region.getAliasesName();
        sb.append(name);
        State state = region.getStateMap() != null ? region.getStateMap().get(stateCode) : null;
        if (state != null) {
            String temp = StringUtils.isBlank(state.getAliasesName()) ? state.getName() : state.getAliasesName();
            if (!name.equals(temp)) sb.append(temp);
            City city = state.getCityMap() != null ? state.getCityMap().get(cityCode) : null;
            if (city != null) {
                temp = StringUtils.isBlank(city.getAliasesName()) ? city.getName() : city.getAliasesName();
                if (!name.equals(temp)) sb.append(temp);
            }
        }
        return (region.getIso() != null ? LocationUtils.convertCountryCodeToEmoji(region.getIso()) : "") + sb.toString();
    }
}
