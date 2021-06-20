package edu.gdei.gdeiassistant.Tools;

import edu.gdei.gdeiassistant.Enum.UserGroup.UserGroupEnum;
import edu.gdei.gdeiassistant.Pojo.Entity.Access;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.Access.AccessMapper;
import edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Mapper.User.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AccessUtils {

    private static UserMapper userMapper;

    private static AccessMapper accessMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        AccessUtils.userMapper = userMapper;
    }

    @Autowired
    public void setAccessMapper(AccessMapper accessMapper) {
        AccessUtils.accessMapper = accessMapper;
    }

    /**
     * 根据用户组枚举名称获取对应的ID值
     *
     * @param name
     * @return
     */
    public static int GetUserGroupIdByGroupEnumName(String name) {
        UserGroupEnum userGroupEnum = UserGroupEnum.getEnumByName(name);
        if (userGroupEnum == null) {
            return -1;
        }
        return userGroupEnum.getValue();
    }

    /**
     * 加载用户组权限信息
     *
     * @return
     * @throws Exception
     */
    public static List<Set<String>> LoadAccessInfo() throws Exception {
        List<Set<String>> info = new ArrayList<>();
        int count = userMapper.selectUserGroupCount();
        for (int i = 0; i <= count; i++) {
            info.add(new HashSet<>());
        }
        //获取权限信息列表
        List<Access> accessList = accessMapper.selectAllAccess();
        for (Access access : accessList) {
            info.get(access.getGroup()).add(access.getName());
        }
        return info;
    }
}
