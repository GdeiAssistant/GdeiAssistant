package cn.gdeiassistant.core.grade.repository;

import cn.gdeiassistant.common.pojo.document.GradeDocument;

public interface GradeDao {

    void saveGrade(GradeDocument gradeDocument);

    GradeDocument queryGrade(String username);

    void removeGrade(String username);
}
