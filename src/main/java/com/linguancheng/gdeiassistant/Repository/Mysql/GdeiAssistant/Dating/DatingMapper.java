package com.linguancheng.gdeiassistant.Repository.Mysql.GdeiAssistant.Dating;

import com.linguancheng.gdeiassistant.Pojo.Entity.DatingMessage;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingPick;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 广东二师助手团队 林冠成 版权所有
 * All rights reserved © 2016 - 2018
 * Author:林冠成
 * Date:2018/5/7
 */
public interface DatingMapper {

    public List<DatingProfile> selectDatingProfilePage(@Param("start") Integer start
            , @Param("size") Integer size, @Param("area") Integer area);

    public DatingProfile selectDatingProfileById(Integer profileId);

    public void updateDatingProfile(DatingProfile datingProfile);

    public void updateDatingProfileState(@Param("profileId") Integer profileId, @Param("state") Integer state);

    public Integer insertDatingProfile(DatingProfile datingProfile);

    public DatingPick selectDatingPickById(Integer pickId);

    public DatingPick selectDatingPick(@Param("profileId") Integer profileId, @Param("username") String username);

    public Integer insertDatingPick(DatingPick datingPick);

    public void updateDatingPickState(@Param("pickId") Integer pickId, @Param("state") Integer state);

    public List<DatingMessage> selectUserDatingMessagePage(@Param("username") String username
            , @Param("start") Integer start, @Param("size") Integer size);

    public Integer selectUserUnReadDatingMessageCount(String username);

    public void insertDatingMessage(DatingMessage datingMessage);

    public void updateDatingMessageState(@Param("messageId") Integer messageId, @Param("state") Integer state);
}
