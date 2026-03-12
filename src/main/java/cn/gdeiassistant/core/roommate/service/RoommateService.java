package cn.gdeiassistant.core.roommate.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatingException.RepeatPickException;
import cn.gdeiassistant.common.exception.DatingException.SelfPickException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.core.roommate.mapper.RoommateMapper;
import cn.gdeiassistant.core.roommate.pojo.dto.RoommatePickSubmitDTO;
import cn.gdeiassistant.core.roommate.pojo.dto.RoommatePublishDTO;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateMessageEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommatePickEntity;
import cn.gdeiassistant.core.roommate.pojo.entity.RoommateProfileEntity;
import cn.gdeiassistant.core.roommate.pojo.vo.RoommateMessageVO;
import cn.gdeiassistant.core.roommate.pojo.vo.RoommatePickVO;
import cn.gdeiassistant.core.roommate.pojo.vo.RoommateProfileVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RoommateService {

    private static final Logger logger = LoggerFactory.getLogger(RoommateService.class);
    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "roommateMapper")
    private RoommateMapper roommateMapper;

    @Autowired
    private R2StorageService r2StorageService;

    public RoommateProfileVO queryRoommateProfile(Integer id) throws DataNotExistException {
        RoommateProfileEntity entity = roommateMapper.selectRoommateProfileById(id);
        if (entity == null) throw new DataNotExistException("该卖室友信息不存在");
        return profileEntityToVO(entity);
    }

    public List<RoommateProfileVO> queryRoommateProfile(Integer start, Integer size, Integer area) {
        List<RoommateProfileEntity> list = roommateMapper.selectRoommateProfilePage(start, size, area);
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> {
            RoommateProfileVO vo = profileEntityToVO(e);
            vo.setQq("请进入详情页查看");
            vo.setWechat("请进入详情页查看");
            return vo;
        }).collect(Collectors.toList());
    }

    public List<RoommateProfileVO> queryMyRoommateProfiles(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<RoommateProfileEntity> list = roommateMapper.selectRoommateProfileByUsername(user.getUsername());
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> {
            RoommateProfileVO vo = profileEntityToVO(e);
            vo.setPictureURL(getRoommateProfilePictureURL(e.getProfileId()));
            return vo;
        }).collect(Collectors.toList());
    }

    public void updateRoommateProfile(RoommatePublishDTO dto, Integer profileId) {
        RoommateProfileEntity entity = roommateMapper.selectRoommateProfileById(profileId);
        if (entity == null) return;
        dtoToProfileEntity(dto, entity);
        roommateMapper.updateRoommateProfile(entity);
    }

    public void updateRoommateProfileState(Integer id, Integer state) {
        roommateMapper.updateRoommateProfileState(id, state);
    }

    public void verifyRoommateProfileOwner(String sessionId, Integer profileId) throws DataNotExistException, NoAccessException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommateProfileEntity profile = roommateMapper.selectRoommateProfileById(profileId);
        if (profile == null) throw new DataNotExistException("该卖室友信息不存在");
        if (!user.getUsername().equals(profile.getUsername())) throw new NoAccessException("无权限操作");
    }

    public Integer addRoommateProfile(String sessionId, RoommatePublishDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommateProfileEntity entity = new RoommateProfileEntity();
        entity.setUsername(user.getUsername());
        dtoToProfileEntity(dto, entity);
        roommateMapper.insertRoommateProfile(entity);
        return entity.getProfileId();
    }

    public void uploadPicture(int id, InputStream inputStream) {
        r2StorageService.uploadObject("gdeiassistant-userdata", "dating/" + id + ".jpg", inputStream);
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            logger.error("关闭室友信息图片上传流失败，id={}", id, e);
        }
    }

    public void movePictureFromTempObject(int id, String objectKey) {
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "dating/" + id + ".jpg");
    }

    public RoommatePickVO queryRoommatePick(Integer profileId, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommatePickEntity entity = roommateMapper.selectRoommatePick(profileId, user.getUsername());
        return entity == null ? null : pickEntityToVO(entity);
    }

    public boolean checkIsPickPageHidden(String sessionId, int pickId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommatePickEntity entity = queryRoommatePickById(pickId);
        if (entity != null) {
            if (Integer.valueOf(1).equals(entity.getState())) return true;
            return entity.getUsername().equals(user.getUsername());
        }
        return false;
    }

    public RoommatePickEntity queryRoommatePickById(int id) {
        return roommateMapper.selectRoommatePickById(id);
    }

    /** 按 ID 查询撩一下记录并转为 VO（供 Controller 详情页使用） */
    public RoommatePickVO getRoommatePickDetailVo(int id) {
        RoommatePickEntity e = roommateMapper.selectRoommatePickById(id);
        return e == null ? null : pickEntityToVO(e);
    }

    /** 提交撩一下前校验：非本人发布、未重复撩。 */
    public void verifyRoommatePickRequestAccess(String sessionId, Integer profileId) throws RepeatPickException, SelfPickException {
        if (profileId == null) return;
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommateProfileEntity profile = roommateMapper.selectRoommateProfileById(profileId);
        if (profile != null && user.getUsername().equals(profile.getUsername()))
            throw new SelfPickException("不能向自己发布的卖室友信息发送撩一下请求");
        RoommatePickEntity existing = roommateMapper.selectRoommatePick(profileId, user.getUsername());
        if (existing != null && !Integer.valueOf(-1).equals(existing.getState()))
            throw new RepeatPickException("重复的撩一下请求");
    }

    public void verifyRoommatePickViewAccess(String sessionId, int id) throws DataNotExistException, NoAccessException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommatePickEntity entity = roommateMapper.selectRoommatePickById(id);
        if (entity == null) throw new DataNotExistException("该撩一下信息不存在");
        if (entity.getRoommateProfile() != null && entity.getRoommateProfile().getUsername().equals(user.getUsername()))
            return;
        throw new NoAccessException("没有权限查看该撩一下信息");
    }

    public void addRoommatePick(String sessionId, RoommatePickSubmitDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        RoommatePickEntity pick = new RoommatePickEntity();
        RoommateProfileEntity ref = new RoommateProfileEntity();
        ref.setProfileId(dto.getProfileId());
        pick.setRoommateProfile(ref);
        pick.setUsername(user.getUsername());
        pick.setContent(dto.getContent());
        roommateMapper.insertRoommatePick(pick);
        RoommateProfileEntity profile = roommateMapper.selectRoommateProfileById(dto.getProfileId());
        RoommateMessageEntity msg = new RoommateMessageEntity();
        msg.setUsername(profile.getUsername());
        msg.setRoommatePick(pick);
        msg.setType(0);
        msg.setState(0);
        roommateMapper.insertRoommateMessage(msg);
    }

    public void updateRoommatePickState(Integer id, Integer state) throws DataNotExistException, NoAccessException {
        RoommatePickEntity entity = roommateMapper.selectRoommatePickById(id);
        if (entity == null) throw new DataNotExistException("该撩一下记录不存在");
        if (Integer.valueOf(0).equals(entity.getState())) {
            roommateMapper.updateRoommatePickState(id, state);
            RoommateMessageEntity msg = new RoommateMessageEntity();
            msg.setUsername(entity.getUsername());
            msg.setType(1);
            msg.setRoommatePick(entity);
            msg.setState(0);
            roommateMapper.insertRoommateMessage(msg);
            return;
        }
        throw new NoAccessException("该撩一下记录已处理，请勿重复提交");
    }

    public List<RoommatePickVO> queryMySentPicks(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<RoommatePickEntity> list = roommateMapper.selectRoommatePickListByUsername(user.getUsername());
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> {
            RoommatePickVO vo = pickEntityToVO(e);
            if (e.getRoommateProfile() != null && e.getRoommateProfile().getProfileId() != null)
                vo.getRoommateProfile().setPictureURL(getRoommateProfilePictureURL(e.getRoommateProfile().getProfileId()));
            return vo;
        }).collect(Collectors.toList());
    }

    public List<RoommateMessageVO> queryUserRoommateMessage(String sessionId, Integer start, Integer size) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<RoommateMessageEntity> list = roommateMapper.selectUserRoommateMessagePage(user.getUsername(), start, size);
        if (list == null) return new ArrayList<>();
        return list.stream().map(this::messageEntityToVO).collect(Collectors.toList());
    }

    public Integer queryUserUnReadRoommateMessageCount(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        Integer n = roommateMapper.selectUserUnReadRoommateMessageCount(user.getUsername());
        return n != null ? n : 0;
    }

    public void updateRoommateMessageState(Integer id, Integer state) {
        roommateMapper.updateRoommateMessageState(id, state);
    }

    public String getRoommateProfilePictureURL(int id) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "dating/" + id + ".jpg", 30, TimeUnit.MINUTES);
    }

    private void dtoToProfileEntity(RoommatePublishDTO dto, RoommateProfileEntity entity) {
        entity.setNickname(dto.getNickname());
        entity.setGrade(dto.getGrade());
        entity.setFaculty(dto.getFaculty());
        entity.setHometown(dto.getHometown());
        entity.setContent(dto.getContent());
        entity.setQq(dto.getQq());
        entity.setWechat(dto.getWechat());
        entity.setArea(dto.getArea());
    }

    private RoommateProfileVO profileEntityToVO(RoommateProfileEntity e) {
        RoommateProfileVO vo = new RoommateProfileVO();
        vo.setProfileId(e.getProfileId());
        vo.setUsername(e.getUsername());
        vo.setNickname(e.getNickname());
        vo.setGrade(e.getGrade());
        vo.setFaculty(e.getFaculty());
        vo.setHometown(e.getHometown());
        vo.setContent(e.getContent());
        vo.setQq(e.getQq());
        vo.setWechat(e.getWechat());
        vo.setArea(e.getArea());
        vo.setState(e.getState());
        return vo;
    }

    private RoommatePickVO pickEntityToVO(RoommatePickEntity e) {
        RoommatePickVO vo = new RoommatePickVO();
        vo.setPickId(e.getPickId());
        vo.setUsername(e.getUsername());
        vo.setContent(e.getContent());
        vo.setState(e.getState());
        if (e.getRoommateProfile() != null) vo.setRoommateProfile(profileEntityToVO(e.getRoommateProfile()));
        return vo;
    }

    private RoommateMessageVO messageEntityToVO(RoommateMessageEntity e) {
        RoommateMessageVO vo = new RoommateMessageVO();
        vo.setMessageId(e.getMessageId());
        vo.setUsername(e.getUsername());
        vo.setType(e.getType());
        vo.setState(e.getState());
        vo.setCreateTime(e.getCreateTime());
        if (e.getRoommatePick() != null) vo.setRoommatePick(pickEntityToVO(e.getRoommatePick()));
        return vo;
    }
}
