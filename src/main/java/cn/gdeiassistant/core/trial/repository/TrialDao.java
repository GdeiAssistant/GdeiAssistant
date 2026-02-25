package cn.gdeiassistant.core.trial.repository;

import cn.gdeiassistant.common.pojo.Document.TrialDocument;

public interface TrialDao {

    TrialDocument queryTrialData(String type);

    TrialDocument queryTrialData(String type, int index);
}
