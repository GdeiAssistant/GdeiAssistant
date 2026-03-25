package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileLocationValueVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileMajorValueVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProfileLocalizationServiceTest {

    private final ProfileLocalizationService service = new ProfileLocalizationService();

    @Test
    void normalizeLanguage_supportsSixLocalesAndFallback() {
        assertEquals("zh-CN", service.normalizeLanguage(null));
        assertEquals("zh-CN", service.normalizeLanguage("fr-FR,fr;q=0.9"));
        assertEquals("zh-HK", service.normalizeLanguage("zh-Hant-HK"));
        assertEquals("zh-TW", service.normalizeLanguage("zh-Hant"));
        assertEquals("en", service.normalizeLanguage("en-US"));
        assertEquals("ja", service.normalizeLanguage("ja-JP"));
        assertEquals("ko", service.normalizeLanguage("ko-KR"));
    }

    @Test
    void buildMajorValue_acceptsStoredCodeAndLegacyLabel() {
        ProfileMajorValueVO fromCode = service.buildMajorValue(11, "software_engineering", "zh-CN");
        ProfileMajorValueVO fromLegacyLabel = service.buildMajorValue(11, "软件工程", "zh-CN");

        assertNotNull(fromCode);
        assertEquals("software_engineering", fromCode.getCode());
        assertEquals("软件工程", fromCode.getLabel());

        assertNotNull(fromLegacyLabel);
        assertEquals("software_engineering", fromLegacyLabel.getCode());
        assertEquals("软件工程", fromLegacyLabel.getLabel());
    }

    @Test
    void buildLocationValue_restoresCodesAndDisplayName() {
        ProfileVO profile = new ProfileVO();
        profile.setLocationRegion("CN");
        profile.setLocationState("44");
        profile.setLocationCity("1");

        ProfileLocationValueVO value = service.buildLocationValue(profile, false, "en-US");

        assertNotNull(value);
        assertEquals("CN", value.getRegion());
        assertEquals("44", value.getState());
        assertEquals("1", value.getCity());
        assertFalse(value.getDisplayName().isBlank());
    }
}
