package cn.gdeiassistant.core.secondhand.controller;

import cn.gdeiassistant.common.annotation.RecordIPAddress;
import cn.gdeiassistant.common.constant.ValueConstantUtils;
import cn.gdeiassistant.common.enums.IPAddress.IPAddressEnum;
import cn.gdeiassistant.common.exception.DatabaseException.ConfirmedStateException;
import cn.gdeiassistant.common.exception.DatabaseException.NotAvailableStateException;
import cn.gdeiassistant.core.secondhand.pojo.dto.SecondhandPublishDTO;
import cn.gdeiassistant.core.secondhand.pojo.entity.SecondhandItemEntity;
import cn.gdeiassistant.core.secondhand.pojo.vo.SecondhandItemVO;
import cn.gdeiassistant.core.secondhand.service.SecondhandService;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class SecondhandController {

    @Autowired
    private SecondhandService secondhandService;

    @RequestMapping(value = "/api/ershou/item/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<SecondhandItemEntity>> getItemList(HttpServletRequest request, @PathVariable("start") int start) throws Exception {
        List<SecondhandItemEntity> list = secondhandService.queryItems(start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/ershou/item", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult addItem(HttpServletRequest request,
            @Validated SecondhandPublishDTO dto, MultipartFile image1,
            MultipartFile image2, MultipartFile image3, MultipartFile image4) throws Exception {
        if (image1 == null || image1.getSize() <= 0 || image1.getSize() >= ValueConstantUtils.MAX_IMAGE_SIZE) {
            return new JsonResult(false, "不合法的图片文件");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        SecondhandItemEntity entity = secondhandService.publishItem(dto, sessionId);
        secondhandService.uploadItemPicture(entity.getId(), 1, image1.getInputStream());
        if (image2 != null && image2.getSize() > 0 && image2.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
            secondhandService.uploadItemPicture(entity.getId(), 2, image2.getInputStream());
            if (image3 != null && image3.getSize() > 0 && image3.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                secondhandService.uploadItemPicture(entity.getId(), 3, image3.getInputStream());
                if (image4 != null && image4.getSize() > 0 && image4.getSize() < ValueConstantUtils.MAX_IMAGE_SIZE) {
                    secondhandService.uploadItemPicture(entity.getId(), 4, image4.getInputStream());
                }
            }
        }
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/ershou/keyword/{keyword}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<SecondhandItemEntity>> getItemWithKeyword(HttpServletRequest request, @PathVariable("keyword") String keyword,
            @PathVariable("start") int start) throws Exception {
        List<SecondhandItemEntity> list = secondhandService.queryItemsWithKeyword(keyword, start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/ershou/item/id/{id}/preview", method = RequestMethod.GET)
    public DataJsonResult<String> getItemPreviewImage(HttpServletRequest request, @PathVariable("id") int id) {
        List<String> list = secondhandService.getItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            return new DataJsonResult<>(true, list.get(0));
        }
        return new DataJsonResult<>(false, "获取二手交易商品预览图失败");
    }

    @RequestMapping(value = "/api/ershou/item/id/{id}", method = RequestMethod.GET)
    public DataJsonResult<SecondhandItemVO> getItemDetail(@PathVariable("id") int id) throws Exception {
        SecondhandItemVO vo = secondhandService.queryDetailById(id);
        if (vo.getSecondhandItem().getState().equals(0)) {
            throw new NotAvailableStateException("已下架的二手交易信息不能查看");
        }
        if (vo.getSecondhandItem().getState().equals(2)) {
            throw new ConfirmedStateException("已出售的二手交易信息不能查看");
        }
        return new DataJsonResult<>(true, vo);
    }

    @RequestMapping(value = "/api/ershou/item/type/{type}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<SecondhandItemEntity>> getItemByType(HttpServletRequest request, @Validated @Range(min = 0, max = 11) @PathVariable("type") int type,
            @PathVariable("start") int start) throws Exception {
        List<SecondhandItemEntity> list = secondhandService.queryItemsByType(type, start);
        return new DataJsonResult<>(true, list);
    }

    @RequestMapping(value = "/api/ershou/item/id/{id}", method = RequestMethod.POST)
    @RecordIPAddress(type = IPAddressEnum.POST)
    public JsonResult updateItem(HttpServletRequest request, @Validated SecondhandPublishDTO dto,
            @PathVariable("id") int id) throws Exception {
        if (dto.getPrice() <= 0 || dto.getPrice() > 9999.99) {
            return new JsonResult(false, "商品价格不合法");
        }
        String sessionId = (String) request.getAttribute("sessionId");
        secondhandService.updateItem(sessionId, dto, id);
        return new JsonResult(true);
    }

    @RequestMapping(value = "/api/ershou/item/state/id/{id}", method = RequestMethod.POST)
    public JsonResult updateItemState(HttpServletRequest request, @PathVariable("id") int id,
            @Validated @Range(min = 0, max = 2) int state) throws Exception {
        String sessionId = (String) request.getAttribute("sessionId");
        secondhandService.updateItemState(sessionId, id, state);
        return new JsonResult(true);
    }
}
