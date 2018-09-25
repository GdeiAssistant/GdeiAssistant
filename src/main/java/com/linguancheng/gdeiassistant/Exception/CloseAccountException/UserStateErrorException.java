package com.linguancheng.gdeiassistant.Exception.CloseAccountException;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/25
 */

/**
 * 用户账号状态异常时抛出该异常
 */
public class UserStateErrorException extends CloseAccountException {

    public UserStateErrorException() {
    }

    public UserStateErrorException(String message) {
        super(message);
    }
}
