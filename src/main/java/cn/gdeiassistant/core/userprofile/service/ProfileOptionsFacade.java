package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.common.constant.OptionConstantUtils;
import cn.gdeiassistant.core.userProfile.pojo.ProfileOptionsVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles the profile-options dictionary response.
 * Extracted from ProfileController to keep the controller thin.
 */
@Service
public class ProfileOptionsFacade {

    public ProfileOptionsVO buildProfileOptions() {
        ProfileOptionsVO options = new ProfileOptionsVO();
        options.setFaculties(buildFacultyOptions());
        options.setMarketplaceItemTypes(buildDictionaryOptions(OptionConstantUtils.MARKETPLACE_ITEM_TYPE_OPTIONS));
        options.setLostFoundItemTypes(buildDictionaryOptions(OptionConstantUtils.LOST_FOUND_ITEM_TYPE_OPTIONS));
        options.setLostFoundModes(buildDictionaryOptions(OptionConstantUtils.LOST_FOUND_MODE_OPTIONS));
        return options;
    }

    private List<ProfileOptionsVO.FacultyOptionVO> buildFacultyOptions() {
        List<ProfileOptionsVO.FacultyOptionVO> options = new ArrayList<>();
        for (int i = 0; i < OptionConstantUtils.FACULTY_OPTIONS.length; i++) {
            List<String> majors = new ArrayList<>();
            for (ProfileMajorCatalog.MajorDefinition major : ProfileMajorCatalog.majorsForFaculty(i)) {
                majors.add(major.getCode());
            }
            options.add(new ProfileOptionsVO.FacultyOptionVO(i, majors));
        }
        return options;
    }

    private List<Integer> buildDictionaryOptions(String[] values) {
        List<Integer> options = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            options.add(i);
        }
        return options;
    }
}
