package cn.gdeiassistant.Repository.Mongodb.Grade;

import cn.gdeiassistant.Pojo.Document.GradeDocument;

public interface GradeDao {

    void saveGrade(GradeDocument gradeDocument);

    GradeDocument queryGrade(String username);

    void removeGrade(String username);
}
