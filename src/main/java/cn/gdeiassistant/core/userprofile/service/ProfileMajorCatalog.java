package cn.gdeiassistant.core.userProfile.service;

import cn.gdeiassistant.common.constant.OptionConstantUtils;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ProfileMajorCatalog {

    private static final Map<String, String> LABEL_TO_CODE = new LinkedHashMap<>();

    static {
        LABEL_TO_CODE.put("未选择", "unselected");
        LABEL_TO_CODE.put("教育学", "education");
        LABEL_TO_CODE.put("学前教育", "preschool_education");
        LABEL_TO_CODE.put("小学教育", "primary_education");
        LABEL_TO_CODE.put("特殊教育", "special_education");
        LABEL_TO_CODE.put("法学", "law");
        LABEL_TO_CODE.put("思想政治教育", "ideological_political_education");
        LABEL_TO_CODE.put("社会工作", "social_work");
        LABEL_TO_CODE.put("汉语言文学", "chinese_language_literature");
        LABEL_TO_CODE.put("历史学", "history");
        LABEL_TO_CODE.put("秘书学", "secretarial_studies");
        LABEL_TO_CODE.put("数学与应用数学", "mathematics_applied_mathematics");
        LABEL_TO_CODE.put("信息与计算科学", "information_computing_science");
        LABEL_TO_CODE.put("统计学", "statistics");
        LABEL_TO_CODE.put("英语", "english");
        LABEL_TO_CODE.put("商务英语", "business_english");
        LABEL_TO_CODE.put("日语", "japanese");
        LABEL_TO_CODE.put("翻译", "translation");
        LABEL_TO_CODE.put("物理学", "physics");
        LABEL_TO_CODE.put("电子信息工程", "electronic_information_engineering");
        LABEL_TO_CODE.put("通信工程", "communication_engineering");
        LABEL_TO_CODE.put("化学", "chemistry");
        LABEL_TO_CODE.put("应用化学", "applied_chemistry");
        LABEL_TO_CODE.put("材料化学", "materials_chemistry");
        LABEL_TO_CODE.put("生物科学", "biological_science");
        LABEL_TO_CODE.put("生物技术", "biotechnology");
        LABEL_TO_CODE.put("食品科学与工程", "food_science_engineering");
        LABEL_TO_CODE.put("体育教育", "physical_education");
        LABEL_TO_CODE.put("社会体育指导与管理", "social_sports_guidance_management");
        LABEL_TO_CODE.put("美术学", "fine_arts");
        LABEL_TO_CODE.put("视觉传达设计", "visual_communication_design");
        LABEL_TO_CODE.put("环境设计", "environmental_design");
        LABEL_TO_CODE.put("软件工程", "software_engineering");
        LABEL_TO_CODE.put("网络工程", "network_engineering");
        LABEL_TO_CODE.put("计算机科学与技术", "computer_science_technology");
        LABEL_TO_CODE.put("物联网工程", "internet_of_things_engineering");
        LABEL_TO_CODE.put("音乐学", "musicology");
        LABEL_TO_CODE.put("音乐表演", "music_performance");
        LABEL_TO_CODE.put("舞蹈学", "dance");
        LABEL_TO_CODE.put("教育技术学", "educational_technology");
        LABEL_TO_CODE.put("行政管理", "public_administration");
        LABEL_TO_CODE.put("工商管理", "business_administration");
        LABEL_TO_CODE.put("会计学", "accounting");
        LABEL_TO_CODE.put("马克思主义理论", "marxist_theory");
    }

    private ProfileMajorCatalog() {
    }

    public static String codeForLabel(String label) {
        return LABEL_TO_CODE.get(label);
    }

    public static String labelForCode(String code) {
        if (code == null) {
            return null;
        }
        for (Map.Entry<String, String> entry : LABEL_TO_CODE.entrySet()) {
            if (entry.getValue().equals(code)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static MajorDefinition resolveForFaculty(Integer facultyCode, String majorValue) {
        if (facultyCode == null || majorValue == null || facultyCode < 0 || facultyCode >= OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY.length) {
            return null;
        }
        for (String label : OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY[facultyCode]) {
            String code = codeForLabel(label);
            if (majorValue.equals(code) || majorValue.equals(label)) {
                return new MajorDefinition(code, label);
            }
        }
        return null;
    }

    public static boolean isValidForFaculty(Integer facultyCode, String majorValue) {
        return resolveForFaculty(facultyCode, majorValue) != null;
    }

    public static MajorDefinition[] majorsForFaculty(Integer facultyCode) {
        if (facultyCode == null || facultyCode < 0 || facultyCode >= OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY.length) {
            return new MajorDefinition[0];
        }
        String[] labels = OptionConstantUtils.MAJOR_OPTIONS_BY_FACULTY[facultyCode];
        MajorDefinition[] definitions = new MajorDefinition[labels.length];
        for (int i = 0; i < labels.length; i++) {
            definitions[i] = new MajorDefinition(codeForLabel(labels[i]), labels[i]);
        }
        return definitions;
    }

    public static final class MajorDefinition {
        private final String code;
        private final String label;

        private MajorDefinition(String code, String label) {
            this.code = code;
            this.label = label;
        }

        String getCode() {
            return code;
        }

        String getLabel() {
            return label;
        }
    }
}
