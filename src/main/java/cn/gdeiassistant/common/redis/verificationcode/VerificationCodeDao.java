package cn.gdeiassistant.common.redis.verificationcode;

public interface VerificationCodeDao {

    Integer queryPhoneVerificationCode(int code, String phone);

    void savePhoneVerificationCode(int code, String phone, int randomCode);

    void deletePhoneVerificationCode(int code, String phone);

    Integer queryEmailVerificationCode(String email);

    void saveEmailVerificationCode(String email, int randomCode);

    void deleteEmailVerificationCode(String email);

}
