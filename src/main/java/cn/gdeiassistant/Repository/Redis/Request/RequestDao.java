package cn.gdeiassistant.Repository.Redis.Request;

public interface RequestDao {

    String QueryRequest(String nonce);

    void InsertRequest(String nonce, String timestamp);
}
