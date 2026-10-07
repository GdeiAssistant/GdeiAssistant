package cn.gdeiassistant.core.marketplace.controller;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.ipaddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.databaseexception.ConfirmedStateException;
import cn.gdeiassistant.common.exception.databaseexception.NotAvailableStateException;
import cn.gdeiassistant.core.i18n.BackendTextLocalizer;
import cn.gdeiassistant.core.marketplace.pojo.dto.MarketplacePublishDTO;
import cn.gdeiassistant.core.marketplace.pojo.entity.MarketplaceItemEntity;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemVO;
import cn.gdeiassistant.core.marketplace.pojo.vo.MarketplaceItemResponse;
import cn.gdeiassistant.core.marketplace.service.MarketplaceService;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.common.tools.utils.PageUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class MarketplaceController {

    private static final BigDecimal MIN_PRICE = new BigDecimal("0.01");
    private static final BigDecimal MAX_PRICE = new BigDecimal("9999.99");

    /**
     * Per-group cap for the personal profile endpoint.
     * Prevents unbounded result sets for doing/sold/off groups.
     */
    @Autowired
    private MarketplaceService marketplaceService;

    @Autowired
    private cn.gdeiassistant.core.user.service.PublicAuthorResolver publicAuthorResolver;

    private MarketplaceItemResponse response(MarketplaceItemEntity item) {
        return new MarketplaceItemResponse(item.getId(), item.getAuthorId(),
                item.getDisplayName() == null ? "用户" : item.getDisplayName(),
                item.getName(), item.getDescription(), item.getPrice(), item.getLocation(), item.getType(),
                item.getQq(), item.getPhone(), item.getState(), item.getPublishTime(), item.getPictureURL());
    }

    public record MarketplaceMineResponse(List<MarketplaceItemResponse> doing,
            List<MarketplaceItemResponse> sold, List<MarketplaceItemResponse> off, boolean hasMore, Integer nextStart) {}
    public record MarketplaceAuthorResponse(String authorId, String displayName, String avatarURL) {}
    public record MarketplaceDetailResponse(MarketplaceItemResponse item, MarketplaceAuthorResponse profile, boolean ownedByCurrentUser) {}

    private String localize(HttpServletRequest request, String message) {
        return BackendTextLocalizer.localizeMessage(message, request != null ? request.getHeader("Accept-Language") : null);
    }

    private JsonResult failure(HttpServletRequest request, String message) {
        return new JsonResult(false, localize(request, message));
    }

    private boolean isInvalidPrice(BigDecimal price) {
        if (price == null) {
            return true;
        }
        BigDecimal value = price;
        return value.compareTo(MIN_PRICE) < 0 || value.compareTo(MAX_PRICE) > 0 || value.stripTrailingZeros().scale() > 2;
    }

    @RequestMapping(value = "/api/marketplace/item/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<MarketplaceItemResponse>> getItemList(@PathVariable("start") int start) throws Exception {
        start = PageUtils.requireNonNegativeStart(start);
        List<MarketplaceItemEntity> list = marketplaceService.queryItems(start);
        return new DataJsonResult<>(true, list.stream().map(this::response).toList());
    }

    @RequestMapping(value = "/api/marketplace/profile", method = RequestMethod.GET)
    public DataJsonResult<MarketplaceMineResponse> getMyMarketplaceItems(HttpServletRequest request, @RequestParam(value="start", defaultValue="0") int start) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        start = PageUtils.requireNonNegativeStart(start);
        var rows = marketplaceService.queryPersonalItems(sessionId, start, 26);
        boolean hasMore = rows.size() > 25;
        var list = rows.subList(0, Math.min(25, rows.size()));
        List<MarketplaceItemEntity> doing = new ArrayList<>();
        List<MarketplaceItemEntity> sold = new ArrayList<>();
        List<MarketplaceItemEntity> off = new ArrayList<>();
        for (MarketplaceItemEntity item : list) {
            if (Integer.valueOf(1).equals(item.getState())) {
                doing.add(item);
            } else if (Integer.valueOf(0).equals(item.getState())) {
                off.add(item);
            } else if (Integer.valueOf(2).equals(item.getState())) {
                sold.add(item);
            }
        }
        return new DataJsonResult<>(true, new MarketplaceMineResponse(
                doing.stream().map(this::response).toList(), sold.stream().map(this::response).toList(),
                off.stream().map(this::response).toList(), hasMore, hasMore ? start + 25 : null));
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/marketplace/item", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addItem(HttpServletRequest request,
            @Validated MarketplacePublishDTO dto, MultipartFile image1,
            MultipartFile image2, MultipartFile image3, MultipartFile image4,
            @RequestParam(value = "imageKeys", required = false) String[] imageKeys) throws Exception {
        if (isInvalidPrice(dto.getPrice())) {
            return failure(request, "商品价格不合法");
        }
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
                    return failure(request, "不合法的图片文件");
                }
                uploadedKeyCount++;
            }
        }
        if (uploadedKeyCount > 4) {
            return failure(request, "不合法的图片文件");
        }
        if (uploadedFileCount == 0 && uploadedKeyCount == 0) {
            return failure(request, "不合法的图片文件");
        }
        if (uploadedKeyCount > 0 && uploadedFileCount > 0) {
            return failure(request, "不支持混合上传图片参数");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        try {
            marketplaceService.publishItem(dto, sessionId, images, imageKeys);
        } catch (Exception failure) {
            return new JsonResult(false, BackendTextLocalizer.localizeMessage("上传失败", request.getHeader("Accept-Language")));
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/marketplace/keyword/{keyword}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<MarketplaceItemResponse>> getItemWithKeyword(@PathVariable("keyword") String keyword,
            @PathVariable("start") int start) throws Exception {
        start = PageUtils.requireNonNegativeStart(start);
        List<MarketplaceItemEntity> list = marketplaceService.queryItemsWithKeyword(keyword, start);
        return new DataJsonResult<>(true, list.stream().map(this::response).toList());
    }

    @RequestMapping(value = "/api/marketplace/item/id/{id}/preview", method = RequestMethod.GET)
    public DataJsonResult<String> getItemPreviewImage(HttpServletRequest request, @PathVariable("id") int id) {
        List<String> list = marketplaceService.getItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            return new DataJsonResult<>(true, list.get(0));
        }
        return new DataJsonResult<>(false, localize(request, "获取二手交易商品预览图失败"));
    }

    @RequestMapping(value = "/api/marketplace/item/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<MarketplaceDetailResponse> getItemDetail(HttpServletRequest request, @PathVariable("id") int id) throws Exception {
        MarketplaceItemVO vo = marketplaceService.queryDetailById(id);
        if (vo.getMarketplaceItem().getState().equals(0)) {
            throw new NotAvailableStateException("已下架的二手交易信息不能查看");
        }
        if (vo.getMarketplaceItem().getState().equals(2)) {
            throw new ConfirmedStateException("已出售的二手交易信息不能查看");
        }
        MarketplaceItemResponse item = response(vo.getMarketplaceItem());
        return new DataJsonResult<>(true, new MarketplaceDetailResponse(item,
                new MarketplaceAuthorResponse(item.authorId(), item.displayName(),
                        item.authorId() == null ? null : "/api/social/users/" + item.authorId() + "/avatar"), marketplaceService.ownedByCurrentUser(
                        (String) request.getAttribute("sessionId"), vo.getMarketplaceItem())));
    }

    @RequestMapping(value = "/api/marketplace/item/type/{type}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<MarketplaceItemResponse>> getItemByType(@Validated @Range(min = 0, max = 11) @PathVariable("type") int type,
            @PathVariable("start") int start) throws Exception {
        if (type < 0 || type > 11) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        start = PageUtils.requireNonNegativeStart(start);
        List<MarketplaceItemEntity> list = marketplaceService.queryItemsByType(type, start);
        return new DataJsonResult<>(true, list.stream().map(this::response).toList());
    }

    @RequestMapping(value = "/api/marketplace/item/id/{id}", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult updateItem(HttpServletRequest request, @Validated MarketplacePublishDTO dto,
            @PathVariable("id") int id) throws Exception {
        if (isInvalidPrice(dto.getPrice())) {
            return failure(request, "商品价格不合法");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        marketplaceService.updateItem(sessionId, dto, id);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/marketplace/item/state/id/{id}", method = RequestMethod.POST)
    public JsonResult updateItemState(HttpServletRequest request, @PathVariable("id") int id,
            @Validated @Range(min = 0, max = 2) int state) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        marketplaceService.updateItemState(sessionId, id, state);
        return new JsonResult(true);
    }
}
