package cn.gdeiassistant.Repository.Redis.ExportData;

public interface ExportDataDao {

    String QueryExportingDataToken(String username);

    void RemoveExportingDataToken(String username);

    void SaveExportingDataToken(String username, String token);

    String QueryExportDataToken(String username);

    void SaveExportDataToken(String username, String token);
}
