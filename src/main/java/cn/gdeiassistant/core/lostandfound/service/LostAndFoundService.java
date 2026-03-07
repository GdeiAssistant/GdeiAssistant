package cn.gdeiassistant.core.lostandfound.service;

import cn.gdeiassistant.common.exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.lostandfound.converter.LostAndFoundDetailConverter;
import cn.gdeiassistant.core.lostandfound.converter.LostAndFoundItemConverter;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.dto.LostAndFoundPublishDTO;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundDetailEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundDetailVO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class LostAndFoundService {

    private static final Logger logger = LoggerFactory.getLogger(LostAndFoundService.class);
    @Resource(name = "lostAndFoundMapper")
    private LostAndFoundMapper lostAndFoundMapper;

    @Autowired
    private LostAndFoundItemConverter lostAndFoundItemConverter;

    @Autowired
    private LostAndFoundDetailConverter lostAndFoundDetailConverter;

    @Autowired
    private UserCertificateService userCertificateService;

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private R2StorageService r2StorageService;

    public LostAndFoundDetailVO queryLostAndFoundInfoByID(int id) throws Exception {
        LostAndFoundDetailEntity detail = lostAndFoundMapper.selectInfoByID(id);
        if (detail == null || detail.getItem() == null) {
            throw new DataNotExistException("失物招领信息不存在");
        }
        LostAndFoundItemEntity item = detail.getItem();
        if (Integer.valueOf(1).equals(item.getState())) {
            throw new ConfirmedStateException("物品已确认寻回，不可再次编辑和查看");
        }
        String username = item.getUsername();
        List<String> pictureURL = getLostAndFoundItemPictureURL(item.getId());
        item.setPictureURL(pictureURL);
        LostAndFoundDetailVO vo = lostAndFoundDetailConverter.toVO(detail);
        vo.getProfile().setUsername(username);
        vo.getProfile().setAvatarURL(userProfileService.getOtherUserAvatar(username));
        return vo;
    }

    public List<LostAndFoundItemVO> queryPersonalLostAndFoundItems(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<LostAndFoundItemEntity> list = lostAndFoundMapper.selectItemByUsername(user.getUsername());
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<LostAndFoundItemVO> voList = new ArrayList<>();
        for (LostAndFoundItemEntity e : list) {
            e.setUsername(user.getUsername());
            voList.add(lostAndFoundItemConverter.toVO(e));
        }
        return voList;
    }

    public void verifyLostAndFoundInfoEditAccess(String sessionId, int id) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        LostAndFoundDetailEntity detail = lostAndFoundMapper.selectInfoByID(id);
        if (detail != null && detail.getItem() != null) {
            if (detail.getItem().getUsername().equals(user.getUsername())) return;
            throw new NoAccessException("没有权限编辑该失物招领信息");
        }
        throw new DataNotExistException("失物招领信息不存在");
    }

    public List<LostAndFoundItemVO> queryLostItems(int start) throws Exception {
        return toItemVOList(lostAndFoundMapper.selectAvailableItem(0, start, 10));
    }

    public List<LostAndFoundItemVO> queryFoundItems(int start) throws Exception {
        return toItemVOList(lostAndFoundMapper.selectAvailableItem(1, start, 10));
    }

    public List<LostAndFoundItemVO> queryLostItemsWithKeyword(String keyword, int start) throws Exception {
        return toItemVOList(lostAndFoundMapper.selectItemWithKeyword(0, keyword, start, 10));
    }

    public List<LostAndFoundItemVO> queryFoundItemsWithKeyword(String keyword, int start) throws Exception {
        return toItemVOList(lostAndFoundMapper.selectItemWithKeyword(1, keyword, start, 10));
    }

    public List<LostAndFoundItemVO> queryLostItemsByType(int type, int start) throws Exception {
        return toItemVOList(lostAndFoundMapper.selectItemByItemType(0, type, start, 10));
    }

    public List<LostAndFoundItemVO> queryFoundItemsByType(int type, int start) throws Exception {
        return toItemVOList(lostAndFoundMapper.selectItemByItemType(1, type, start, 10));
    }

    public LostAndFoundItemVO addLostAndFoundItem(LostAndFoundPublishDTO dto, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        LostAndFoundItemEntity item = dtoToEntity(dto);
        item.setUsername(user.getUsername());
        item.setPublishTime(new Date());
        item.setState(0);
        lostAndFoundMapper.insertItem(item);
        return lostAndFoundItemConverter.toVO(item);
    }

    public void updateLostAndFoundItem(LostAndFoundPublishDTO dto, int id) throws Exception {
        LostAndFoundItemEntity item = dtoToEntity(dto);
        item.setId(id);
        LostAndFoundDetailEntity detail = lostAndFoundMapper.selectInfoByID(id);
        if (detail == null || detail.getItem() == null) {
            throw new DataNotExistException("查找的失物招领信息不存在");
        }
        if (!Integer.valueOf(1).equals(detail.getItem().getState())) {
            lostAndFoundMapper.updateItemItem(item);
            return;
        }
        throw new ConfirmedStateException("物品已确认寻回，不可再次编辑");
    }

    public void updateLostAndFoundItemState(int id, int state) throws Exception {
        LostAndFoundDetailEntity detail = lostAndFoundMapper.selectInfoByID(id);
        if (detail == null || detail.getItem() == null) {
            throw new DataNotExistException("查找的失物招领信息不存在");
        }
        if (!Integer.valueOf(1).equals(detail.getItem().getState())) {
            lostAndFoundMapper.updateItemState(id, state);
            return;
        }
        throw new ConfirmedStateException("物品已确认寻回，不可再次编辑");
    }

    public void uploadLostAndFoundItemPicture(int id, int index, InputStream inputStream) {
        r2StorageService.uploadObject("gdeiassistant-userdata", "lostandfound/" + id + "_" + index + ".jpg", inputStream);
        try {
            if (inputStream != null) inputStream.close();
        } catch (IOException e) {
            logger.error("关闭失物招领图片上传流失败，id={}, index={}", id, index, e);
        }
    }

    public void moveLostAndFoundItemPictureFromTempObject(int id, int index, String objectKey) {
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "lostandfound/" + id + "_" + index + ".jpg");
    }

    public List<String> getLostAndFoundItemPictureURL(int id) {
        List<String> pictureURL = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String url = r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "lostandfound/" + id + "_" + i + ".jpg", 30, TimeUnit.MINUTES);
            if (StringUtils.isNotBlank(url)) pictureURL.add(url);
            else break;
        }
        return pictureURL;
    }

    private List<LostAndFoundItemVO> toItemVOList(List<LostAndFoundItemEntity> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        return lostAndFoundItemConverter.toVOList(list);
    }

    private static LostAndFoundItemEntity dtoToEntity(LostAndFoundPublishDTO dto) {
        LostAndFoundItemEntity e = new LostAndFoundItemEntity();
        e.setName(dto.getName());
        e.setDescription(dto.getDescription());
        e.setLocation(dto.getLocation());
        e.setItemType(dto.getItemType());
        e.setLostType(dto.getLostType());
        e.setQq(dto.getQq());
        e.setWechat(dto.getWechat());
        e.setPhone(dto.getPhone());
        return e;
    }
}
