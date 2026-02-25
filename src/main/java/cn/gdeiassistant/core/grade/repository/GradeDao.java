package cn.gdeiassistant.core.grade.repository;

import cn.gdeiassistant.common.pojo.Document.GradeDocument;

public interface GradeDao {

    void saveGrade(GradeDocument gradeDocument);

    GradeDocument queryGrade(String username);

    void removeGrade(String username);
}
