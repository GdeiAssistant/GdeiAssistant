package com.linguancheng.gdeiassistant.Controller.LostAndFound;

import com.linguancheng.gdeiassistant.Enum.Base.BoolResultEnum;
import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundInfo;
import com.linguancheng.gdeiassistant.Pojo.Entity.LostAndFoundItem;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.LostAndFound.LostAndFoundService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class LostAndFoundController {

    @Autowired
    private LostAndFoundService lostAndFoundService;

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    private final String[] LOSTANDFOUND_ITEM_TYPE = {"手机", "校园卡", "身份证"
            , "银行卡", "书", "钥匙"
            , "包包", "衣帽", "校园代步"
            , "运动健身", "数码配件", "其他"};

    /**
     * 进入失物主页
     *
     * @return
     */
    @RequestMapping(value = {"/lostandfound", "/lostandfound/lost"}, method = RequestMethod.GET)
    public ModelAndView ResolveLostIndexPage() {
        return new ModelAndView("LostAndFound/lostIndex");
    }

    /**
     * 进入招领主页
     *
     * @return
     */
    @RequestMapping(value = "/lostandfound/found", method = RequestMethod.GET)
    public ModelAndView ResolveFoundIndexPage() {
        return new ModelAndView("LostAndFound/foundIndex");
    }

    /**
     * 进入发布页面
     *
     * @return
     */
    @RequestMapping(value = "/lostandfound/publish", method = RequestMethod.GET)
    public ModelAndView ResolvePublishPage() {
        return new ModelAndView("LostAndFound/publish");
    }

    /**
     * 进入搜索页面
     *
     * @return
     */
    @RequestMapping(value = "/lostandfound/search/index", method = RequestMethod.GET)
    public ModelAndView ResolveSearchPage() {
        return new ModelAndView("LostAndFound/search");
    }

    /**
     * 进入个人页面
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/lostandfound/personal", method = RequestMethod.GET)
    public ModelAndView ResolvePersonalPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = lostAndFoundService
                .QueryPersonalLostAndFoundItem(username);
        switch (result.getResultType()) {
            case SUCCESS:
                List<LostAndFoundItem> lostItemList = new ArrayList<>();
                List<LostAndFoundItem> foundItemList = new ArrayList<>();
                List<LostAndFoundItem> didFoundItemList = new ArrayList<>();
                for (LostAndFoundItem lostAndFoundItem : result.getResultData()) {
                    if (lostAndFoundItem.getState().equals(1)) {
                        didFoundItemList.add(lostAndFoundItem);
                    } else {
                        if (lostAndFoundItem.getLostType().equals(0)) {
                            lostItemList.add(lostAndFoundItem);
                        } else {
                            foundItemList.add(lostAndFoundItem);
                        }
                    }
                }
                modelAndView.setViewName("LostAndFound/personal");
                modelAndView.addObject("LostItemList", lostItemList);
                modelAndView.addObject("FoundItemList", foundItemList);
                modelAndView.addObject("DidFoundItemList", didFoundItemList);
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("LostAndFound/personal");
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍后再试");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    @RequestMapping(value = "/lostandfound/edit/id/{id}", method = RequestMethod.GET)
    public ModelAndView EditLostAndFoundInfo(HttpServletRequest request, @PathVariable("id") Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<LostAndFoundItem, DataBaseResultEnum> queryResult = lostAndFoundService
                .QueryLostAndFoundItemByID(id);
        switch (queryResult.getResultType()) {
            case SUCCESS:
                LostAndFoundItem lostAndFoundItem = queryResult.getResultData();
                if (username != null && !username.trim().isEmpty() && username.equals(lostAndFoundItem.getUsername())) {
                    modelAndView.addObject("LostAndFoundItemID", id);
                    modelAndView.addObject("LostAndFoundItemName", lostAndFoundItem.getName());
                    modelAndView.addObject("LostAndFoundItemDescription", lostAndFoundItem.getDescription());
                    modelAndView.addObject("LostAndFoundItemLocation", lostAndFoundItem.getLocation());
                    modelAndView.addObject("LostAndFoundItemLostType", lostAndFoundItem.getLostType());
                    modelAndView.addObject("LostAndFoundItemItemType", LOSTANDFOUND_ITEM_TYPE[lostAndFoundItem.getItemType()]);
                    modelAndView.addObject("LostAndFoundItemItemTypeValue", lostAndFoundItem.getItemType());
                    modelAndView.addObject("LostAndFoundItemQQ", lostAndFoundItem.getQq());
                    modelAndView.addObject("LostAndFoundItemWechat", lostAndFoundItem.getWechat());
                    modelAndView.addObject("LostAndFoundItemPhone", lostAndFoundItem.getPhone());
                    modelAndView.setViewName("LostAndFound/edit");
                } else {
                    modelAndView.setViewName("Error/commonError");
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                    modelAndView.addObject("ErrorMessage", "你没有权限编辑该失物招领信息");
                }
                break;

            case ERROR:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "服务器异常，请稍候再试");
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("Error/commonError");
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "失物招领信息不存在");
                break;
        }
        return modelAndView;
    }

    /**
     * 关键字查询失物招领信息
     *
     * @param lostType
     * @param keywords
     * @return
     */
    @RequestMapping(value = "/lostandfound/search", method = RequestMethod.POST)
    public ModelAndView SearchLostAndFoundInfo(@RequestParam("type") Integer lostType
            , @RequestParam("keywords") String keywords) {
        ModelAndView modelAndView = new ModelAndView();
        if (lostType < 0 || lostType > 1 || keywords.length() < 1 || keywords.length() > 50) {
            modelAndView.setViewName("Error/commonError");
            modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
            modelAndView.addObject("ErrorMessage", "请求参数不合法");
        } else {
            BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = null;
            if (lostType == 0) {
                result = lostAndFoundService.QueryLostItemWithKeyword(keywords, 0);
            } else {
                result = lostAndFoundService.QueryFoundItemWithKeyword(keywords, 0);
            }
            switch (result.getResultType()) {
                case SUCCESS:
                    modelAndView.setViewName("LostAndFound/searchResult");
                    modelAndView.addObject("KeyWord", keywords);
                    modelAndView.addObject("LostType", lostType);
                    modelAndView.addObject("LostAndFoundItemList", result.getResultData());
                    break;

                case EMPTY_RESULT:
                    modelAndView.setViewName("LostAndFound/searchResult");
                    modelAndView.addObject("LostType", lostType);
                    modelAndView.addObject("KeyWord", keywords);
                    break;

                case ERROR:
                    modelAndView.setViewName("Error/commonError");
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                    modelAndView.addObject("ErrorMessage", "服务器维护中，请稍后再试");
                    break;
            }
        }
        return modelAndView;
    }

    /**
     * 查看ID对应的失物招领信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/lostandfound/detail/id/{id}", method = RequestMethod.GET)
    public ModelAndView GetLostAndFoundItemDetailInfo(@PathVariable("id") Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        BaseResult<LostAndFoundInfo, DataBaseResultEnum> result = lostAndFoundService
                .QueryLostAndFoundInfoByID(id);
        switch (result.getResultType()) {
            case SUCCESS:
                LostAndFoundInfo lostAndFoundInfo = result.getResultData();
                if (lostAndFoundInfo.getLostAndFoundItem().getState() == 1) {
                    //物品已经找回
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                    modelAndView.addObject("ErrorMessage", "该失物招领物品已经找回");
                    modelAndView.setViewName("Error/commonError");
                } else if (lostAndFoundInfo.getLostAndFoundItem().getState() == 2) {
                    //信息被管理员屏蔽
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院二手交易-错误");
                    modelAndView.addObject("ErrorMessage", "该失物招领信息因违规被管理员删除");
                    modelAndView.setViewName("Error/commonError");
                } else {
                    modelAndView.addObject("LostAndFoundInfo", lostAndFoundInfo);
                    modelAndView.setViewName("LostAndFound/ItemDetail");
                }
                break;

            case EMPTY_RESULT:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "失物招领信息不存在");
                modelAndView.setViewName("Error/commonError");
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再试");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    /**
     * 根据分类查找失物信息
     *
     * @param type
     * @return
     */
    @RequestMapping(value = "/lostandfound/lostinfo/type/{type}", method = RequestMethod.GET)
    public ModelAndView SearchLostInfoByType(@PathVariable("type") Integer type) {
        ModelAndView modelAndView = new ModelAndView();
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = lostAndFoundService
                .QueryLostItemByType(type, 0);
        switch (result.getResultType()) {
            case SUCCESS:
                modelAndView.setViewName("LostAndFound/TypeResult");
                modelAndView.addObject("LostAndFoundItemList", result.getResultData());
                modelAndView.addObject("ItemType", type);
                modelAndView.addObject("LostType", 0);
                modelAndView.addObject("KeyWord", LOSTANDFOUND_ITEM_TYPE[type]);
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("LostAndFound/TypeResult");
                modelAndView.addObject("ItemType", type);
                modelAndView.addObject("LostType", 0);
                modelAndView.addObject("KeyWord", LOSTANDFOUND_ITEM_TYPE[type]);
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再试");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    /**
     * 根据分类查找招领信息
     *
     * @param type
     * @return
     */
    @RequestMapping(value = "/lostandfound/foundinfo/type/{type}", method = RequestMethod.GET)
    public ModelAndView SearchFoundInfoByType(@PathVariable("type") Integer type) {
        ModelAndView modelAndView = new ModelAndView();
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = lostAndFoundService
                .QueryFoundItemByType(type, 0);
        switch (result.getResultType()) {
            case SUCCESS:
                modelAndView.setViewName("LostAndFound/TypeResult");
                modelAndView.addObject("LostAndFoundItemList", result.getResultData());
                modelAndView.addObject("ItemType", type);
                modelAndView.addObject("LostType", 1);
                modelAndView.addObject("KeyWord", LOSTANDFOUND_ITEM_TYPE[type]);
                break;

            case EMPTY_RESULT:
                modelAndView.setViewName("LostAndFound/TypeResult");
                modelAndView.addObject("ItemType", type);
                modelAndView.addObject("LostType", 1);
                modelAndView.addObject("KeyWord", LOSTANDFOUND_ITEM_TYPE[type]);
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院失物招领-错误");
                modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再试");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    /**
     * 分页查询失物信息
     *
     * @param request
     * @param start
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/lostinfo/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<LostAndFoundItem>> GetLostInfo(HttpServletRequest request
            , @PathVariable("start") int start) {
        DataJsonResult<List<LostAndFoundItem>> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.isBlank(username)) {
            result.setSuccess(false);
            result.setErrorMessage("用户身份凭证已过期，请重新登录");
        } else {
            BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> queryResult = lostAndFoundService
                    .QueryLostItem(start);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    result.setData(queryResult.getResultData());
                    result.setSuccess(true);
                    break;

                case ERROR:
                    result.setSuccess(false);
                    result.setErrorMessage("服务器异常，请稍候再试");
                    break;

                case EMPTY_RESULT:
                    result.setSuccess(true);
                    break;
            }
        }
        return result;
    }

    /**
     * 分页查询招领信息
     *
     * @param request
     * @param start
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/foundinfo/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<LostAndFoundItem>> GetFoundInfo(HttpServletRequest request
            , @PathVariable("start") int start) {
        DataJsonResult<List<LostAndFoundItem>> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.isBlank(username)) {
            result.setSuccess(false);
            result.setErrorMessage("用户身份凭证已过期，请重新登录");
        } else {
            BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> queryResult = lostAndFoundService
                    .QueryFoundItem(start);
            switch (queryResult.getResultType()) {
                case SUCCESS:
                    result.setData(queryResult.getResultData());
                    result.setSuccess(true);
                    break;

                case ERROR:
                    result.setSuccess(false);
                    result.setErrorMessage("服务器异常，请稍候再试");
                    break;

                case EMPTY_RESULT:
                    result.setSuccess(true);
                    break;
            }
        }
        return result;
    }

    /**
     * 关键字查询更多失物招领信息
     *
     * @param lostType
     * @param start
     * @param keywords
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/lostinfo/type/{type}/start/{start}", method = RequestMethod.POST)
    @ResponseBody
    public DataJsonResult<List<LostAndFoundItem>> SearchLostAndFoundInfo(@PathVariable("type") Integer lostType
            , @PathVariable("start") Integer start, @RequestParam("keyword") String keywords) {
        DataJsonResult<List<LostAndFoundItem>> jsonResult = new DataJsonResult<>();
        if (lostType < 0 || lostType > 1 || keywords.length() < 1 || keywords.length() > 50) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合法");
        } else {
            BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = null;
            if (lostType.equals(0)) {
                result = lostAndFoundService.QueryLostItemWithKeyword(keywords, start);
            } else {
                result = lostAndFoundService.QueryFoundItemWithKeyword(keywords, start);
            }
            switch (result.getResultType()) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    jsonResult.setData(result.getResultData());
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(true);
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("服务器维护中，请稍后再试");
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 获取失物招领物品预览图
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/info/id/{id}/preview", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetLostAndFoundItemPreviewImage(HttpServletRequest request
            , @PathVariable("id") int id) {
        DataJsonResult<String> result = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.isBlank(username)) {
            result.setSuccess(false);
            result.setErrorMessage("用户身份凭证已过期，请重新登录");
        } else {
            List<String> list = lostAndFoundService.GetLostAndFoundItemPictureURL(username, id);
            if (list != null && !list.isEmpty()) {
                String previewImageURl = list.get(0);
                result.setData(previewImageURl);
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
            }
        }
        return result;
    }

    /**
     * 根据分类查找更多招领信息
     *
     * @param type
     * @param start
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/foundinfo/type/{type}/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<LostAndFoundItem>> SearchFoundInfoByType(@PathVariable("type") Integer type
            , @PathVariable("start") Integer start) {
        DataJsonResult<List<LostAndFoundItem>> jsonResult = new DataJsonResult<>();
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = lostAndFoundService
                .QueryFoundItemByType(type, start);
        switch (result.getResultType()) {
            case SUCCESS:
                jsonResult.setSuccess(true);
                jsonResult.setData(result.getResultData());
                break;

            case EMPTY_RESULT:
                jsonResult.setSuccess(true);
                break;

            case ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("服务器出现异常，请稍候再试");
                break;
        }
        return jsonResult;
    }

    /**
     * 根据分类查找更多失物信息
     *
     * @param type
     * @param start
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/lostinfo/type/{type}/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<LostAndFoundItem>> SearchLostInfoByType(@PathVariable("type") Integer type
            , @PathVariable("start") Integer start) {
        DataJsonResult<List<LostAndFoundItem>> jsonResult = new DataJsonResult<>();
        BaseResult<List<LostAndFoundItem>, DataBaseResultEnum> result = lostAndFoundService
                .QueryLostItemByType(type, start);
        switch (result.getResultType()) {
            case SUCCESS:
                jsonResult.setSuccess(true);
                jsonResult.setData(result.getResultData());
                break;

            case EMPTY_RESULT:
                jsonResult.setSuccess(true);
                break;

            case ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setErrorMessage("服务器出现异常，请稍候再试");
                break;
        }
        return jsonResult;
    }

    /**
     * 确认寻回物品
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/info/id/{id}/didfound", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult DidFoundItem(HttpServletRequest request, @PathVariable("id") Integer id) {
        BaseJsonResult jsonResult = new BaseJsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (StringUtils.isBlank(username)) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("用户身份凭证过期，请重新登录");
        } else {
            BaseResult<LostAndFoundItem, DataBaseResultEnum> result = lostAndFoundService
                    .QueryLostAndFoundItemByID(id);
            switch (result.getResultType()) {
                case SUCCESS:
                    if (username.equals(result.getResultData().getUsername())) {
                        BoolResultEnum boolResultEnum = lostAndFoundService.UpdateLostAndFoundItemState(id, 1);
                        switch (boolResultEnum) {
                            case SUCCESS:
                                jsonResult.setSuccess(true);
                                break;

                            case ERROR:
                                jsonResult.setSuccess(false);
                                jsonResult.setErrorMessage("服务器异常，请稍后再试");
                                break;
                        }
                    } else {
                        jsonResult.setSuccess(false);
                        jsonResult.setErrorMessage("你没有操作该信息的权限");
                    }
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("服务器异常，请稍后再试");
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("该失物招领信息不存在");
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 更新失物招领信息
     *
     * @param id
     * @param lostAndFoundItem
     * @param bindingResult
     * @return
     */
    @RequestMapping(value = "/rest/lostandfound/info/id/{id}", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult UpdateLostAndFoundInfo(@PathVariable("id") Integer id
            , @Validated LostAndFoundItem lostAndFoundItem, BindingResult bindingResult) {
        BaseJsonResult jsonResult = new BaseJsonResult();
        if (bindingResult.hasErrors() || !lostAndFoundItem.containsContactInfo()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合法");
        } else {
            BoolResultEnum boolResultEnum = lostAndFoundService.UpdateLostAndFoundItem(lostAndFoundItem, id);
            switch (boolResultEnum) {
                case SUCCESS:
                    jsonResult.setSuccess(true);
                    break;

                case ERROR:
                    //服务器内部异常
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("服务器异常，请稍候再试");
                    break;
            }
        }
        return jsonResult;
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
     * @param bindingResult
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/rest/lostandfound/info", method = RequestMethod.POST)
    @ResponseBody
    public BaseJsonResult AddLostAndFoundInfo(HttpServletRequest request
            , @Validated LostAndFoundItem lostAndFoundItem, MultipartFile image1
            , MultipartFile image2, MultipartFile image3, MultipartFile image4
            , BindingResult bindingResult) throws IOException {
        BaseJsonResult jsonResult = new BaseJsonResult();
        if (bindingResult.hasErrors() || !lostAndFoundItem.containsContactInfo()) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("请求参数不合法");
        } else if (image1 == null || image1.getSize() <= 0 || image1.getSize() >= MAX_PICTURE_SIZE) {
            jsonResult.setSuccess(false);
            jsonResult.setErrorMessage("不合法的图片文件");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            BaseResult<LostAndFoundItem, BoolResultEnum> result = lostAndFoundService
                    .AddLostAndFoundItem(lostAndFoundItem, username);
            switch (result.getResultType()) {
                case SUCCESS:
                    //添加二手交易数据成功，进行图片上传
                    lostAndFoundService.UploadLostAndFoundItemPicture(result.getResultData().getId(), 1, image1.getInputStream());
                    if (image2 != null && image2.getSize() > 0 && image2.getSize() < MAX_PICTURE_SIZE) {
                        lostAndFoundService.UploadLostAndFoundItemPicture(result.getResultData().getId(), 2, image2.getInputStream());
                        if (image3 != null && image3.getSize() > 0 && image3.getSize() < MAX_PICTURE_SIZE) {
                            lostAndFoundService.UploadLostAndFoundItemPicture(result.getResultData().getId(), 3, image3.getInputStream());
                            if (image4 != null && image4.getSize() > 0 && image4.getSize() < MAX_PICTURE_SIZE) {
                                lostAndFoundService.UploadLostAndFoundItemPicture(result.getResultData().getId(), 4, image4.getInputStream());
                            }
                        }
                    }
                    jsonResult.setSuccess(true);
                    break;

                case ERROR:
                    //服务器内部异常
                    jsonResult.setSuccess(false);
                    jsonResult.setErrorMessage("服务器异常，请稍候再试");
                    break;
            }
        }
        return jsonResult;
    }
}
