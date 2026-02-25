package cn.gdeiassistant.common.redis.Request;

public interface RequestDao {

    String QueryRequest(String nonce);

    void InsertRequest(String nonce, String timestamp);
}
