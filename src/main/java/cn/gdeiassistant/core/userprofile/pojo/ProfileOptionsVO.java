package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;
import java.util.List;

public class ProfileOptionsVO implements Serializable {

    private List<FacultyOptionVO> faculties;
    private List<Integer> marketplaceItemTypes;
    private List<Integer> lostFoundItemTypes;
    private List<Integer> lostFoundModes;

    public List<FacultyOptionVO> getFaculties() {
        return faculties;
    }

    public void setFaculties(List<FacultyOptionVO> faculties) {
        this.faculties = faculties;
    }

    public List<Integer> getMarketplaceItemTypes() {
        return marketplaceItemTypes;
    }

    public void setMarketplaceItemTypes(List<Integer> marketplaceItemTypes) {
        this.marketplaceItemTypes = marketplaceItemTypes;
    }

    public List<Integer> getLostFoundItemTypes() {
        return lostFoundItemTypes;
    }

    public void setLostFoundItemTypes(List<Integer> lostFoundItemTypes) {
        this.lostFoundItemTypes = lostFoundItemTypes;
    }

    public List<Integer> getLostFoundModes() {
        return lostFoundModes;
    }

    public void setLostFoundModes(List<Integer> lostFoundModes) {
        this.lostFoundModes = lostFoundModes;
    }

    public static class FacultyOptionVO implements Serializable {

        private Integer code;

        private List<String> majors;

        public FacultyOptionVO() {
        }

        public FacultyOptionVO(Integer code, List<String> majors) {
            this.code = code;
            this.majors = majors;
        }

        public Integer getCode() {
            return code;
        }

        public void setCode(Integer code) {
            this.code = code;
        }

        public List<String> getMajors() {
            return majors;
        }

        public void setMajors(List<String> majors) {
            this.majors = majors;
        }
    }
}
