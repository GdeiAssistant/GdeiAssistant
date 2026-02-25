package cn.gdeiassistant.core.lostandfound.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.lostandfound.pojo.dto.LostAndFoundPublishDTO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundDetailVO;
import cn.gdeiassistant.core.lostandfound.pojo.vo.LostAndFoundItemVO;
import cn.gdeiassistant.core.lostandfound.service.LostAndFoundService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class LostAndFoundController {

    @Autowired
    private LostAndFoundService lostAndFoundService;

    @RequestMapping(value = "/api/lostandfound/lostitem/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItemVO>> getLostItem(HttpServletRequest request, @PathVariable("start") int start) throws Exception {
        List<LostAndFoundItemVO> list = lostAndFoundService.queryLostItems(start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/lostandfound/founditem/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItemVO>> getFoundInfo(HttpServletRequest request, @PathVariable("start") int start) throws Exception {
        List<LostAndFoundItemVO> list = lostAndFoundService.queryFoundItems(start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/lostandfound/lostitem/type/{type}/start/{start}", method = RequestMethod.POST)
    public DataJsonResult<List<LostAndFoundItemVO>> searchLostAndFoundInfo(HttpServletRequest request, @PathVariable("type") Integer lostType,
            @Validated @Range(min = 0, max = 1) @PathVariable("start") Integer start,
            @Validated @NotBlank @Length(min = 1, max = 50) @RequestParam("keyword") String keywords) throws Exception {
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
        lostAndFoundService.updateLostAndFoundItem(dto, id);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/lostandfound/item", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addLostAndFoundInfo(HttpServletRequest request,
            @Validated LostAndFoundPublishDTO dto, MultipartFile image1,
            MultipartFile image2, MultipartFile image3, MultipartFile image4) throws Exception {
        if (image1 == null || image1.getSize() <= 0 || image1.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
            return new JsonResult(false, "不合法的图片文件");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        LostAndFoundItemVO vo = lostAndFoundService.addLostAndFoundItem(dto, sessionId);
        lostAndFoundService.uploadLostAndFoundItemPicture(vo.getId(), 1, image1.getInputStream());
        if (image2 != null && image2.getSize() > 0 && image2.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
            lostAndFoundService.uploadLostAndFoundItemPicture(vo.getId(), 2, image2.getInputStream());
            if (image3 != null && image3.getSize() > 0 && image3.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                lostAndFoundService.uploadLostAndFoundItemPicture(vo.getId(), 3, image3.getInputStream());
                if (image4 != null && image4.getSize() > 0 && image4.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                    lostAndFoundService.uploadLostAndFoundItemPicture(vo.getId(), 4, image4.getInputStream());
                }
            }
        }
        return new JsonResult(true);
    }
}
