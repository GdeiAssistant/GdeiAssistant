package cn.gdeiassistant.Repository.Mongodb.Grade;

import cn.gdeiassistant.Exception.DatasourceException.MongodbNotConfiguredException;
import cn.gdeiassistant.Pojo.Document.GradeDocument;

public interface GradeDao {

    public void saveGrade(GradeDocument gradeDocument) throws MongodbNotConfiguredException;

    public GradeDocument queryGradeByUsername(String username);

    public void removeGrade(String username) throws MongodbNotConfiguredException;
}
