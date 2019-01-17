package com.gdeiassistant.gdeiassistant.Repository.Mysql.GdeiAssistant.Access;

import com.gdeiassistant.gdeiassistant.Pojo.Entity.Access;

public interface AccessMapper {

    public Access selectAndroidAccess(Integer groupId) throws Exception;

    public Access selectWechatAccess(Integer groupId) throws Exception;
}
