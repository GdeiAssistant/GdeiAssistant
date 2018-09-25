package com.linguancheng.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/9/26
 */

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CloseLog extends Log {

    private String username;

    private String resetname;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getResetname() {
        return resetname;
    }

    public void setResetname(String resetname) {
        this.resetname = resetname;
    }
}
