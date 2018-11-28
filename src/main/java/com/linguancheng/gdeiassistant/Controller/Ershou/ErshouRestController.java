package com.linguancheng.gdeiassistant.Controller.Ershou;

import com.linguancheng.gdeiassistant.Exception.DatabaseException.NoAccessException;
import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.ErshouItem;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Service.Ershou.ErshouService;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
public class ErshouRestController {

    @Autowired
    private ErshouService ershouService;

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    /**
     * 分页查询二手交易信息
     *
     * @return
     */
    @RequestMapping(value = "/ershou/item/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<ErshouItem>> GetErshouItem(@PathVariable("start") int start) throws Exception {
        DataJsonResult<List<ErshouItem>> result = new DataJsonResult<>();
        List<ErshouItem> ershouItemList = ershouService.QueryErshouItems(start);
        return new DataJsonResult<>(true, ershouItemList);
    }

    /**
     * 添加新的二手交易信息
     *
     * @param ershouItem
     * @param image1
     * @param image2
     * @param image3
     * @param image4
     * @return
     */
    @RequestMapping(value = "/ershou/item", method = RequestMethod.POST)
    public JsonResult AddErshouInfo(HttpServletRequest request
            , @Validated ErshouItem ershouItem, MultipartFile image1
            , MultipartFile image2, MultipartFile image3, MultipartFile image4) throws Exception {
        if (image1 == null || image1.getSize() <= 0 || image1.getSize() >= MAX_PICTURE_SIZE) {
            return new JsonResult(false, "不合法的图片文件");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            ershouItem = ershouService.AddErshouItem(ershouItem, username);
            //添加二手交易数据成功，进行图片上传
            ershouService.UploadErshouItemPicture(ershouItem.getId(), 1, image1.getInputStream());
            if (image2 != null && image2.getSize() > 0 && image2.getSize() < MAX_PICTURE_SIZE) {
                ershouService.UploadErshouItemPicture(ershouItem.getId(), 2, image2.getInputStream());
                if (image3 != null && image3.getSize() > 0 && image3.getSize() < MAX_PICTURE_SIZE) {
                    ershouService.UploadErshouItemPicture(ershouItem.getId(), 3, image3.getInputStream());
                    if (image4 != null && image4.getSize() > 0 && image4.getSize() < MAX_PICTURE_SIZE) {
                        ershouService.UploadErshouItemPicture(ershouItem.getId(), 4, image4.getInputStream());
                    }
                }
            }
            return new JsonResult(true);
        }
    }

    /**
     * 查询指定关键词的二手交易信息
     *
     * @param keyword
     * @param start
     * @return
     */
    @RequestMapping(value = "/ershou/keyword/{keyword}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<ErshouItem>> GetErshouItemWithKeyword(@PathVariable("keyword") String keyword
            , @PathVariable("start") int start) throws Exception {
        List<ErshouItem> ershouItemList = ershouService.QueryErshouItemsWithKeyword(keyword, start);
        return new DataJsonResult<>(true, ershouItemList);
    }

    /**
     * 获取二手交易商品预览图
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/ershou/item/id/{id}/preview", method = RequestMethod.GET)
    public DataJsonResult<String> GetErshouItemPreviewImage(@PathVariable("id") int id) {
        DataJsonResult<String> result = new DataJsonResult<>();
        List<String> list = ershouService.GetErshouItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            String previewImageURl = list.get(0);
            return new DataJsonResult<>(true, previewImageURl);
        }
        return new DataJsonResult<>(false, "获取二手交易商品预览图失败");
    }

    /**
     * 查询特殊类型的二手交易信息
     *
     * @param type
     * @param start
     * @return
     */
    @RequestMapping(value = "/ershou/item/type/{type}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<ErshouItem>> GetErshouItemByType(@Validated @Range(min = 0, max = 11) @PathVariable("type") int type
            , @PathVariable("start") int start) throws Exception {
        List<ErshouItem> ershouItemList = ershouService.QueryErshouItemByType(type, start);
        return new DataJsonResult<>(true, ershouItemList);
    }

    /**
     * 更新二手交易信息
     *
     * @param request
     * @param ershouItem
     * @return
     */
    @RequestMapping(value = "/ershou/item/id/{id}", method = RequestMethod.POST)
    public JsonResult UpdateErshouItem(HttpServletRequest request, @Validated ErshouItem ershouItem
            , @PathVariable("id") int id) throws Exception {
        if (ershouItem.getPrice() <= 0 || ershouItem.getPrice() > 9999.99) {
            return new JsonResult(false, "商品价格不合法");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            ErshouInfo ershouInfo = ershouService.QueryErshouInfoByID(id);
            if (ershouInfo.getErshouItem().getUsername().equals(username)) {
                ershouService.UpdateErshouItem(ershouItem, ershouInfo.getErshouItem().getId());
                return new JsonResult(true);
            }
            throw new NoAccessException("没有权限修改该商品信息");
        }
    }

    /**
     * 修改指定ID的二手交易商品状态
     *
     * @param request
     * @param id
     * @param state
     * @return
     */
    @RequestMapping(value = "/ershou/item/state/id/{id}", method = RequestMethod.POST)
    public JsonResult UpdateErshouItemState(HttpServletRequest request, @PathVariable("id") int id
            , @Validated @Range(min = 0, max = 2) int state) throws Exception {
        ErshouInfo ershouInfo = ershouService.QueryErshouInfoByID(id);
        String username = (String) request.getSession().getAttribute("username");
        if (ershouInfo.getErshouItem().getUsername().equals(username)) {
            if (ershouInfo.getErshouItem().getState() == 2) {
                //已出售状态的商品不能修改状态
                return new JsonResult(false, "商品已出售，不能修改");
            }
            ershouService.UpdateErshouItemState(id, state);
            return new JsonResult(true);
        }
        throw new NoAccessException("没有权限修改该商品信息");
    }
}
