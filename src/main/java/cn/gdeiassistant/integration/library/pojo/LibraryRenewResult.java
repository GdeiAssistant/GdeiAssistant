package cn.gdeiassistant.integration.library.pojo;

/**
 * 图书馆续借接口返回（防腐层出参），与 OPAC  JSON 解耦。
 */
public class LibraryRenewResult {

    private int result;
    private String message;

    public int getResult() { return result; }
    public void setResult(int result) { this.result = result; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
