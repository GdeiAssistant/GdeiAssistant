package cn.gdeiassistant.core.marketplace.service;

import cn.gdeiassistant.common.exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.common.exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.common.exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import cn.gdeiassistant.core.profile.service.UserProfileService;
import cn.gdeiassistant.core.marketplace.mapper.MarketplaceMapper;
import cn.gdeiassistant.core.marketplace.pojo.dto.MarketplacePublishDTO;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.userLogin.service.UserCertificateService;
import cn.gdeiassistant.common.pojo.Entity.User;
import cn.gdeiassistant.common.tools.SpringUtils.R2StorageService;
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

    public MarketplaceItemVO queryDetailById(int id) throws Exception {
        MarketplaceItemVO vo = marketplaceMapper.selectInfoByID(id);
        if (vo == null) {
            throw new DataNotExistException("二手交易商品不存在");
        }
        String username = vo.getSecondhandItem().getUsername();
        int itemId = vo.getSecondhandItem().getId();
        List<String> pictureURL = getItemPictureURL(itemId);
        vo.getSecondhandItem().setUsername(username);
        vo.getSecondhandItem().setPictureURL(pictureURL);
        vo.getProfile().setUsername(username);
        vo.getProfile().setAvatarURL(userProfileService.getOtherUserAvatar(username));
        return vo;
    }

    public void verifyEditAccess(String sessionId, int id) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        MarketplaceItemVO vo = marketplaceMapper.selectInfoByID(id);
        if (vo != null) {
            if (vo.getSecondhandItem().getUsername().equals(user.getUsername())) {
                if (!vo.getSecondhandItem().getState().equals(2)) {
                    return;
                }
                throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
            }
            throw new NoAccessException("没有权限编辑该二手交易信息");
        }
        throw new DataNotExistException("二手交易商品不存在");
    }

    public List<MarketplaceItemEntity> queryPersonalItems(String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        List<MarketplaceItemEntity> list = marketplaceMapper.selectItemsByUsername(user.getUsername());
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        for (MarketplaceItemEntity e : list) {
            e.setUsername(user.getUsername());
            e.setPictureURL(getItemPictureURL(e.getId()));
        }
        return list;
    }

    public List<MarketplaceItemEntity> queryItems(int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceMapper.selectAvailableItems(start, 10);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list;
    }

    public List<MarketplaceItemEntity> queryItemsWithKeyword(String keyword, int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceMapper.selectItemsWithKeyword(start, 10, keyword);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list;
    }

    public List<MarketplaceItemEntity> queryItemsByType(int type, int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceMapper.selectItemsByType(start, 10, type);
        if (list == null || list.isEmpty()) {
            return new ArrayList<>();
        }
        return list;
    }

    /** 发布：DTO -> Entity -> 持久化，返回带 id 的 Entity 供上传图片使用 */
    @Transactional
    public MarketplaceItemEntity publishItem(MarketplacePublishDTO dto, String sessionId) throws Exception {
        User user = userCertificateService.getUserLoginCertificate(sessionId);
        MarketplaceItemEntity entity = new MarketplaceItemEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setPrice((float) (Math.round(dto.getPrice() * 100)) / 100);
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
        if (vo.getSecondhandItem().getUsername().equals(user.getUsername())) {
            if (!vo.getSecondhandItem().getState().equals(2)) {
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
        if (vo.getSecondhandItem().getUsername().equals(user.getUsername())) {
            if (!vo.getSecondhandItem().getState().equals(2)) {
                marketplaceMapper.updateItemState(id, state);
                return;
            }
            throw new ConfirmedStateException("已出售的二手交易信息不能再次编辑");
        }
        throw new NoAccessException("没有权限修改该商品信息");
    }

    public void uploadItemPicture(int id, int index, InputStream inputStream) {
        r2StorageService.uploadObject("gdeiassistant-userdata", "ershou/" + id + "_" + index + ".jpg", inputStream);
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            logger.error("关闭二手交易图片上传流失败，id={}，index={}", id, index, e);
        }
    }

    public void moveItemPictureFromTempObject(int id, int index, String objectKey) {
        r2StorageService.moveObject("gdeiassistant-userdata", objectKey, "ershou/" + id + "_" + index + ".jpg");
    }

    public List<String> getItemPictureURL(int id) {
        List<String> pictureURL = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String url = r2StorageService.generatePresignedUrl("gdeiassistant-userdata", "ershou/" + id + "_" + i + ".jpg"
                    , 30, TimeUnit.MINUTES);
            if (StringUtils.isNotBlank(url)) {
                pictureURL.add(url);
            } else {
                break;
            }
        }
        return pictureURL;
    }
}
