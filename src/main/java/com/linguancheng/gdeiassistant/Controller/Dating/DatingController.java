package com.linguancheng.gdeiassistant.Controller.Dating;

import com.linguancheng.gdeiassistant.Enum.Base.DataBaseResultEnum;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingMessage;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingPick;
import com.linguancheng.gdeiassistant.Pojo.Entity.DatingProfile;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.JsonResult;
import com.linguancheng.gdeiassistant.Pojo.Result.BaseResult;
import com.linguancheng.gdeiassistant.Service.Dating.DatingService;
import com.linguancheng.gdeiassistant.Tools.StringUtils;
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
import java.util.List;

@Controller
public class DatingController {

    @Autowired
    private DatingService datingService;

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    /**
     * 进入卖室友首页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dating", method = RequestMethod.GET)
    public ModelAndView ResolveDatingIndexPage(HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Dating/datingIndex");
        String username = (String) request.getSession().getAttribute("username");
        Integer unreadCount = datingService.QueryUserUnReadDatingMessageCount(username).getResultData();
        if (unreadCount != null && !unreadCount.equals(0)) {
            modelAndView.addObject("hasUnReadMessage", 1);
        }
        return modelAndView;
    }

    /**
     * 进入卖室友消息界面
     *
     * @return
     */
    @RequestMapping(value = "/dating/message", method = RequestMethod.GET)
    public ModelAndView ResolveDatingMessagePage() {
        return new ModelAndView("Dating/datingMessage");
    }

    /**
     * 进入卖室友发布界面
     *
     * @return
     */
    @RequestMapping(value = "/dating/publish", method = RequestMethod.GET)
    public ModelAndView ResolveDatingPublishPage() {
        return new ModelAndView("Dating/datingPublish");
    }

