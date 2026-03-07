package cn.gdeiassistant.core.capability.email;

import cn.gdeiassistant.common.exception.VerificationException.SendEmailException;

public interface EmailVerificationSender {

    void sendVerificationCode(String recipientEmail, int code) throws SendEmailException;
}
