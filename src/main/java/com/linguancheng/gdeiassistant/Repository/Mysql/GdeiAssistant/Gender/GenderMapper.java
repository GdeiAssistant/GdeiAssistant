package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Gender;

import org.apache.ibatis.annotations.Param;

public interface GenderMapper {

    public String selectCustomGender(String username) throws Exception;

    public void insertCustomGender(@Param("username") String username
            , @Param("customGender") String customGender) throws Exception;

    public void updateCustomGender(@Param("username") String username
            , @Param("customGender") String customGender) throws Exception;

    public void deleteCustomGender(String username) throws Exception;
}
