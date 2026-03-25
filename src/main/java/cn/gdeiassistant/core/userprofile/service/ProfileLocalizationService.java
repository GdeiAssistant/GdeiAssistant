package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.common.pojo.Entity.Region;
import cn.gdeiassistant.common.tools.Utils.LocationUtils;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.i18n.I18nTranslationService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileLocationValueVO;
import com.github.houbb.opencc4j.util.ZhConverterUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public String resolveMajorCode(Integer facultyCode, String storedMajorValue) {
        if (facultyCode == null || facultyCode <= 0 || StringUtils.isBlank(storedMajorValue)) {
            return null;
        }
        ProfileMajorCatalog.MajorDefinition major = ProfileMajorCatalog.resolveForFaculty(facultyCode, storedMajorValue);
        if (major == null) {
            return storedMajorValue;
        }
        return major.getCode();
    }

    public ProfileLocationValueVO buildLocationValue(ProfileVO profile, boolean hometown) {
        if (profile == null) {
            return null;
        }
        String regionCode = hometown ? profile.getHometownRegion() : profile.getLocationRegion();
        String stateCode = hometown ? profile.getHometownState() : profile.getLocationState();
        String cityCode = hometown ? profile.getHometownCity() : profile.getLocationCity();
        if (StringUtils.isBlank(regionCode)) {
            return null;
        }
        return new ProfileLocationValueVO(regionCode, stateCode, cityCode);
    }

    public List<ProfileRegionNodeVO> buildRegionTree() {
        List<ProfileRegionNodeVO> regions = new ArrayList<>();
        for (Region sourceRegion : LocationUtils.getRegionMap().values()) {
            List<ProfileRegionNodeVO> states = new ArrayList<>();
            if (sourceRegion.getStateMap() != null) {
                for (var stateEntry : sourceRegion.getStateMap().entrySet()) {
                    var sourceState = stateEntry.getValue();
                    List<ProfileRegionNodeVO> cities = new ArrayList<>();
                    if (sourceState.getCityMap() != null) {
                        for (var cityEntry : sourceState.getCityMap().entrySet()) {
                            cities.add(new ProfileRegionNodeVO(cityEntry.getValue().getCode(), List.of()));
                        }
                    }
                    states.add(new ProfileRegionNodeVO(sourceState.getCode(), cities));
                }
            }
            regions.add(new ProfileRegionNodeVO(sourceRegion.getCode(), states));
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
        String translated = translationService.translate(text, normalizedLanguage);
        if (translated != null && !translated.isBlank()) {
            return translated;
        }
        return text;
    }

    public static class ProfileRegionNodeVO {
        private String code;
        private List<ProfileRegionNodeVO> children;

        public ProfileRegionNodeVO() {
        }

        public ProfileRegionNodeVO(String code, List<ProfileRegionNodeVO> children) {
            this.code = code;
            this.children = children;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public List<ProfileRegionNodeVO> getChildren() {
            return children;
        }

        public void setChildren(List<ProfileRegionNodeVO> children) {
            this.children = children;
        }
    }
}
