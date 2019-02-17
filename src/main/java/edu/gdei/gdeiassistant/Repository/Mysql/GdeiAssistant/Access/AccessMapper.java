package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Access;

import edu.gdei.gdeiassistant.Pojo.Entity.Access;

public interface AccessMapper {

    public Access selectAndroidAccess(Integer groupId) throws Exception;

    public Access selectWechatAccess(Integer groupId) throws Exception;
}
