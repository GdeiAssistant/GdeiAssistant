package cn.gdeiassistant.Repository.Mongodb.Trial;

import cn.gdeiassistant.Pojo.Document.TrialDocument;

public interface TrialDao {

    TrialDocument queryTrialData(String type);

    TrialDocument queryTrialData(String type, int index);
}
