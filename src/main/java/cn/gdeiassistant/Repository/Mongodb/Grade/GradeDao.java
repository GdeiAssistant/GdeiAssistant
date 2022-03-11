package cn.gdeiassistant.Repository.Mongodb.Grade;

import cn.gdeiassistant.Pojo.Document.GradeDocument;

public interface GradeDao {

    public void saveGrade(GradeDocument gradeDocument);

    public GradeDocument queryGrade(String username);

    public void removeGrade(String username);
}
