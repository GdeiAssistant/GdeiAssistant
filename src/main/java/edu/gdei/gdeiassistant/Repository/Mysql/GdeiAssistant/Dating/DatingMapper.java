package edu.gdei.gdeiassistant.Repository.Mysql.GdeiAssistant.Dating;

import edu.gdei.gdeiassistant.Pojo.Entity.DatingMessage;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingPick;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingProfile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Deprecated
public interface DatingMapper {

    public List<DatingProfile> selectDatingProfilePage(@Param("start") Integer start
            , @Param("size") Integer size, @Param("area") Integer area);

    public DatingProfile selectDatingProfileById(Integer profileId);

    public List<DatingProfile> selectDatingProfileByUsername(String username);

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
