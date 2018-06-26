package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Grade;

import com.linguancheng.gdeiassistant.Pojo.GradeQuery.GradeQuery;
import org.apache.ibatis.annotations.Param;

public interface GradeMapper {

    public GradeQuery selectGrade(String username) throws Exception;

    public void insertGrade(@Param("username") String username, @Param("grade") String grade) throws Exception;

    public void updateGrade(@Param("username") String username, @Param("grade") String grade) throws Exception;
}
