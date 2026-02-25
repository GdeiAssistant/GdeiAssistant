package cn.gdeiassistant.integration.edu.pojo;

/**
 * 教务系统会话凭证（防腐层入参），与 core 的 UserCertificateEntity 解耦。
 */
public class EduSessionCredential {

    private String username;
    private String number;
    private String keycode;
    private Long timestamp;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getNumber() { return number; }
    public void setNumber(String number) { this.number = number; }
    public String getKeycode() { return keycode; }
    public void setKeycode(String keycode) { this.keycode = keycode; }
    public Long getTimestamp() { return timestamp; }
    public void setTimestamp(Long timestamp) { this.timestamp = timestamp; }
}
