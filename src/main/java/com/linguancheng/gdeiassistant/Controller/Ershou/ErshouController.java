package com.gdeiassistant.gdeiassistant.Controller.Ershou;

import com.gdeiassistant.gdeiassistant.Enum.Base.BoolResultEnum;
import com.gdeiassistant.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.ErshouInfo;
import com.gdeiassistant.gdeiassistant.Pojo.Entity.ErshouItem;
import com.gdeiassistant.gdeiassistant.Pojo.Result.DataJsonResult;
import com.gdeiassistant.gdeiassistant.Pojo.Result.JsonResult;
import com.gdeiassistant.gdeiassistant.Pojo.Result.BaseResult;
import com.gdeiassistant.gdeiassistant.Service.Ershou.ErshouService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class ErshouController {

    @Autowired
    private ErshouService ershouService;

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    private final String[] ERSHOU_ITEM_TYPE = {"校园代步", "手机", "电脑"
            , "数码配件", "数码", "电器"
            , "运动健身", "衣物伞帽", "图书教材"
            , "租赁", "生活娱乐", "其他"};

    /**
     * 进入二手交易首页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/ershou"}, method = RequestMethod.GET)
    public ModelAndView ResolveErshouIndexPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Ershou/ershouIndex");
        return modelAndView;
    }

    /**
     * 进入二手交易信息发布界面
     *
     * @return
     */
    @RequestMapping(value = {"/ershou/publish", "/yiban/lightapp/ershou/publish"}, method = RequestMethod.GET)
    public ModelAndView ResolveErshouPublishPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Ershou/ershouPublish");
        return modelAndView;
    }

    /**
     * 进入二手交易信息编辑界面
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = {"/ershou/edit/id/{id}"}, method = RequestMethod.GET)
    public ModelAndView ResolveErshouEditPage(HttpServletRequest request, @PathVariable("id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<ErshouItem, DataBaseResultEnum> queryResult = ershouService.QueryErshouItemByID(id);
        switch (queryResult.getResultType()) {
            case SUCCESS:
                ErshouItem ershouItem = queryResult.getResultData();
                if (username != null && !username.trim().isEmpty() && username.equals(ershouItem.getUsername())) {
                    modelAndView.addObject("ErshouItemID", id);
                    modelAndView.addObject("ErshouItemName", ershouItem.getName());
                    modelAndView.addObject("ErshouItemDescription", ershouItem.getDescription());
                    modelAndView.addObject("ErshouItemPrice", ershouItem.getPrice());
                    modelAndView.addObject("ErshouItemLocation", ershouItem.getLocation());
                    modelAndView.addObject("ErshouItemType", ERSHOU_ITEM_TYPE[ershouItem.getType()]);
                    modelAndView.addObject("ErshouItemTypeValue", ershouItem.getType());
                    modelAndView.addObject("ErshouItemQQ", ershouItem.getQq());
                    modelAndView.addObject("ErshouItemPhone", ershouItem.getPhone());
                    modelAndView.setViewName("Ershou/ershouEdit");
                } else {
                    modelAndView.setViewName("Error/commonError");
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                    modelAndView.addObject("ErrorMessage", "你没有权限编辑该二手交易信息");
                }
                break;

            case ERROR:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再�?");
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                modelAndView.addObject("ErrorMessage", "二手交易信息不存�?");
                break;
        }
        return modelAndView;
    }

    /**
     * 进入二手交易个人界面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = {"/ershou/personal"}, method = RequestMethod.GET)
    public ModelAndView ResolveErshouPersonalPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("Ershou/ershouPersonal");
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<List<ErshouItem>, DataBaseResultEnum> queryResult = ershouService.QueryPersonalErShouItem(username);
        switch (queryResult.getResultType()) {
            case SUCCESS:
                List<ErshouItem> availableErshouItemList = new ArrayList<>();
                List<ErshouItem> soldedErshouItemList = new ArrayList<>();
                List<ErshouItem> notAvailableErshouItemList = new ArrayList<>();
                for (ErshouItem ershouItem : queryResult.getResultData()) {
                    switch (ershouItem.getState()) {
                        case 0:
                            //下架的二手交易商�?
                            notAvailableErshouItemList.add(ershouItem);
                            break;

                        case 1:
                            //待出售的二手交易商品
                            availableErshouItemList.add(ershouItem);
                            break;

                        case 2:
                            //已出售的的二手交易商�?
                            soldedErshouItemList.add(ershouItem);
                            break;
                    }
                }
                modelAndView.addObject("NotAvailableErshouItemList", notAvailableErshouItemList);
                modelAndView.addObject("AvailableErshouItemList", availableErshouItemList);
                modelAndView.addObject("SoldedErshouItemList", soldedErshouItemList);
                break;

            case EMPTY_RESULT:
                break;

            case ERROR:
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再�?");
                break;
        }
        return modelAndView;
    }

    /**
     * 查询指定关键字二手交易信�?
     *
     * @param request
     * @param keyword
     * @return
     */
    @RequestMapping(value = {"/ershou/search/keyword/{keyword}"}, method = RequestMethod.GET)
    public ModelAndView SearchErshouInfoWithKeyword(HttpServletRequest request, @PathVariable("keyword") String keyword) {
        ModelAndView modelAndView = new ModelAndView();
        if (keyword.length() > 25) {
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
            modelAndView.addObject("ErrorMessage", "请求参数不合�?");
        } else {
            modelAndView.addObject("KeyWord", keyword);
            BaseResult<List<ErshouItem>, DataBaseResultEnum> result = ershouService.QueryErshouItemWithKeyword(keyword, 0);
            switch (result.getResultType()) {
                case SUCCESS:
                    modelAndView.addObject("ErshouItemList", result.getResultData());
                    modelAndView.setViewName("Ershou/ershouSearch");
                    break;

                case EMPTY_RESULT:
                    modelAndView.setViewName("Ershou/ershouSearch");
                    break;

                case ERROR:
                    modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再�?");
                    modelAndView.setViewName("Ershou/ershouSearch");
                    break;
            }
        }
        return modelAndView;
    }

    /**
     * 查询特殊类型的二手交易信�?
     *
     * @param type
     * @return
     */
    @RequestMapping(value = {"/ershou/type/{type}"}, method = RequestMethod.GET)
    public ModelAndView SearchErshouInfoByType(HttpServletRequest request, @PathVariable("type") int type) {
        ModelAndView modelAndView = new ModelAndView();
        if (type < 0 || type > 11) {
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
            modelAndView.addObject("ErrorMessage", "不存在的交易类型");
        } else {
            modelAndView.addObject("TypeNumber", type);
            modelAndView.addObject("TypeName", ERSHOU_ITEM_TYPE[type]);
            BaseResult<List<ErshouItem>, DataBaseResultEnum> result = ershouService.QueryErshouItemByType(type, 0);
            switch (result.getResultType()) {
                case SUCCESS:
                    modelAndView.addObject("ErshouItemList", result.getResultData());
                    modelAndView.setViewName("Ershou/ershouType");
                    break;

                case EMPTY_RESULT:
                    modelAndView.setViewName("Ershou/ershouType");
                    break;

                case ERROR:
                    modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再�?");
                    modelAndView.setViewName("Ershou/ershouType");
                    break;
            }
        }
        return modelAndView;
    }

    /**
     * 查询指定ID的二手交易商品的详细信息
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = {"ershou/detail/id/{id}"})
    public ModelAndView GetErshouInfoDetail(HttpServletRequest request, @PathVariable("id") int id) {
        ModelAndView modelAndView = new ModelAndView();
        BaseResult<ErshouInfo, DataBaseResultEnum> result = ershouService.QueryErshouItemDetailInfo(id);
        switch (result.getResultType()) {
            case SUCCESS:
                ErshouInfo ershouInfo = result.getResultData();
                if (ershouInfo.getErshouItem().getState() == 0) {
                    //商品已经下架，不能查�?
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                    modelAndView.addObject("ErrorMessage", "该商品已经下架，无法查看");
                    modelAndView.setViewName("Error/commonError");
                } else if (ershouInfo.getErshouItem().getState() == 2) {
                    //商品已经出售，不能查�?
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                    modelAndView.addObject("ErrorMessage", "该商品已经出售，无法查看");
                    modelAndView.setViewName("Error/commonError");
                } else {
                    modelAndView.addObject("ErshouInfo", ershouInfo);
                    modelAndView.setViewName("Ershou/ershouDetail");
                }
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再�?");
                modelAndView.setViewName("Error/commonError");
                break;

            case EMPTY_RESULT:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                modelAndView.addObject("ErrorMessage", "二手交易信息不存�?");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    /**
     * 分页查询二手交易信息
     *
     * @return
     */
    @RequestMapping(value = "/ershou/info/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<ErshouItem>> GetErshouInfo(@PathVariable("start") int start) {
        DataJsonResult<List<ErshouItem>> result = new DataJsonResult<>();
        BaseResult<List<ErshouItem>, DataBaseResultEnum> queryResult = ershouService.QueryErshouItem(start);
        switch (queryResult.getResultType()) {
            case SUCCESS:
                result.setData(queryResult.getResultData());
                result.setSuccess(true);
                break;

            case ERROR:
                result.setSuccess(false);
                result.setMessage("服务器异常，请稍候再�?");
                break;

            case EMPTY_RESULT:
                result.setSuccess(true);
                break;
        }
        return result;
    }

    /**
     * 添加新的二手交易信息
     *
     * @param ershouItem
     * @param image1
     * @param image2
     * @param image3
     * @param image4
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/ershou/info", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult AddErshouInfo(HttpServletRequest request
            , @Validated ErshouItem ershouItem, MultipartFile image1
            , MultipartFile image2, MultipartFile image3, MultipartFile image4
            , BindingResult bindingResult) throws IOException {
        JsonResult jsonResult = new JsonResult();
        if (bindingResult.hasErrors() || ershouItem.getPrice() <= 0 || ershouItem.getPrice() > 9999.99) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合�?");
        } else if (image1 == null || image1.getSize() <= 0 || image1.getSize() >= MAX_PICTURE_SIZE) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("不合法的图片文件");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            BaseResult<ErshouItem, BoolResultEnum> result = ershouService.AddErshouInfo(ershouItem, username);
            switch (result.getResultType()) {
                case SUCCESS:
                    //添加二手交易数据成功，进行图片上�?
                    ershouService.UploadErshouItemPicture(result.getResultData().getId(), 1, image1.getInputStream());
                    if (image2 != null && image2.getSize() > 0 && image2.getSize() < MAX_PICTURE_SIZE) {
                        ershouService.UploadErshouItemPicture(result.getResultData().getId(), 2, image2.getInputStream());
                        if (image3 != null && image3.getSize() > 0 && image3.getSize() < MAX_PICTURE_SIZE) {
                            ershouService.UploadErshouItemPicture(result.getResultData().getId(), 3, image3.getInputStream());
                            if (image4 != null && image4.getSize() > 0 && image4.getSize() < MAX_PICTURE_SIZE) {
                                ershouService.UploadErshouItemPicture(result.getResultData().getId(), 4, image4.getInputStream());
                            }
                        }
                    }
                    jsonResult.setSuccess(true);
                    break;

                case ERROR:
                    //服务器内部异�?
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("服务器异常，请稍候再�?");
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 查询指定关键词的二手交易信息
     *
     * @param keyword
     * @param start
     * @return
     */
    @RequestMapping(value = "/ershou/keyword/{keyword}/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<ErshouItem>> GetErshouInfoWithKeyword(@PathVariable("keyword") String keyword
            , @PathVariable("start") int start) {
        DataJsonResult<List<ErshouItem>> result = new DataJsonResult<>();
        BaseResult<List<ErshouItem>, DataBaseResultEnum> queryResult = ershouService.QueryErshouItemWithKeyword(keyword, start);
        switch (queryResult.getResultType()) {
            case SUCCESS:
                result.setSuccess(true);
                result.setData(queryResult.getResultData());
                break;

            case EMPTY_RESULT:
                result.setSuccess(true);
                break;

            case ERROR:
                result.setSuccess(false);
                result.setMessage("服务器异常，请稍候再�?");
                break;
        }
        return result;
    }

    /**
     * 获取二手交易商品预览�?
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/ershou/info/id/{id}/preview", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetErshouItemPreviewImage(HttpServletRequest request, @PathVariable("id") int id) {
        DataJsonResult<String> result = new DataJsonResult<>();
        List<String> list = ershouService.GetErshouItemPictureURL(id);
        if (list != null && !list.isEmpty()) {
            String previewImageURl = list.get(0);
            result.setData(previewImageURl);
            result.setSuccess(true);
        } else {
            result.setSuccess(false);
        }
        return result;
    }

    /**
     * 查询特殊类型的二手交易信�?
     *
     * @param type
     * @param start
     * @return
     */
    @RequestMapping(value = "/ershou/info/type/{type}/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<ErshouItem>> GetErshouInfoByType(@PathVariable("type") int type
            , @PathVariable("start") int start) {
        DataJsonResult<List<ErshouItem>> result = new DataJsonResult<>();
        if (type < 0 || type > 11) {
            result.setSuccess(false);
            result.setMessage("请求参数不合�?");
        } else {
            BaseResult<List<ErshouItem>, DataBaseResultEnum> queryResult = ershouService.QueryErshouItemByType(type, start);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    result.setData(queryResult.getResultData());
                    result.setSuccess(true);
                    break;

                case ERROR:
                    result.setSuccess(false);
                    result.setMessage("服务器异常，请稍候再�?");
                    break;

                case EMPTY_RESULT:
                    result.setSuccess(true);
                    break;
            }
        }
        return result;
    }

    /**
     * 更新二手交易信息
     *
     * @param request
     * @param ershouItem
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/ershou/info/id/{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult UpdateErshouItem(HttpServletRequest request, @Validated ErshouItem ershouItem
            , @PathVariable("id") int id, BindingResult bindingResult) {
        JsonResult result = new JsonResult();
        if (bindingResult.hasErrors() || ershouItem.getPrice() <= 0 || ershouItem.getPrice() > 9999.99) {
            result.setSuccess(false);
            result.setMessage("请求参数不合�?");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            if (username == null || username.trim().isEmpty()) {
                result.setSuccess(false);
                result.setMessage("用户身份凭证过期，请稍�?�再�?");
            } else {
                BaseResult<ErshouItem, DataBaseResultEnum> queryResult = ershouService.QueryErshouItemByID(id);
                switch (queryResult.getResultType()) {
                    case SUCCESS:
                        if (queryResult.getResultData().getUsername().equals(username)) {
                            BoolResultEnum updateResult = ershouService.UpdateErshouInfo(ershouItem, id);
                            switch (updateResult) {
                                case SUCCESS:
                                    result.setSuccess(true);
                                    break;

                                case ERROR:
                                    result.setSuccess(false);
                                    result.setMessage("服务器异常，请稍候再�?");
                                    break;
                            }
                        } else {
                            result.setSuccess(false);
                            result.setMessage("用户身份凭证过期，请稍�?�再�?");
                        }
                        break;

                    case EMPTY_RESULT:
                        result.setSuccess(false);
                        result.setMessage("该二手交易信息不存在");
                        break;

                    case ERROR:
                        result.setSuccess(false);
                        result.setMessage("服务器异常，请稍候再�?");
                        break;
                }
            }
        }
        return result;
    }

    /**
     * 修改指定ID的二手交易商品状�?
     *
     * @param request
     * @param id
     * @param state
     * @return
     */
    @RequestMapping(value = "/ershou/info/state/id/{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult UpdateErshouItemState(HttpServletRequest request, @PathVariable("id") int id, int state) {
        JsonResult result = new JsonResult();
        if (state < 0 || state > 2) {
            result.setSuccess(false);
            result.setMessage("请求参数不合�?");
        } else {
            BaseResult<ErshouItem, DataBaseResultEnum> queryResult = ershouService.QueryErshouItemByID(id);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    ErshouItem ershouItem = queryResult.getResultData();
                    String username = (String) request.getSession().getAttribute("username");
                    if (username != null && !username.trim().isEmpty() && username.equals(ershouItem.getUsername())) {
                        if (ershouItem.getState() == 2) {
                            //已出售状态的商品不能修改状�??
                            result.setSuccess(false);
                            result.setMessage("商品已出售，不能修改");
                        } else {
                            BoolResultEnum resultEnum = ershouService.UpdateErshouItemState(id, state);
                            switch (resultEnum) {
                                case SUCCESS:
                                    result.setSuccess(true);
                                    break;

                                case ERROR:
                                    result.setSuccess(false);
                                    result.setMessage("服务器异常，请稍候再�?");
                                    break;
                            }
                        }
                    } else {
                        result.setSuccess(false);
                        result.setMessage("你没有权限修改该二手交易信息");
                    }
                    break;

                case EMPTY_RESULT:
                    result.setSuccess(false);
                    result.setMessage("二手交易信息不存�?");
                    break;

                case ERROR:
                    result.setSuccess(false);
                    result.setMessage("服务器异常，请稍候再�?");
                    break;
            }
        }
        return result;
    }
}
