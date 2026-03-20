package cn.gdeiassistant.core.marketplace.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.common.exception.DatabaseException.NotAvailableStateException;
import cn.gdeiassistant.core.marketplace.pojo.dto.MarketplacePublishDTO;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.marketplace.service.MarketplaceService;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.common.tools.Utils.StringUtils;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MarketplaceController {

    @Autowired
    private MarketplaceService marketplaceService;

    @RequestMapping(value = "/api/ershou/item/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<MarketplaceItemEntity>> getItemList(HttpServletRequest request, @PathVariable("start") int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceService.queryItems(start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/ershou/profile", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getMySecondhandItems(HttpServletRequest request) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        List<MarketplaceItemEntity> list = marketplaceService.queryPersonalItems(sessionId);
        List<MarketplaceItemEntity> doing = new ArrayList<>();
        List<MarketplaceItemEntity> sold = new ArrayList<>();
        List<MarketplaceItemEntity> off = new ArrayList<>();
        for (MarketplaceItemEntity item : list) {
            if (Integer.valueOf(1).equals(item.getState())) {
                doing.add(item);
            } else if (Integer.valueOf(0).equals(item.getState())) {
                sold.add(item);
            } else if (Integer.valueOf(2).equals(item.getState())) {
                off.add(item);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("doing", doing);
        data.put("sold", sold);
        data.put("off", off);
        return new DataJsonResult<>(true, data);
    }

    @RequestMapping(value = "/api/ershou/item", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addItem(HttpServletRequest request,
            @Validated MarketplacePublishDTO dto, MultipartFile image1,
            MultipartFile image2, MultipartFile image3, MultipartFile image4,
            @RequestParam(value = "imageKeys", required = false) String[] imageKeys) throws Exception {
        MultipartFile[] images = new MultipartFile[]{image1, image2, image3, image4};
        int uploadedFileCount = 0;
        for (MultipartFile image : images) {
            if (image != null && image.getSize() > 0 && image.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                uploadedFileCount++;
            }
        }
        int uploadedKeyCount = 0;
        if (imageKeys != null) {
            for (String imageKey : imageKeys) {
                if (StringUtils.isBlank(imageKey)) {
                    return new JsonResult(false, "不合法的图片文件");
                }
                uploadedKeyCount++;
            }
        }
        if (uploadedKeyCount > 4) {
            return new JsonResult(false, "不合法的图片文件");
        }
        if (uploadedFileCount == 0 && uploadedKeyCount == 0) {
            return new JsonResult(false, "不合法的图片文件");
        }
        if (uploadedKeyCount > 0 && uploadedFileCount > 0) {
            return new JsonResult(false, "不支持混合上传图片参数");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        MarketplaceItemEntity entity = marketplaceService.publishItem(dto, sessionId);
        if (uploadedFileCount > 0) {
            int imageIndex = 1;
            for (MultipartFile image : images) {
                if (image != null && image.getSize() > 0 && image.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                    marketplaceService.uploadItemPicture(entity.getId(), imageIndex++, image.getInputStream());
                }
            }
        } else {
            for (int i = 1; i <= imageKeys.length; i++) {
                marketplaceService.moveItemPictureFromTempObject(entity.getId(), i, imageKeys[i - 1]);
            }
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/ershou/keyword/{keyword}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<MarketplaceItemEntity>> getItemWithKeyword(HttpServletRequest request, @PathVariable("keyword") String keyword,
            @PathVariable("start") int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceService.queryItemsWithKeyword(keyword, start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/ershou/item/id/{id}/preview", method = RequestMethod.GET)
    public DataJsonResult<String> getItemPreviewImage(HttpServletRequest request, @PathVariable("id") int id) {
        List<String> list = marketplaceService.getItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            return new DataJsonResult<>(true, list.get(0));
        }
        return new DataJsonResult<>(false, "获取二手交易商品预览图失败");
    }

    @RequestMapping(value = "/api/ershou/item/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<MarketplaceItemVO> getItemDetail(@PathVariable("id") int id) throws Exception {
        MarketplaceItemVO vo = marketplaceService.queryDetailById(id);
        if (vo.getSecondhandItem().getState().equals(0)) {
            throw new NotAvailableStateException("已下架的二手交易信息不能查看");
        }
        if (vo.getSecondhandItem().getState().equals(2)) {
            throw new ConfirmedStateException("已出售的二手交易信息不能查看");
        }
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/ershou/item/type/{type}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<MarketplaceItemEntity>> getItemByType(HttpServletRequest request, @Validated @Range(min = 0, max = 11) @PathVariable("type") int type,
            @PathVariable("start") int start) throws Exception {
        List<MarketplaceItemEntity> list = marketplaceService.queryItemsByType(type, start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/ershou/item/id/{id}", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult updateItem(HttpServletRequest request, @Validated MarketplacePublishDTO dto,
            @PathVariable("id") int id) throws Exception {
        if (dto.getPrice() <= 0 || dto.getPrice() > 9999.99) {
            return new JsonResult(false, "商品价格不合法");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        marketplaceService.updateItem(sessionId, dto, id);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/ershou/item/state/id/{id}", method = RequestMethod.POST)
    public JsonResult updateItemState(HttpServletRequest request, @PathVariable("id") int id,
            @Validated @Range(min = 0, max = 2) int state) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        marketplaceService.updateItemState(sessionId, id, state);
        return new JsonResult(true);
    }
}
