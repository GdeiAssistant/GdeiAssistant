package edu.gdei.gdeiassistant.Exception.AuthenticationException;

/**
 * 身份证不是通过相机拍照直接上传，而是被编辑软件编辑过，则抛出该异常
 */
public class IDCardEditedException extends Exception {

    public IDCardEditedException() {
    }

    public IDCardEditedException(String message) {
        super(message);
    }
}
