package cn.gdeiassistant.common.exception.CommonException;

/**
 * 测试账号操作受限时抛出的异常，例如不允许更新教务缓存数据等。
 */
public class TestAccountException extends Exception {

    public TestAccountException(String message) {
        super(message);
    }
}

