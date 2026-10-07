package cn.gdeiassistant.core.lostandfound.service;

import cn.gdeiassistant.common.exception.databaseexception.ConfirmedStateException;
import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.databaseexception.NoAccessException;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.core.lostandfound.converter.LostAndFoundDetailConverter;
import cn.gdeiassistant.core.lostandfound.converter.LostAndFoundItemConverter;
import cn.gdeiassistant.core.lostandfound.mapper.LostAndFoundMapper;
import cn.gdeiassistant.core.lostandfound.pojo.dto.LostAndFoundPublishDTO;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundDetailEntity;
import cn.gdeiassistant.core.lostandfound.pojo.entity.LostAndFoundItemEntity;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundDetailVO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Autowired
    private cn.gdeiassistant.core.objectstorage.service.UploadService uploads;

    @Autowired
    private cn.gdeiassistant.core.objectstorage.service.StoredAssetService storedAssets;

    @Autowired
    private PublicAuthorResolver publicAuthorResolver;

    public LostAndFoundDetailVO queryLostAndFoundInfoByID(int id) throws Exception {
        LostAndFoundDetailEntity detail = lostAndFoundMapper.selectInfoByID(id);
        if (detail == null || detail.getItem() == null) {
            throw new DataNotExistException("失物招领信息不存在");
        }
        LostAndFoundItemEntity item = detail.getItem();
        if (Integer.valueOf(1).equals(item.getState())) {
            throw new ConfirmedStateException("物品已确认寻回，不可再次编辑和查看");
        }
        String campusUsername = item.getUsername();
        PublicAuthorResolver.AuthorPublic author = publicAuthorResolver.resolve(campusUsername);
        List<String> pictureURL = getLostAndFoundItemPictureURL(item.getId());
        item.setPictureURL(pictureURL);
        LostAndFoundDetailVO vo = lostAndFoundDetailConverter.toVO(detail);
        if (vo.getItem() != null) {
            vo.getItem().setAuthorId(author.authorId());
            vo.getItem().setUsername(author.displayName());
        }
        vo.getProfile().setUsername(author.displayName());
        vo.getProfile().setAvatarURL(author.authorId() != null
                ? "/api/social/users/" + author.authorId() + "/avatar"
                : null);
        return vo;
    }

    public List<LostAndFoundItemVO> queryPersonalLostAndFoundItems(String sessionId, int start, int size) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<LostAndFoundItemEntity> list = lostAndFoundMapper.selectItemByUsername(user.getUsername(), start, size);
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<LostAndFoundItemVO> voList = new ArrayList<>();
        for (LostAndFoundItemEntity e : list) {
            e.setUsername(user.getUsername());
            e.setPictureURL(java.util.List.of(storedAssets.generatePresignedUrl("lostandfound/" + e.getId() + "_1.jpg", 30, TimeUnit.MINUTES)));
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

    @Transactional
    public LostAndFoundItemVO addLostAndFoundItem(LostAndFoundPublishDTO dto, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        LostAndFoundItemEntity item = dtoToEntity(dto);
        item.setUsername(user.getUsername());
        item.setPublishTime(new Date());
        item.setState(0);
        lostAndFoundMapper.insertItem(item);
        return lostAndFoundItemConverter.toVO(item);
    }

    public void updateLostAndFoundItem(LostAndFoundPublishDTO dto, int id, String sessionId) throws Exception {
        verifyLostAndFoundInfoEditAccess(sessionId, id);
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
        try {
            storedAssets.uploadObject("lostandfound/" + id + "_" + index + ".jpg", inputStream);
        } catch (Exception e) {
            logger.error("上传失物招领图片失败，id={}，index={}", id, index, e);
            throw new RuntimeException("上传失败", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭失物招领图片上传输入流失败，id={}，index={}", id, index, e);
                }
            }
        }
    }

    public void moveLostAndFoundItemPictureFromTempObject(int id, int index, String objectKey, String sessionId) {
        uploads.moveUpload(sessionId, objectKey, "lostandfound/" + id + "_" + index + ".jpg");
    }

    public void deleteLostAndFoundItemImages(int id, int count) {
        for (int i = 1; i <= count; i++) {
            try {
                storedAssets.deleteObject("lostandfound/" + id + "_" + i + ".jpg");
            } catch (Exception e) {
                logger.warn("删除失物招领图片失败，id={}，index={}", id, i, e);
            }
        }
    }

    public void deleteLostAndFoundItem(int id) {
        lostAndFoundMapper.deleteItem(id);
    }

    public List<String> getLostAndFoundItemPictureURL(int id) {
        List<String> pictureURL = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String url = storedAssets.generatePresignedUrl("lostandfound/" + id + "_" + i + ".jpg", 30, TimeUnit.MINUTES);
            if (StringUtils.isNotBlank(url)) pictureURL.add(url);
            else break;
        }
        return pictureURL;
    }

    private List<LostAndFoundItemVO> toItemVOList(List<LostAndFoundItemEntity> list) {
        if (list == null || list.isEmpty()) return new ArrayList<>();
        List<LostAndFoundItemVO> vos = lostAndFoundItemConverter.toVOList(list);
        for (int i = 0; i < vos.size(); i++) {
            LostAndFoundItemEntity entity = list.get(i);
            PublicAuthorResolver.AuthorPublic author = publicAuthorResolver.resolve(entity.getUsername());
            LostAndFoundItemVO vo = vos.get(i);
            vo.setAuthorId(author.authorId());
            vo.setUsername(author.displayName());
        }
        return vos;
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
    @org.springframework.transaction.annotation.Transactional(value="appTransactionManager", rollbackFor=Exception.class)
    public void publishItem(LostAndFoundPublishDTO dto, String sessionId,
            org.springframework.web.multipart.MultipartFile[] images, String[] imageKeys) throws Exception {
        var created = addLostAndFoundItem(dto, sessionId);
        if (imageKeys != null && imageKeys.length > 0) {
            for (int i=0; i<imageKeys.length; i++) moveLostAndFoundItemPictureFromTempObject(created.getId(), i+1, imageKeys[i], sessionId);
        } else if (images != null) {
            int index = 1;
            for (var image : images) {
                if (image != null && !image.isEmpty()) uploadLostAndFoundItemPicture(created.getId(), index++, image.getInputStream());
            }
        }
    }

}
