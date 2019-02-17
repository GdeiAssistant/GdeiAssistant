package edu.gdei.gdeiassistant.Repository.Redis.Request;

public interface RequestDao {

    public String QueryRequest(String nonce);

    public Boolean InsertRequest(String nonce, String timestamp);
}
