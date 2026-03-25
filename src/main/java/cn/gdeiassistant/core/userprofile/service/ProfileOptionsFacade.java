package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.common.constant.OptionConstantUtils;
import cn.gdeiassistant.core.userProfile.pojo.DictionaryOptionVO;
import cn.gdeiassistant.core.userProfile.pojo.ProfileOptionsVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the profile-options dictionary response.
 * Extracted from ProfileController to keep the controller thin.
 */
@Service
public class ProfileOptionsFacade {

    @Autowired
    private ProfileLocalizationService profileLocalizationService;

    public ProfileOptionsVO buildProfileOptions() {
        return buildProfileOptions("zh-CN");
    }

    public ProfileOptionsVO buildProfileOptions(String language) {
        ProfileOptionsVO options = new ProfileOptionsVO();
        options.setFaculties(buildFacultyOptions(language));
        options.setMarketplaceItemTypes(buildDictionaryOptions(OptionConstantUtils.MARKETPLACE_ITEM_TYPE_OPTIONS, language));
        options.setLostFoundItemTypes(buildDictionaryOptions(OptionConstantUtils.LOST_FOUND_ITEM_TYPE_OPTIONS, language));
        options.setLostFoundModes(buildDictionaryOptions(OptionConstantUtils.LOST_FOUND_MODE_OPTIONS, language));
        return options;
    }

    private List<ProfileOptionsVO.FacultyOptionVO> buildFacultyOptions(String language) {
        List<ProfileOptionsVO.FacultyOptionVO> options = new ArrayList<>();
        for (int i = 0; i < OptionConstantUtils.FACULTY_OPTIONS.length; i++) {
            List<ProfileOptionsVO.MajorOptionVO> majors = new ArrayList<>();
            for (ProfileMajorCatalog.MajorDefinition major : ProfileMajorCatalog.majorsForFaculty(i)) {
                majors.add(new ProfileOptionsVO.MajorOptionVO(
                        major.getCode(),
                        profileLocalizationService.localizeText(major.getLabel(), language)
                ));
            }
            options.add(new ProfileOptionsVO.FacultyOptionVO(
                    i,
                    profileLocalizationService.localizeText(OptionConstantUtils.FACULTY_OPTIONS[i], language),
                    majors
            ));
        }
        return options;
    }

    private List<DictionaryOptionVO> buildDictionaryOptions(String[] values, String language) {
        List<DictionaryOptionVO> options = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            options.add(new DictionaryOptionVO(i, profileLocalizationService.localizeText(values[i], language)));
        }
        return options;
    }
}
