package edu.gdei.gdeiassistant.Config.Option;

import edu.gdei.gdeiassistant.Constant.OptionConstantUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class ProfileOptionConfig {

    /**
     * 个人资料性别字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> genderMap() {
        Map<Integer, String> genderMap = new HashMap<>();
        for (int i = 0; i < OptionConstantUtils.GENDER_OPTIONS.length; i++) {
            genderMap.put(i, OptionConstantUtils.GENDER_OPTIONS[i]);
        }
        return genderMap;
    }

    /**
     * 职业字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> professionMap() {
        Map<Integer, String> professionMap = new HashMap<>();
        for (int i = 0; i < OptionConstantUtils.PROFESSION_OPTIONS.length; i++) {
            professionMap.put(i, OptionConstantUtils.PROFESSION_OPTIONS[i]);
        }
        return professionMap;
    }

    /**
     * 学历字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> degreeMap() {
        Map<Integer, String> degreeMap = new HashMap<>();
        for (int i = 0; i < OptionConstantUtils.DEGREE_OPTIONS.length; i++) {
            degreeMap.put(i, OptionConstantUtils.DEGREE_OPTIONS[i]);
        }
        return degreeMap;
    }

    /**
     * 院系字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> facultyMap() {
        Map<Integer, String> facultyMap = new HashMap<>();
        for (int i = 0; i < OptionConstantUtils.FACULTY_OPTIONS.length; i++) {
            facultyMap.put(i, OptionConstantUtils.FACULTY_OPTIONS[i]);
        }
        return facultyMap;
    }
}
