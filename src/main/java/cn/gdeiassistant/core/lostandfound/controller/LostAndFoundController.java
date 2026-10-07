package cn.gdeiassistant.core.lostandfound.controller;

import cn.gdeiassistant.core.i18n.BackendTextLocalizer;

import cn.gdeiassistant.common.annotation.RateLimit;
import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.ipaddress.IPAddressEnum;
import cn.gdeiassistant.common.pojo.result.DataJsonResult;
import cn.gdeiassistant.common.pojo.result.JsonResult;
import cn.gdeiassistant.common.tools.utils.PageUtils;
import cn.gdeiassistant.common.tools.utils.StringUtils;
import cn.gdeiassistant.core.lostandfound.pojo.dto.LostAndFoundPublishDTO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundDetailVO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundService;
import org.hibernate.validator.constraints.Length;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Validated
public class LostAndFoundController {

    @Autowired
    private LostAndFoundService lostAndFoundService;

    @RequestMapping(value = "/api/lostandfound/lostitem/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItemVO>> getLostItem(HttpServletRequest request, @PathVariable("start") @Min(0) int start) throws Exception {
        start = PageUtils.requireNonNegativeStart(start);
        List<LostAndFoundItemVO> list = lostAndFoundService.queryLostItems(start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/lostandfound/profile", method = RequestMethod.GET)
    public DataJsonResult<Map<String, Object>> getMyLostAndFoundItems(HttpServletRequest request, @RequestParam(value="start", defaultValue="0") int start) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        start = PageUtils.requireNonNegativeStart(start);
        var rows = lostAndFoundService.queryPersonalLostAndFoundItems(sessionId, start, 26);
        boolean hasMore = rows.size() > 25;
        var list = rows.subList(0, Math.min(25, rows.size()));
        List<LostAndFoundItemVO> lost = new ArrayList<>();
        List<LostAndFoundItemVO> found = new ArrayList<>();
        List<LostAndFoundItemVO> didfound = new ArrayList<>();
        for (LostAndFoundItemVO item : list) {
            if (Integer.valueOf(1).equals(item.getState())) {
                didfound.add(item);
            } else if (Integer.valueOf(0).equals(item.getState()) && Integer.valueOf(0).equals(item.getLostType())) {
                lost.add(item);
            } else if (Integer.valueOf(0).equals(item.getState()) && Integer.valueOf(1).equals(item.getLostType())) {
                found.add(item);
            }
        }
        Map<String, Object> data = new HashMap<>();
        data.put("lost", lost);
        data.put("found", found);
        data.put("didfound", didfound);
        data.put("hasMore", hasMore);
        data.put("nextStart", hasMore ? start + 25 : null);
        return new DataJsonResult<>(true, data);
    }

    @RequestMapping(value = "/api/lostandfound/founditem/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItemVO>> getFoundInfo(HttpServletRequest request, @PathVariable("start") @Min(0) int start) throws Exception {
        start = PageUtils.requireNonNegativeStart(start);
        List<LostAndFoundItemVO> list = lostAndFoundService.queryFoundItems(start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/lostandfound/lostitem/type/{type}/start/{start}", method = RequestMethod.POST)
    public DataJsonResult<List<LostAndFoundItemVO>> searchLostAndFoundInfo(HttpServletRequest request, @Validated @Range(min = 0, max = 1) @PathVariable("type") Integer lostType,
            @PathVariable("start") @Min(0) Integer start,
            @Validated @NotBlank @Length(min = 1, max = 50) @RequestParam("keyword") String keywords) throws Exception {
        if (lostType == null || (lostType != 0 && lostType != 1)) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        start = PageUtils.requireNonNegativeStart(start);
        List<LostAndFoundItemVO> list = lostType.equals(0)
                ? lostAndFoundService.queryLostItemsWithKeyword(keywords, start)
                : lostAndFoundService.queryFoundItemsWithKeyword(keywords, start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/lostandfound/item/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<LostAndFoundDetailVO> getLostAndFoundItemDetail(@PathVariable("id") int id) throws Exception {
        LostAndFoundDetailVO info = lostAndFoundService.queryLostAndFoundInfoByID(id);
        return new DataJsonResult<>(true, info);
    }

    @RequestMapping(value = "/api/lostandfound/item/id/{id}/preview", method = RequestMethod.GET)
    public DataJsonResult<String> getLostAndFoundItemPreviewImage(HttpServletRequest request, @PathVariable("id") int id) {
        List<String> list = lostAndFoundService.getLostAndFoundItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            return new DataJsonResult<>(true, list.get(0));
        }
        return new DataJsonResult<>(false, "获取失物招领物品预览图失败");
    }

    @RequestMapping(value = "/api/lostandfound/founditem/type/{type}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItemVO>> searchFoundInfoByType(HttpServletRequest request, @PathVariable("type") Integer type,
            @PathVariable("start") Integer start) throws Exception {
        if (type == null || type < 0 || type > 11) {
            throw new IllegalArgumentException("请求参数不合法");
        }
        start = PageUtils.requireNonNegativeStart(start);
        List<LostAndFoundItemVO> list = lostAndFoundService.queryFoundItemsByType(type, start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/lostandfound/item/id/{id}/didfound", method = RequestMethod.POST)
    public JsonResult didFoundItem(HttpServletRequest request, @PathVariable("id") Integer id) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        lostAndFoundService.verifyLostAndFoundInfoEditAccess(sessionId, id);
        lostAndFoundService.updateLostAndFoundItemState(id, 1);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/lostandfound/item/id/{id}", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult updateLostAndFoundInfo(HttpServletRequest request, @PathVariable("id") Integer id,
            @Validated LostAndFoundPublishDTO dto) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        lostAndFoundService.updateLostAndFoundItem(dto, id, sessionId);
        return new JsonResult(true);
    }

    @RateLimit(maxRequests = 5, windowSeconds = 60)
    @RequestMapping(value = "/api/lostandfound/item", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addLostAndFoundInfo(HttpServletRequest request,
            @Validated LostAndFoundPublishDTO dto, MultipartFile image1,
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
        try {
            lostAndFoundService.publishItem(dto, sessionId, images, imageKeys);
        } catch (Exception failure) {
            return new JsonResult(false, BackendTextLocalizer.localizeMessage("上传失败", request.getHeader("Accept-Language")));
        }
        return new JsonResult(true);
    }
}
