package cn.gdeiassistant.Repository.Redis.VerificationCode;

public interface VerificationCodeDao {

    public Integer QueryPhoneVerificationCode(int code, String phone);

    public void SavePhoneVerificationCode(int code, String phone, int randomCode);

    public void DeletePhoneVerificationCode(int code, String phone);

}
