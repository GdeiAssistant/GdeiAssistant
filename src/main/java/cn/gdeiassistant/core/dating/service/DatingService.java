package cn.gdeiassistant.core.dating.service;

import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.exception.DatingException.RepeatPickException;
import cn.gdeiassistant.common.exception.DatingException.SelfPickException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.dating.mapper.DatingMapper;
import cn.gdeiassistant.core.message.service.InteractionNotificationService;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPickSubmitDTO;
import cn.gdeiassistant.core.dating.pojo.dto.DatingPublishDTO;
import cn.gdeiassistant.core.dating.pojo.entity.DatingPickEntity;
import cn.gdeiassistant.core.dating.pojo.entity.DatingProfileEntity;
import cn.gdeiassistant.core.dating.pojo.vo.DatingPickVO;
import cn.gdeiassistant.core.dating.pojo.vo.DatingProfileVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class DatingService {

    private static final Logger logger = LoggerFactory.getLogger(DatingService.class);
    @Autowired
    private UserCertificateService userCertificateService;

    @Resource(name = "datingMapper")
    private DatingMapper datingMapper;

    @Autowired
    private R2StorageService r2StorageService;

    @Autowired
    private InteractionNotificationService interactionNotificationService;

    public DatingProfileVO queryDatingProfile(Integer id) throws DataNotExistException {
        DatingProfileEntity entity = datingMapper.selectDatingProfileById(id);
        if (entity == null) throw new DataNotExistException("该卖室友信息不存在");
        return profileEntityToVO(entity);
    }

    public List<DatingProfileVO> queryDatingProfile(Integer start, Integer size, Integer area) {
        List<DatingProfileEntity> list = datingMapper.selectDatingProfilePage(start, size, area);
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> {
            DatingProfileVO vo = profileEntityToVO(e);
            vo.setQq("请进入详情页查看");
            vo.setWechat("请进入详情页查看");
            return vo;
        }).collect(Collectors.toList());
    }

    public List<DatingProfileVO> queryMyRoommateProfiles(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<DatingProfileEntity> list = datingMapper.selectDatingProfileByUsername(user.getUsername());
        if (list == null) return new ArrayList<>();
        return list.stream()
                .filter(e -> e.getState() == null || e.getState() != 0)
                .map(e -> {
                    DatingProfileVO vo = profileEntityToVO(e);
                    vo.setPictureURL(getRoommateProfilePictureURL(e.getProfileId()));
                    return vo;
                }).collect(Collectors.toList());
    }

    public void updateRoommateProfile(DatingPublishDTO dto, Integer profileId) {
        DatingProfileEntity entity = datingMapper.selectDatingProfileById(profileId);
        if (entity == null) return;
        dtoToProfileEntity(dto, entity);
        datingMapper.updateRoommateProfile(entity);
    }

    public void updateRoommateProfileState(Integer id, Integer state) {
        datingMapper.updateRoommateProfileState(id, state);
    }

    public void verifyRoommateProfileOwner(String sessionId, Integer profileId) throws DataNotExistException, NoAccessException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingProfileEntity profile = datingMapper.selectDatingProfileById(profileId);
        if (profile == null) throw new DataNotExistException("该卖室友信息不存在");
        if (!user.getUsername().equals(profile.getUsername())) throw new NoAccessException("无权限操作");
    }

    public Integer addRoommateProfile(String sessionId, DatingPublishDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingProfileEntity entity = new DatingProfileEntity();
        entity.setUsername(user.getUsername());
        dtoToProfileEntity(dto, entity);
        datingMapper.insertRoommateProfile(entity);
        return entity.getProfileId();
    }

    public void uploadPicture(int id, InputStream inputStream) {
        try {
            r2StorageService.uploadObject("gdeiassistant-userdata", "dating/" + id + ".jpg", inputStream);
        } catch (Exception e) {
            logger.error("上传室友信息图片失败，id={}", id, e);
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException ignored) {}
            }
        }
    }

    public void movePictureFromTempObject(int id, String objectKey) {
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "dating/" + id + ".jpg");
    }

    public DatingPickVO queryRoommatePick(Integer profileId, String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingPickEntity entity = datingMapper.selectDatingPick(profileId, user.getUsername());
        return entity == null ? null : pickEntityToVO(entity);
    }

    public boolean checkIsPickPageHidden(String sessionId, int pickId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingPickEntity entity = queryRoommatePickById(pickId);
        if (entity != null) {
            if (Integer.valueOf(1).equals(entity.getState())) return true;
            return entity.getUsername().equals(user.getUsername());
        }
        return false;
    }

    public DatingPickEntity queryRoommatePickById(int id) {
        return datingMapper.selectDatingPickById(id);
    }

    /** 按 ID 查询撩一下记录并转为 VO（供 Controller 详情页使用） */
    public DatingPickVO getRoommatePickDetailVo(int id) {
        DatingPickEntity e = datingMapper.selectDatingPickById(id);
        return e == null ? null : pickEntityToVO(e);
    }

    /** 提交撩一下前校验：非本人发布、未重复撩。 */
    public void verifyRoommatePickRequestAccess(String sessionId, Integer profileId) throws RepeatPickException, SelfPickException {
        if (profileId == null) return;
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingProfileEntity profile = datingMapper.selectDatingProfileById(profileId);
        if (profile != null && user.getUsername().equals(profile.getUsername()))
            throw new SelfPickException("不能向自己发布的卖室友信息发送撩一下请求");
        DatingPickEntity existing = datingMapper.selectDatingPick(profileId, user.getUsername());
        if (existing != null && !Integer.valueOf(-1).equals(existing.getState()))
            throw new RepeatPickException("重复的撩一下请求");
    }

    public void verifyRoommatePickViewAccess(String sessionId, int id) throws DataNotExistException, NoAccessException {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingPickEntity entity = datingMapper.selectDatingPickById(id);
        if (entity == null) throw new DataNotExistException("该撩一下信息不存在");
        if (entity.getRoommateProfile() != null && entity.getRoommateProfile().getUsername().equals(user.getUsername()))
            return;
        throw new NoAccessException("没有权限查看该撩一下信息");
    }

    @Transactional("appTransactionManager")
    public void addRoommatePick(String sessionId, DatingPickSubmitDTO dto) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        DatingPickEntity pick = new DatingPickEntity();
        DatingProfileEntity ref = new DatingProfileEntity();
        ref.setProfileId(dto.getProfileId());
        pick.setRoommateProfile(ref);
        pick.setUsername(user.getUsername());
        pick.setContent(dto.getContent());
        datingMapper.insertRoommatePick(pick);
        DatingProfileEntity profile = datingMapper.selectDatingProfileById(dto.getProfileId());
        interactionNotificationService.createInteractionNotification(
                "dating",
                "pick_received",
                profile != null ? profile.getUsername() : null,
                user.getUsername(),
                toStringValue(pick.getPickId()),
                toStringValue(profile != null ? profile.getProfileId() : null),
                "received",
                "收到新的撩一下",
                StringUtils.isNotBlank(dto.getContent())
                        ? user.getUsername() + " 向你发起了撩一下：" + dto.getContent()
                        : user.getUsername() + " 向你发起了撩一下"
        );
    }

    @Transactional("appTransactionManager")
    public void updateRoommatePickState(Integer id, Integer state) throws DataNotExistException, NoAccessException {
        DatingPickEntity entity = datingMapper.selectDatingPickById(id);
        if (entity == null) throw new DataNotExistException("该撩一下记录不存在");
        if (Integer.valueOf(0).equals(entity.getState())) {
            datingMapper.updateRoommatePickState(id, state);
            String nickname = entity.getRoommateProfile() != null && StringUtils.isNotBlank(entity.getRoommateProfile().getNickname())
                    ? entity.getRoommateProfile().getNickname()
                    : "对方";
            boolean accepted = Integer.valueOf(1).equals(state);
            interactionNotificationService.createInteractionNotification(
                    "dating",
                    accepted ? "pick_accepted" : "pick_rejected",
                    entity.getUsername(),
                    entity.getRoommateProfile() != null ? entity.getRoommateProfile().getUsername() : null,
                    toStringValue(entity.getPickId()),
                    toStringValue(entity.getRoommateProfile() != null ? entity.getRoommateProfile().getProfileId() : null),
                    "sent",
                    accepted ? "撩一下已通过" : "撩一下未通过",
                    accepted ? nickname + " 通过了你的请求" : nickname + " 拒绝了你的请求"
            );
            return;
        }
        throw new NoAccessException("该撩一下记录已处理，请勿重复提交");
    }

    public List<DatingPickVO> queryMySentPicks(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<DatingPickEntity> list = datingMapper.selectDatingPickListByUsername(user.getUsername());
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> {
            DatingPickVO vo = pickEntityToVO(e);
            if (e.getRoommateProfile() != null && e.getRoommateProfile().getProfileId() != null)
                vo.getRoommateProfile().setPictureURL(getRoommateProfilePictureURL(e.getRoommateProfile().getProfileId()));
            return vo;
        }).collect(Collectors.toList());
    }

    public List<DatingPickVO> queryMyReceivedPicks(String sessionId) {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<DatingPickEntity> list = datingMapper.selectReceivedRoommatePickListByProfileOwner(user.getUsername());
        if (list == null) return new ArrayList<>();
        return list.stream().map(e -> {
            DatingPickVO vo = pickEntityToVO(e);
            if (e.getRoommateProfile() != null && e.getRoommateProfile().getProfileId() != null) {
                vo.getRoommateProfile().setPictureURL(getRoommateProfilePictureURL(e.getRoommateProfile().getProfileId()));
            }
            return vo;
        }).collect(Collectors.toList());
    }

    public String getRoommateProfilePictureURL(int id) {
        return r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "dating/" + id + ".jpg", 30, TimeUnit.MINUTES);
    }

    private void dtoToProfileEntity(DatingPublishDTO dto, DatingProfileEntity entity) {
        entity.setNickname(dto.getNickname());
        entity.setGrade(dto.getGrade());
        entity.setFaculty(dto.getFaculty());
        entity.setHometown(dto.getHometown());
        entity.setContent(dto.getContent());
        entity.setQq(dto.getQq());
        entity.setWechat(dto.getWechat());
        entity.setArea(dto.getArea());
    }

    private DatingProfileVO profileEntityToVO(DatingProfileEntity e) {
        DatingProfileVO vo = new DatingProfileVO();
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

    private DatingPickVO pickEntityToVO(DatingPickEntity e) {
        DatingPickVO vo = new DatingPickVO();
        vo.setPickId(e.getPickId());
        vo.setUsername(e.getUsername());
        vo.setContent(e.getContent());
        vo.setState(e.getState());
        if (e.getRoommateProfile() != null) vo.setRoommateProfile(profileEntityToVO(e.getRoommateProfile()));
        return vo;
    }

    private String toStringValue(Integer value) {
        return value == null ? null : String.valueOf(value);
    }
}
