package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.core.userProfile.pojo.ProfileOptionsVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class ProfileOptionsFacadeTest {

    private final ProfileOptionsFacade facade = new ProfileOptionsFacade();

    ProfileOptionsFacadeTest() {
        ReflectionTestUtils.setField(facade, "profileLocalizationService", new ProfileLocalizationService());
    }

    @Test
    void buildProfileOptions_containsAllSections() {
        ProfileOptionsVO options = facade.buildProfileOptions();

        assertNotNull(options.getFaculties());
        assertFalse(options.getFaculties().isEmpty());

        assertNotNull(options.getMarketplaceItemTypes());
        assertFalse(options.getMarketplaceItemTypes().isEmpty());

        assertNotNull(options.getLostFoundItemTypes());
        assertFalse(options.getLostFoundItemTypes().isEmpty());

        assertNotNull(options.getLostFoundModes());
        assertFalse(options.getLostFoundModes().isEmpty());
    }

    @Test
    void buildProfileOptions_facultiesHaveMajors() {
        ProfileOptionsVO options = facade.buildProfileOptions();
        // Every faculty entry should have a non-null majors list
        for (ProfileOptionsVO.FacultyOptionVO faculty : options.getFaculties()) {
            assertNotNull(faculty.getMajors(), "majors should not be null for faculty code " + faculty.getCode());
            assertFalse(faculty.getMajors().isEmpty());
            for (ProfileOptionsVO.MajorOptionVO major : faculty.getMajors()) {
                assertNotNull(major.getCode());
                assertNotNull(major.getLabel());
                assertFalse(major.getCode().isBlank());
                assertFalse(major.getLabel().isBlank());
            }
        }
    }

    @Test
    void buildProfileOptions_codesAreSequential() {
        ProfileOptionsVO options = facade.buildProfileOptions();
        for (int i = 0; i < options.getFaculties().size(); i++) {
            assertEquals(i, options.getFaculties().get(i).getCode());
        }
        for (int i = 0; i < options.getMarketplaceItemTypes().size(); i++) {
            assertEquals(i, options.getMarketplaceItemTypes().get(i).getCode());
        }
    }
}
