package com.linguancheng.gdeiassistant.Exception.CloseAccountException;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/25
 */

/**
 * 关闭账号时，用户有未处理的社区功能信息时，抛出该异常
 */
public class ItemAvailableException extends CloseAccountException {

    public ItemAvailableException() {
    }

    public ItemAvailableException(String message) {
        super(message);
    }
}
