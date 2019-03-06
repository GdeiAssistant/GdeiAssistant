package edu.gdei.gdeiassistant.Service.Access;

import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Access.AccessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessService {

    @Autowired
    private AccessMapper accessMapper;

    public Access GetUserAccess(int id) throws Exception {
        return accessMapper.selectAccess(id);
    }
}
