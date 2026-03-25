package cn.gdeiassistant.core.userProfile.pojo;

import java.io.Serializable;
import java.util.List;

public class ProfileOptionsVO implements Serializable {

    private List<FacultyOptionVO> faculties;
    private List<DictionaryOptionVO> marketplaceItemTypes;
    private List<DictionaryOptionVO> lostFoundItemTypes;
    private List<DictionaryOptionVO> lostFoundModes;

    public List<FacultyOptionVO> getFaculties() {
        return faculties;
    }

    public void setFaculties(List<FacultyOptionVO> faculties) {
        this.faculties = faculties;
    }

    public List<DictionaryOptionVO> getMarketplaceItemTypes() {
        return marketplaceItemTypes;
    }

    public void setMarketplaceItemTypes(List<DictionaryOptionVO> marketplaceItemTypes) {
        this.marketplaceItemTypes = marketplaceItemTypes;
    }

    public List<DictionaryOptionVO> getLostFoundItemTypes() {
        return lostFoundItemTypes;
    }

    public void setLostFoundItemTypes(List<DictionaryOptionVO> lostFoundItemTypes) {
        this.lostFoundItemTypes = lostFoundItemTypes;
    }

    public List<DictionaryOptionVO> getLostFoundModes() {
        return lostFoundModes;
    }

    public void setLostFoundModes(List<DictionaryOptionVO> lostFoundModes) {
        this.lostFoundModes = lostFoundModes;
    }

    public static class FacultyOptionVO extends DictionaryOptionVO {

        private List<MajorOptionVO> majors;

        public FacultyOptionVO() {
        }

        public FacultyOptionVO(Integer code, String label, List<MajorOptionVO> majors) {
            super(code, label);
            this.majors = majors;
        }

        public List<MajorOptionVO> getMajors() {
            return majors;
        }

        public void setMajors(List<MajorOptionVO> majors) {
            this.majors = majors;
        }
    }

    public static class MajorOptionVO implements Serializable {

        private String code;
        private String label;

        public MajorOptionVO() {
        }

        public MajorOptionVO(String code, String label) {
            this.code = code;
            this.label = label;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