    /**
     * 查看撩一下记录详细信息
     *
     * @param request
     * @param id
     * @return
     */
    @RequestMapping(value = "/dating/pick/id/{id}", method = RequestMethod.GET)
    public ModelAndView ResolveDatingPickPage(HttpServletRequest request, @PathVariable("id") Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<DatingPick, DataBaseResultEnum> result = datingService.QueryDatingPickById(id);
        switch (result.getResultType()) {
            case SUCCESS:
                if (result.getResultData().getDatingProfile().getUsername().equals(username)) {
                    //只有自己可以查看该撩一下记录
                    modelAndView.setViewName("Dating/datingPick");
                    modelAndView.addObject("DatingPick", result.getResultData());
                } else {
                    modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
                    modelAndView.addObject("ErrorMessage", "你没有权限查看该撩一下记录信息");
                    modelAndView.setViewName("Error/commonError");
                }
                break;

            case EMPTY_RESULT:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
                modelAndView.addObject("ErrorMessage", "该撩一下记录不存在");
                modelAndView.setViewName("Error/commonError");
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
                modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再试");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    /**
     * 更新撩一下记录的状态
     *
     * @param id
     * @param state
     * @return
     */
    @RequestMapping(value = "/dating/pick/id/{id}", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult UpdateDatingPickState(HttpServletRequest request
            , @PathVariable("id") Integer id, Integer state) {
        JsonResult jsonResult = new JsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (!state.equals(-1) && !state.equals(1)) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
        } else {
            BaseResult<DatingPick, DataBaseResultEnum> result = datingService.QueryDatingPickById(id);
            switch (result.getResultType()) {
                case SUCCESS:
                    if (result.getResultData().getState().equals(0)) {
                        if (result.getResultData().getDatingProfile().getUsername().equals(username)) {
                            DataBaseResultEnum dataBaseResultEnum = datingService
                                    .UpdateDatingPickState(id, state);
                            switch (dataBaseResultEnum) {
                                case SUCCESS:
                                    jsonResult.setSuccess(true);
                                    break;

                                case ERROR:
                                    jsonResult.setSuccess(false);
                                    jsonResult.setMessage("系统异常，请稍后再试");
                                    break;
                            }
                        } else {
                            jsonResult.setSuccess(false);
                            jsonResult.setMessage("你没有权限操作该撩一下记录");
                        }
                    } else {
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("该撩一下记录已处理，请勿重复提交");
                    }
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("系统异常，请稍后再试");
                    break;

                case EMPTY_RESULT:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("该撩一下记录不存在");
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 查看卖室友详细信息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/dating/profile/id/{id}", method = RequestMethod.GET)
    public ModelAndView ResolveDatingProfileDetailPage(HttpServletRequest request
            , @PathVariable("id") Integer id) {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("ProfileID", id);
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<DatingProfile, DataBaseResultEnum> queryDatingProfileResult = datingService
                .QueryDatingProfile(id);
        switch (queryDatingProfileResult.getResultType()) {
            case SUCCESS:
                //获取图片地址
                String url = datingService.GetDatingProfilePictureURL(id);
                //查询浏览者对该卖室友信息的撩一下记录
                BaseResult<DatingPick, DataBaseResultEnum> queryDatingPickResult = datingService
                        .QueryDatingPick(id, username);
                switch (queryDatingPickResult.getResultType()) {
                    case SUCCESS:
                        //如果对方已接受该撩一下请求，则隐藏撩一下界面，显示联系方式
                        if (queryDatingPickResult.getResultData().getState().equals(1)) {
                            modelAndView.addObject("isContactVisible", 1);
                            modelAndView.addObject("isPickNotAvailable", 1);
                        }
                        modelAndView.addObject("DatingProfile", queryDatingProfileResult.getResultData());
                        modelAndView.addObject("PictureURL", url);
                        modelAndView.setViewName("Dating/datingDetail");
                        break;

                    case ERROR:
                        modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
                        modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再试");
                        modelAndView.setViewName("Error/commonError");
                        break;

                    case EMPTY_RESULT:
                        //当发布者与浏览者相同时，隐藏撩一下功能
                        if (queryDatingProfileResult.getResultData().getUsername().equals(username)) {
                            modelAndView.addObject("isContactVisible", 1);
                            modelAndView.addObject("isPickNotAvailable", 1);
                        }
                        modelAndView.addObject("DatingProfile", queryDatingProfileResult.getResultData());
                        modelAndView.addObject("PictureURL", url);
                        modelAndView.setViewName("Dating/datingDetail");
                        break;
                }
                break;

            case EMPTY_RESULT:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
                modelAndView.addObject("ErrorMessage", "该卖室友信息不存在");
                modelAndView.setViewName("Error/commonError");
                break;

            case ERROR:
                modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
                modelAndView.addObject("ErrorMessage", "服务器出现异常，请稍候再试");
                modelAndView.setViewName("Error/commonError");
                break;
        }
        return modelAndView;
    }

    /**
     * 提交卖室友信息
     *
     * @param request
     * @param datingProfile
     * @return
     */
    @RequestMapping(value = "/dating/profile", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult AddDatingProfile(HttpServletRequest request, @Validated DatingProfile datingProfile
            , MultipartFile image, BindingResult bindingResult) throws IOException {
        JsonResult jsonResult = new JsonResult();
        if (image == null || image.getSize() <= 0 || image.getSize() >= MAX_PICTURE_SIZE) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("不合法的图片文件");
        } else if (bindingResult.hasErrors()) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            BaseResult<Integer, DataBaseResultEnum> result = datingService.AddDatingProfile(username, datingProfile);
            switch (result.getResultType()) {
                case SUCCESS:
                    datingService.UploadPicture(result.getResultData(), image.getInputStream());
                    jsonResult.setSuccess(true);
                    break;

                case ERROR:
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("系统异常，请稍候再试");
                    break;
            }
        }
        return jsonResult;
    }

    /**
     * 提交撩一下请求
     *
     * @param request
     * @param datingPick
     * @return
     */
    @RequestMapping(value = "/dating/pick", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult AddDatingPick(HttpServletRequest request, DatingPick datingPick, Integer profileId) {
        JsonResult jsonResult = new JsonResult();
        DatingProfile datingProfile = new DatingProfile();
        datingProfile.setProfileId(profileId);
        datingPick.setDatingProfile(datingProfile);
        if (datingPick.getDatingProfile().getProfileId() == null) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
            return jsonResult;
        }
        if (datingPick.getContent().length() > 50) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("文本内容超过限制");
            return jsonResult;
        }
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<DatingPick, DataBaseResultEnum> result = datingService.QueryDatingPick(datingPick
                .getDatingProfile().getProfileId(), username);
        switch (result.getResultType()) {
            case SUCCESS:
                //对方未拒绝前，不能发起多次撩一下请求
                if (!result.getResultData().getState().equals(-1)) {
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("你已发送了撩一下请求，请耐心等待对方回复");
                } else {
                    //对方已拒绝，可以再次发起撩一下请求
                    BaseResult<DatingProfile, DataBaseResultEnum> datingProfileResult = datingService
                            .QueryDatingProfile(datingPick.getDatingProfile().getProfileId());
                    switch (datingProfileResult.getResultType()) {
                        case SUCCESS:
                            if (datingProfileResult.getResultData().getUsername().equals(username)) {
                                jsonResult.setSuccess(false);
                                jsonResult.setMessage("不能向自己发布的卖室友信息发送撩一下请求");
                            } else {
                                DataBaseResultEnum dataBaseResultEnum = datingService.AddDatingPick(username, datingPick);
                                switch (dataBaseResultEnum) {
                                    case SUCCESS:
                                        jsonResult.setSuccess(true);
                                        break;

                                    case ERROR:
                                        jsonResult.setSuccess(false);
                                        jsonResult.setMessage("系统异常，请稍后再试");
                                        break;
                                }
                            }
                            break;

                        case ERROR:
                            jsonResult.setSuccess(false);
                            jsonResult.setMessage("系统异常，请稍后再试");
                            break;

                        case EMPTY_RESULT:
                            jsonResult.setSuccess(false);
                            jsonResult.setMessage("卖室友信息不存在，撩一下请求失败");
                            break;
                    }
                }
                break;

            case ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("系统异常，请稍后再试");
                break;

            case EMPTY_RESULT:
                BaseResult<DatingProfile, DataBaseResultEnum> datingProfileResult = datingService
                        .QueryDatingProfile(datingPick.getDatingProfile().getProfileId());
                switch (datingProfileResult.getResultType()) {
                    case SUCCESS:
                        if (datingProfileResult.getResultData().getUsername().equals(username)) {
                            jsonResult.setSuccess(false);
                            jsonResult.setMessage("不能向自己发布的卖室友信息发送撩一下请求");
                        } else {
                            DataBaseResultEnum dataBaseResultEnum = datingService.AddDatingPick(username, datingPick);
                            switch (dataBaseResultEnum) {
                                case SUCCESS:
                                    jsonResult.setSuccess(true);
                                    break;

                                case ERROR:
                                    jsonResult.setSuccess(false);
                                    jsonResult.setMessage("系统异常，请稍后再试");
                                    break;
                            }
                        }
                        break;

                    case ERROR:
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("系统异常，请稍后再试");
                        break;

                    case EMPTY_RESULT:
                        jsonResult.setSuccess(false);
                        jsonResult.setMessage("卖室友信息不存在，撩一下请求失败");
                        break;
                }
                break;
        }
        return jsonResult;
    }

    /**
     * 分页查询用户卖室友消息
     *
     * @param request
     * @param start
     * @return
     */
    @RequestMapping(value = "/dating/message/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<DatingMessage>> QueryDatingMessage(HttpServletRequest request
            , @PathVariable("start") Integer start) {
        DataJsonResult<List<DatingMessage>> jsonResult = new DataJsonResult<>();
        String username = (String) request.getSession().getAttribute("username");
        BaseResult<List<DatingMessage>, DataBaseResultEnum> result = datingService
                .QueryUserDatingMessage(username, start, 10);
        switch (result.getResultType()) {
            case SUCCESS:
                jsonResult.setSuccess(true);
                jsonResult.setData(result.getResultData());
                break;

            case ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("系统异常，请稍后再试");
                break;

            case EMPTY_RESULT:
                jsonResult.setSuccess(true);
                break;
        }
        return jsonResult;
    }

    /**
     * 获取卖室友信息列表
     *
     * @param start
     * @param area
     * @return
     */
    @RequestMapping(value = "/dating/profile/area/{area}/start/{start}", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<List<DatingProfile>> QueryDatingProfile(@PathVariable("start") Integer start
            , @PathVariable("area") Integer area) {
        DataJsonResult<List<DatingProfile>> jsonResult = new DataJsonResult<>();
        BaseResult<List<DatingProfile>, DataBaseResultEnum> result = datingService
                .QueryDatingProfile(start, 10, area);
        switch (result.getResultType()) {
            case SUCCESS:
                jsonResult.setSuccess(true);
                jsonResult.setData(result.getResultData());
                break;

            case ERROR:
                jsonResult.setSuccess(false);
                jsonResult.setMessage("系统异常，请稍后再试");
                break;

            case EMPTY_RESULT:
                jsonResult.setSuccess(true);
                break;
        }
        return jsonResult;
    }

    /**
     * 已读卖室友消息
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/dating/message/id/{id}/read", method = RequestMethod.POST)
    @ResponseBody
    public JsonResult ReadDatingMessage(@PathVariable("id") Integer id) {
        JsonResult result = new JsonResult();
        DataBaseResultEnum dataBaseResultEnum = datingService.UpdateDatingMessageState(id, 1);
        switch (dataBaseResultEnum) {
            case SUCCESS:
                result.setSuccess(true);
                break;

            case ERROR:
                result.setSuccess(false);
                result.setMessage("系统异常，请稍后再试");
                break;
        }
        return result;
    }

    /**
     * 获取卖室友信息的照片
     *
     * @param id
     * @return
     */
    @RequestMapping(value = "/dating/profile/id/{id}/picture", method = RequestMethod.GET)
    @ResponseBody
    public DataJsonResult<String> GetDatingProfilePicture(@PathVariable("id") Integer id) {
        DataJsonResult<String> result = new DataJsonResult<>();
        String url = datingService.GetDatingProfilePictureURL(id);
        if (StringUtils.isNotBlank(url)) {
            result.setSuccess(true);
            result.setData(url);
        } else {
            result.setSuccess(false);
        }
        return result;
    }
}
