package edu.gdei.gdeiassistant.Repository.Mongodb.Grade;

import edu.gdei.gdeiassistant.Pojo.Document.GradeDocument;

public interface GradeDao {

    public void saveGrade(GradeDocument gradeDocument);

    public GradeDocument queryGradeByUsername(String username);

    public void removeGrade(String username);
}
