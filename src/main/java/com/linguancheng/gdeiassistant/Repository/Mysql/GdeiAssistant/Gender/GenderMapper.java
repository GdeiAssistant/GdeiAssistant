package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Gender;

import org.apache.ibatis.annotations.Param;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/15
 */
public interface GenderMapper {

    public String selectCustomGender(String username) throws Exception;

    public void insertCustomGender(@Param("username") String username
            , @Param("customGender") String customGender) throws Exception;

    public void updateCustomGender(@Param("username") String username
            , @Param("customGender") String customGender) throws Exception;

    public void deleteCustomGender(String username) throws Exception;
}
