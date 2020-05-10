package edu.gdei.gdeiassistant.Service.Access;

import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Repository.SQL.Mysql.Mapper.GdeiAssistant.Access.AccessMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AccessService {

    @Autowired
    private AccessMapper accessMapper;

    public Set<String> GetUserAccess(int id) throws Exception {
        Set<String> set = new HashSet<>();
        List<Access> accessList = accessMapper.selectAccess(id);
        for (Access access : accessList) {
            set.add(access.getName());
        }
        return set;
    }
}
