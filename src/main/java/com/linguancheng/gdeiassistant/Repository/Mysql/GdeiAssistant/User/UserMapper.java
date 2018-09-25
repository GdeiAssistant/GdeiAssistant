package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.User;

import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    public User selectUser(String username) throws Exception;

    public Integer selectDeletedUserCount(String username) throws Exception;

    public List<User> selectUserList(@Param("start") int start, @Param("size") int size) throws Exception;

    public Integer selectUserCount() throws Exception;

    public void updateUser(User user) throws Exception;

    public void insertUser(User user) throws Exception;

    public List<User> selectAllUser() throws Exception;

    public void closeUser(@Param("resetname") String resetname
            , @Param("username") String username) throws Exception;
}
