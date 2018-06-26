package com.gdeiassistant.gdeiassistant.Repository.Mongodb.Grade;

import com.gdeiassistant.gdeiassistant.Pojo.Document.GradeDocument;

public interface GradeDao {

    public void saveGrade(GradeDocument gradeDocument);

    public GradeDocument queryGradeByUsername(String username);
}
