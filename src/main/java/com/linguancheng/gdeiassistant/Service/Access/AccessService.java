package com.linguancheng.gdeiassistant.Service.Access;

import com.linguancheng.gdeiassistant.Pojo.Entity.Access;
import com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Access.AccessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessService {

    @Autowired
    private AccessMapper accessMapper;

    public Access GetUserAndroidAccess(int id) throws Exception {
        return accessMapper.selectAndroidAccess(id);
    }

    public Access GetUserWechatAccess(int id) throws Exception {
        return accessMapper.selectWechatAccess(id);
    }
}
