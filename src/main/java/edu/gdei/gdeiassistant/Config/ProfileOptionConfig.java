package edu.gdei.gdeiassistant.Config;

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
        genderMap.put(0, "未选择");
        genderMap.put(1, "男");
        genderMap.put(2, "女");
        genderMap.put(3, "自定义");
        return genderMap;
    }

    /**
     * 个人资料性取向字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> genderOrientationMap() {
        Map<Integer, String> genderOrientationMap = new HashMap<>();
        genderOrientationMap.put(0, "未选择");
        genderOrientationMap.put(1, "异性恋");
        genderOrientationMap.put(2, "同性恋");
        genderOrientationMap.put(3, "双性恋");
        genderOrientationMap.put(4, "无性恋");
        genderOrientationMap.put(5, "泛性恋");
        genderOrientationMap.put(6, "其他");
        return genderOrientationMap;
    }

    /**
     * 院系字典
     *
     * @return
     */
    @Bean
    public Map<Integer, String> facultyMap() {
        Map<Integer, String> facultyMap = new HashMap<>();
        facultyMap.put(0, "未选择");
        facultyMap.put(1, "教育学院");
        facultyMap.put(2, "政法系");
        facultyMap.put(3, "中文系");
        facultyMap.put(4, "数学系");
        facultyMap.put(5, "外语系");
        facultyMap.put(6, "物理与信息工程系");
        facultyMap.put(7, "化学系");
        facultyMap.put(8, "生物与食品工程学院");
        facultyMap.put(9, "体育学院");
        facultyMap.put(10, "美术学院");
        facultyMap.put(11, "计算机科学系");
        facultyMap.put(12, "音乐系");
        facultyMap.put(13, "教师研修学院");
        facultyMap.put(14, "成人教育学院");
        facultyMap.put(15, "网络教育学院");
        facultyMap.put(16, "马克思主义学院");
        return facultyMap;
    }
}
