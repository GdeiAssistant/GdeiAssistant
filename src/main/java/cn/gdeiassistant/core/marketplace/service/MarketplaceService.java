package cn.gdeiassistant.core.marketplace.service;

import cn.gdeiassistant.common.exception.databaseexception.ConfirmedStateException;
import cn.gdeiassistant.common.exception.databaseexception.DataNotExistException;
import cn.gdeiassistant.common.exception.databaseexception.NoAccessException;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.dto.MarketplacePublishDTO;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import cn.gdeiassistant.common.pojo.entity.User;
import cn.gdeiassistant.common.tools.springutils.R2StorageService;
import cn.gdeiassistant.core.user.service.PublicAuthorResolver;
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
public class MarketplaceService {

    private static final Logger logger = LoggerFactory.getLogger(MarketplaceService.class);
    @Resource(name = "marketplaceMapper")
    private MarketplaceMapper marketplaceMapper;

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

    public boolean ownedByCurrentUser(String sessionId, MarketplaceItemEntity item) throws Exception {
        if (sessionId == null) return false;
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        return user != null && java.util.Objects.equals(item.getUsername(), user.getUsername());
    }

    public MarketplaceItemVO queryDetailById(int id) throws Exception {
        MarketplaceItemVO vo = marketplaceMapper.selectInfoByID(id);
        if (vo == null) {
            throw new DataNotExistException("二手交易商品不存在");
        }
        String campusUsername = vo.getMarketplaceItem().getUsername();
        PublicAuthorResolver.AuthorPublic author = publicAuthorResolver.resolve(campusUsername);
        int itemId = vo.getMarketplaceItem().getId();
        List<String> pictureURL = getItemPictureURL(itemId);
        vo.getMarketplaceItem().setAuthorId(author.authorId());
        vo.getMarketplaceItem().setDisplayName(author.displayName());
        vo.getMarketplaceItem().setPictureURL(pictureURL);
        vo.getProfile().setUsername(author.displayName());
        vo.getProfile().setAvatarURL(author.authorId() != null
                ? "/api/social/users/" + author.authorId() + "/avatar"
                : null);
        return vo;
    }

