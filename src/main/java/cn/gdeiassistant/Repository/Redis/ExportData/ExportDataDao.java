package cn.gdeiassistant.Repository.Redis.ExportData;

public interface ExportDataDao {

    public String QueryExportingDataToken(String username);

    public void RemoveExportingDataToken(String username);

    public void SaveExportingDataToken(String username, String token);

    public String QueryExportDataToken(String username);

    public void SaveExportDataToken(String username, String token);
}
