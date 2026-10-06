package cn.gdeiassistant.common.redis.exportdata;

public interface ExportDataDao {

    String queryExportingDataToken(String username);

    void removeExportingDataToken(String username);

    void saveExportingDataToken(String username, String token);

    String queryExportDataToken(String username);

    void saveExportDataToken(String username, String token);
}
