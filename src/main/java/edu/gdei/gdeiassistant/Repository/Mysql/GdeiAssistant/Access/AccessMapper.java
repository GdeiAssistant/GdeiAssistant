package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Access;

import edu.gdei.gdeiassistant.Pojo.Entity.Access;

public interface AccessMapper {

    public Access selectAccess(Integer groupId) throws Exception;
}
