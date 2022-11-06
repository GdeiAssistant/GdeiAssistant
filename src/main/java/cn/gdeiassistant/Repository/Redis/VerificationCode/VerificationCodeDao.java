package cn.gdeiassistant.Repository.Redis.VerificationCode;

public interface VerificationCodeDao {

    Integer QueryPhoneVerificationCode(int code, String phone);

    void SavePhoneVerificationCode(int code, String phone, int randomCode);

    void DeletePhoneVerificationCode(int code, String phone);

    Integer QueryEmailVerificationCode(String email);

    void SaveEmailVerificationCode(String email, int randomCode);

    void DeleteEmailVerificationCode(String email);

}
