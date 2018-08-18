package com.linguancheng.gdeiassistant.Repository.Mongodb.Grade;

import com.linguancheng.gdeiassistant.Pojo.Document.GradeDocument;

public interface GradeDao {

    public void saveGrade(GradeDocument gradeDocument);

    public GradeDocument queryGradeByUsername(String username);

    public void removeGrade(String username);
}
