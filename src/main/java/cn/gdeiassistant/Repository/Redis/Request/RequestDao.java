package cn.gdeiassistant.Repository.Redis.Request;

public interface RequestDao {

    public String QueryRequest(String nonce);

    public void InsertRequest(String nonce, String timestamp);
}
