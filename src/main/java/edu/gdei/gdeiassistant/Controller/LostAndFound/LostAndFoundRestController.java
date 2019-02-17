package edu.gdei.gdeiassistant.Controller.LostAndFound;

import edu.gdei.gdeiassistant.Exception.DatabaseException.NoAccessException;
import edu.gdei.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import edu.gdei.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.LostAndFound.LostAndFoundService;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

@RestController
public class LostAndFoundRestController {

    @Autowired
    private LostAndFoundService lostAndFoundService;

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    /**
     * 分页查询失物信息
     *
     * @param start
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/lostitem/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItem>> GetLostItem(@PathVariable("start") int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundService
                .QueryLostItems(start);
        return new DataJsonResult<>(true, lostAndFoundItemList);
    }

    /**
     * 分页查询招领信息
     *
     * @param start
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/founditem/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItem>> GetFoundInfo(@PathVariable("start") int start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundService.QueryFoundItems(start);
        return new DataJsonResult<>(true, lostAndFoundItemList);
    }

    /**
     * 关键字查询更多失物招领信息
     *
     * @param lostType
     * @param start
     * @param keywords
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/lostitem/type/{type}/start/{start}", method = RequestMethod.POST)
    public DataJsonResult<List<LostAndFoundItem>> SearchLostAndFoundInfo(@PathVariable("type") Integer lostType
            , @Validated @Range(min = 0, max = 1) @PathVariable("start") Integer start, @Validated @NotBlank @Length(min = 1, max = 50) @RequestParam("keyword") String keywords) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = null;
        if (lostType.equals(0)) {
            lostAndFoundItemList = lostAndFoundService.QueryLostItemsWithKeyword(keywords, start);
        } else {
            lostAndFoundItemList = lostAndFoundService.QueryFoundItemsWithKeyword(keywords, start);
        }
        return new DataJsonResult<>(true, lostAndFoundItemList);
    }

    /**
     * 获取失物招领物品预览图
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/item/id/{id}/preview", method = RequestMethod.GET)
    public DataJsonResult<String> GetLostAndFoundItemPreviewImage(@PathVariable("id") int id) {
        List<String> list = lostAndFoundService.GetLostAndFoundItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            String previewImageURl = list.get(0);
            return new DataJsonResult<>(true, previewImageURl);
        } else {
            return new DataJsonResult<>(false, "获取失物招领物品预览图失败");
        }
    }

    /**
     * 根据分类查找更多招领信息
     *
     * @param type
     * @param start
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/founditem/type/{type}/start/{start}", method = RequestMethod.GET)
    public DataJsonResult<List<LostAndFoundItem>> SearchFoundInfoByType(@PathVariable("type") Integer type
            , @PathVariable("start") Integer start) throws Exception {
        List<LostAndFoundItem> lostAndFoundItemList = lostAndFoundService
                .QueryFoundItemsByType(type, start);
        return new DataJsonResult<>(true, lostAndFoundItemList);
    }

    /**
     * 确认寻回物品
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/item/id/{id}/didfound", method = RequestMethod.POST)
    public JsonResult DidFoundItem(HttpServletRequest request, @PathVariable("id") Integer id) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        LostAndFoundInfo lostAndFoundInfo = lostAndFoundService.QueryLostAndFoundInfoByID(id);
        if (lostAndFoundInfo.getLostAndFoundItem().getUsername().equals(username)) {
            lostAndFoundService.UpdateLostAndFoundItemState(id, 1);
            return new JsonResult(true);
        }
        throw new NoAccessException("没有权限修改该失物招领信息状态");
    }

    /**
     * 更新失物招领信息
     *
     * @param id
     * @param lostAndFoundItem
     * @return
     */
    @RequestMapping(value = "/api/lostandfound/item/id/{id}", method = RequestMethod.POST)
    public JsonResult UpdateLostAndFoundInfo(@PathVariable("id") Integer id
            , @Validated LostAndFoundItem lostAndFoundItem) throws Exception {
        lostAndFoundService.UpdateLostAndFoundItem(lostAndFoundItem, id);
        return new JsonResult(true);
    }

    /**
     * 添加失物招领信息
     *
     * @param request
     * @param lostAndFoundItem
     * @param image1
     * @param image2
     * @param image3
     * @param image4
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/api/lostandfound/item", method = RequestMethod.POST)
    public JsonResult AddLostAndFoundInfo(HttpServletRequest request
            , @Validated LostAndFoundItem lostAndFoundItem, MultipartFile image1
            , MultipartFile image2, MultipartFile image3, MultipartFile image4) throws Exception {
        if (image1 == null || image1.getSize() <= 0 || image1.getSize() >= MAX_PICTURE_SIZE) {
            return new JsonResult(false, "不合法的图片文件");
        }
        String username = (String) request.getSession().getAttribute("username");
        lostAndFoundItem = lostAndFoundService.AddLostAndFoundItem(lostAndFoundItem, username);
        //添加二手交易数据成功，进行图片上传
        lostAndFoundService.UploadLostAndFoundItemPicture(lostAndFoundItem.getId(), 1, image1.getInputStream());
        if (image2 != null && image2.getSize() > 0 && image2.getSize() < MAX_PICTURE_SIZE) {
            lostAndFoundService.UploadLostAndFoundItemPicture(lostAndFoundItem.getId(), 2, image2.getInputStream());
            if (image3 != null && image3.getSize() > 0 && image3.getSize() < MAX_PICTURE_SIZE) {
                lostAndFoundService.UploadLostAndFoundItemPicture(lostAndFoundItem.getId(), 3, image3.getInputStream());
                if (image4 != null && image4.getSize() > 0 && image4.getSize() < MAX_PICTURE_SIZE) {
                    lostAndFoundService.UploadLostAndFoundItemPicture(lostAndFoundItem.getId(), 4, image4.getInputStream());
                }
            }
        }
        return new JsonResult(true);
    }
}