    public void verifyEditAccess(String sessionId, int id) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        MarketplaceItemVO vo = marketplaceMapper.selectInfoByID(id);
        if (vo != null) {
            if (vo.getMarketplaceItem().getUsername().equals(user.getUsername())) {
                if (!vo.getMarketplaceItem().getState().equals(2)) {
                    return;
                }
                throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
            }
            throw new NoAccessException("没有权限编辑该二手交易信息");
        }
        throw new DataNotExistException("二手交易商品不存在");
    }

    public List<MarketplaceItemEntity> queryPersonalItems(String sessionId, int start, int size) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<MarketplaceItemEntity> list = marketplaceMapper.selectItemsByUsername(user.getUsername(), start, size);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        applyPublicAuthors(list);
        for (MarketplaceItemEntity e : list) {
            e.setUsername(user.getUsername());
            e.setPictureURL(java.util.List.of(storedAssets.generatePresignedUrl(null, "ershou/" + e.getId() + "_1.jpg", 30, TimeUnit.MINUTES)));
        }
        return list;
    }

    public List<MarketplaceItemEntity> queryItems(int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceMapper.selectAvailableItems(start, 10);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        applyPublicAuthors(list);
        return list;
    }

    public List<MarketplaceItemEntity> queryItemsWithKeyword(String keyword, int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceMapper.selectItemsWithKeyword(start, 10, keyword);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        applyPublicAuthors(list);
        return list;
    }

    public List<MarketplaceItemEntity> queryItemsByType(int type, int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceMapper.selectItemsByType(start, 10, type);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        applyPublicAuthors(list);
        return list;
    }

    private void applyPublicAuthors(List<MarketplaceItemEntity> list) {
        var authors = publicAuthorResolver.resolveAll(list.stream().map(MarketplaceItemEntity::getUsername).toList());
        for (var item : list) {
            var author = authors.getOrDefault(item.getUsername(), new PublicAuthorResolver.AuthorPublic(null, "用户"));
            item.setAuthorId(author.authorId());
            item.setDisplayName(author.displayName());
        }
    }

    /** 发布：DTO -> Entity -> 持久化，返回带 id 的 Entity 供上传图片使用 */
    @Transactional
    public MarketplaceItemEntity publishItem(MarketplacePublishDTO dto, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        MarketplaceItemEntity entity = new MarketplaceItemEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice(dto.getPrice().setScale(2, java.math.RoundingMode.UNNECESSARY));
        entity.setLocation(dto.getLocation());
        entity.setType(dto.getType());
        entity.setQq(dto.getQq());
        entity.setPhone(dto.getPhone());
        entity.setUsername(user.getUsername());
        entity.setPublishTime(new Date());
        marketplaceMapper.insertItem(entity);
        return entity;
    }

    public void updateItem(String sessionId, MarketplacePublishDTO dto, int id) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        MarketplaceItemVO vo = marketplaceMapper.selectInfoByID(id);
        if (vo == null) {
            throw new DataNotExistException("查找的二手交易信息不存在");
        }
        if (vo.getMarketplaceItem().getUsername().equals(user.getUsername())) {
            if (!vo.getMarketplaceItem().getState().equals(2)) {
                MarketplaceItemEntity entity = new MarketplaceItemEntity();
                entity.setId(id);
                entity.setName(dto.getName());
                entity.setDescription(dto.getDescription());
                entity.setPrice(dto.getPrice());
                entity.setLocation(dto.getLocation());
                entity.setType(dto.getType());
                entity.setQq(dto.getQq());
                entity.setPhone(dto.getPhone());
                marketplaceMapper.updateItem(entity);
                return;
            }
            throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
        }
        throw new NoAccessException("没有权限修改该商品信息");
    }

    public void updateItemState(String sessionId, int id, int state) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        MarketplaceItemVO vo = marketplaceMapper.selectInfoByID(id);
        if (vo == null) {
            throw new DataNotExistException("查找的二手交易信息不存在");
        }
        if (vo.getMarketplaceItem().getUsername().equals(user.getUsername())) {
            if (!vo.getMarketplaceItem().getState().equals(2)) {
                marketplaceMapper.updateItemState(id, state);
                return;
            }
            throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
        }
        throw new NoAccessException("没有权限修改该商品信息");
    }

    public void uploadItemPicture(int id, int index, InputStream inputStream) {
        try {
            storedAssets.uploadObject(null, "ershou/" + id + "_" + index + ".jpg", inputStream);
        } catch (Exception e) {
            logger.error("上传二手交易图片失败，id={}，index={}", id, index, e);
            throw new RuntimeException("图片上传失败", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.warn("关闭二手交易图片上传输入流失败，id={}，index={}", id, index, e);
                }
            }
        }
    }

    public void moveItemPictureFromTempObject(int id, int index, String objectKey, String sessionId) {
        uploads.moveUpload(sessionId, objectKey, "ershou/" + id + "_" + index + ".jpg");
    }

    public void deleteItemImages(int id, int count) {
        for (int i = 1; i <= count; i++) {
            try {
                storedAssets.deleteObject(null, "ershou/" + id + "_" + i + ".jpg");
            } catch (Exception e) {
                logger.warn("删除二手交易图片失败，id={}，index={}", id, i, e);
            }
        }
    }

    public void deleteItem(int id) {
        marketplaceMapper.deleteItem(id);
    }

    public List<String> getItemPictureURL(int id) {
        List<String> pictureURL = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String url = storedAssets.generatePresignedUrl(null, "ershou/" + id + "_" + i + ".jpg"
                    , 30, TimeUnit.MINUTES);
            if (StringUtils.isNotBlank(url)) {
                pictureURL.add(url);
            } else {
                break;
            }
        }
        return pictureURL;
    }

    @org.springframework.transaction.annotation.Transactional(value="appTransactionManager", rollbackFor=Exception.class)
    public void publishItem(MarketplacePublishDTO dto, String sessionId,
            org.springframework.web.multipart.MultipartFile[] images, String[] imageKeys) throws Exception {
        var created = publishItem(dto, sessionId);
        if (imageKeys != null && imageKeys.length > 0) {
            for (int i=0; i<imageKeys.length; i++) moveItemPictureFromTempObject(created.getId(), i+1, imageKeys[i], sessionId);
        } else if (images != null) {
            int index = 1;
            for (var image : images) {
                if (image != null && !image.isEmpty()) uploadItemPicture(created.getId(), index++, image.getInputStream());
            }
        }
    }

}
