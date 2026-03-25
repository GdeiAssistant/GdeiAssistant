package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.common.constant.OptionConstantUtils;
import cn.gdeiassistant.common.pojo.Entity.City;
import cn.gdeiassistant.common.pojo.Entity.Region;
import cn.gdeiassistant.common.pojo.Entity.State;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.i18n.I18nTranslationService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileFacultyValueVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileLocationValueVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileMajorValueVO;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ProfileLocalizationService {

    @Autowired(required = false)
    private I18nTranslationService translationService;

    public String normalizeLanguage(String header) {
        if (header == null || header.isBlank()) {
            return "zh-CN";
        }
        String lang = header.split(",")[0].trim().split(";")[0].trim();
        switch (lang) {
            case "zh-HK":
            case "zh-Hant-HK":
                return "zh-HK";
            case "zh-TW":
            case "zh-Hant":
            case "zh-Hant-TW":
                return "zh-TW";
            case "zh-CN":
            case "zh-Hans":
            case "zh-Hans-CN":
            case "zh":
                return "zh-CN";
            default:
                if (lang.startsWith("en")) {
                    return "en";
                }
                if (lang.startsWith("ja")) {
                    return "ja";
                }
                if (lang.startsWith("ko")) {
                    return "ko";
                }
                if (lang.startsWith("zh-HK")) {
                    return "zh-HK";
                }
                if (lang.startsWith("zh-TW") || lang.startsWith("zh-Hant")) {
                    return "zh-TW";
                }
                if (lang.startsWith("zh")) {
                    return "zh-CN";
                }
                return "zh-CN";
        }
    }

    public ProfileFacultyValueVO buildFacultyValue(Integer facultyCode, String language) {
        if (facultyCode == null || facultyCode <= 0 || facultyCode >= OptionConstantUtils.FACULTY_OPTIONS.length) {
            return null;
        }
        return new ProfileFacultyValueVO(facultyCode, localizeText(OptionConstantUtils.FACULTY_OPTIONS[facultyCode], language));
    }

    public ProfileMajorValueVO buildMajorValue(Integer facultyCode, String storedMajorValue, String language) {
        if (facultyCode == null || facultyCode <= 0 || StringUtils.isBlank(storedMajorValue)) {
            return null;
        }
        ProfileMajorCatalog.MajorDefinition major = ProfileMajorCatalog.resolveForFaculty(facultyCode, storedMajorValue);
        if (major == null) {
            return new ProfileMajorValueVO(storedMajorValue, localizeText(storedMajorValue, language));
        }
        return new ProfileMajorValueVO(major.getCode(), localizeText(major.getLabel(), language));
    }

    public ProfileLocationValueVO buildLocationValue(ProfileVO profile, boolean hometown, String language) {
        if (profile == null) {
            return null;
        }
        String regionCode = hometown ? profile.getHometownRegion() : profile.getLocationRegion();
        String stateCode = hometown ? profile.getHometownState() : profile.getLocationState();
        String cityCode = hometown ? profile.getHometownCity() : profile.getLocationCity();
        if (StringUtils.isBlank(regionCode)) {
            return null;
        }
        return buildLocationValue(regionCode, stateCode, cityCode, language);
    }

    public ProfileLocationValueVO buildLocationValue(String regionCode, String stateCode, String cityCode, String language) {
        Region region = LocationUtils.getRegionMap().get(regionCode);
        if (region == null) {
            return null;
        }
        State state = region.getStateMap() != null ? region.getStateMap().get(stateCode) : null;
        City city = state != null && state.getCityMap() != null ? state.getCityMap().get(cityCode) : null;

        List<String> parts = new ArrayList<>();
        appendLocationPart(parts, baseLocationName(region.getAliasesName(), region.getName()), language);
        appendLocationPart(parts, state != null ? baseLocationName(state.getAliasesName(), state.getName()) : null, language);
        appendLocationPart(parts, city != null ? baseLocationName(city.getAliasesName(), city.getName()) : null, language);

        return new ProfileLocationValueVO(regionCode, stateCode, cityCode, joinLocationParts(parts, normalizeLanguage(language)));
    }

    public List<Region> buildLocalizedRegions(String language) {
        List<Region> regions = new ArrayList<>();
        for (Region sourceRegion : LocationUtils.getRegionMap().values()) {
            Region region = new Region();
            region.setCode(sourceRegion.getCode());
            region.setIso(sourceRegion.getIso());
            region.setName(localizeText(baseLocationName(sourceRegion.getAliasesName(), sourceRegion.getName()), language));
            region.setAliasesName(null);

            java.util.LinkedHashMap<String, State> states = new java.util.LinkedHashMap<>();
            for (Map.Entry<String, State> stateEntry : sourceRegion.getStateMap().entrySet()) {
                State sourceState = stateEntry.getValue();
                State state = new State();
                state.setCode(sourceState.getCode());
                state.setName(localizeText(baseLocationName(sourceState.getAliasesName(), sourceState.getName()), language));
                state.setAliasesName(null);

                java.util.LinkedHashMap<String, City> cities = new java.util.LinkedHashMap<>();
                for (Map.Entry<String, City> cityEntry : sourceState.getCityMap().entrySet()) {
                    City sourceCity = cityEntry.getValue();
                    City city = new City();
                    city.setCode(sourceCity.getCode());
                    city.setName(localizeText(baseLocationName(sourceCity.getAliasesName(), sourceCity.getName()), language));
                    city.setAliasesName(null);
                    cities.put(city.getCode(), city);
                }
                state.setCityMap(cities);
                states.put(state.getCode(), state);
            }
            region.setStateMap(states);
            regions.add(region);
        }
        return regions;
    }

    public String localizeText(String text, String language) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        String normalizedLanguage = normalizeLanguage(language);
        if ("zh-CN".equals(normalizedLanguage)) {
            return text;
        }
        if ("zh-HK".equals(normalizedLanguage) || "zh-TW".equals(normalizedLanguage)) {
            return ZhConverterUtil.toTraditional(text);
        }
        if (translationService == null) {
            return text;
        }
        String cached = translationService.getCachedTranslation(text, normalizedLanguage);
        if (cached != null) {
            return cached;
        }
        translationService.enqueueTranslation(text, normalizedLanguage);
        return text;
    }

    private void appendLocationPart(List<String> parts, String value, String language) {
        String localized = localizeText(value, language);
        if (StringUtils.isBlank(localized) || parts.contains(localized)) {
            return;
        }
        parts.add(localized);
    }

    private String baseLocationName(String aliasesName, String name) {
        return StringUtils.isBlank(aliasesName) ? name : aliasesName;
    }

    private String joinLocationParts(List<String> parts, String language) {
        if (parts.isEmpty()) {
            return "";
        }
        if ("zh-CN".equals(language) || "zh-HK".equals(language) || "zh-TW".equals(language)) {
            return String.join("", parts);
        }
        return String.join(" ", parts);
    }
}
