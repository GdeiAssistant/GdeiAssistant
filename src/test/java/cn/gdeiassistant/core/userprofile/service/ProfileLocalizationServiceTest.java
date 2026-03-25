package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.core.i18n.I18nTranslationService;
import cn.gdeiassistant.core.profile.pojo.vo.ProfileVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileLocationValueVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    void resolveMajorCode_acceptsStoredCodeAndLegacyLabel() {
        String fromCode = service.resolveMajorCode(11, "software_engineering");
        String fromLegacyLabel = service.resolveMajorCode(11, "软件工程");

        assertEquals("software_engineering", fromCode);
        assertEquals("software_engineering", fromLegacyLabel);
    }

    @Test
    void buildLocationValue_restoresCodesOnly() {
        ProfileVO profile = new ProfileVO();
        profile.setLocationRegion("CN");
        profile.setLocationState("44");
        profile.setLocationCity("1");

        ProfileLocationValueVO value = service.buildLocationValue(profile, false);

        assertNotNull(value);
        assertEquals("CN", value.getRegionCode());
        assertEquals("44", value.getStateCode());
        assertEquals("1", value.getCityCode());
    }

    @Test
    void localizeText_usesSynchronousTranslationBeforeReturningOriginalText() {
        I18nTranslationService translationService = mock(I18nTranslationService.class);
        when(translationService.translate("软件工程", "en")).thenReturn("Software Engineering");
        ReflectionTestUtils.setField(service, "translationService", translationService);

        String localized = service.localizeText("软件工程", "en-US");

        assertEquals("Software Engineering", localized);
        verify(translationService).translate("软件工程", "en");
        verify(translationService, never()).enqueueTranslation(anyString(), anyString());
    }
}
