package cn.gdeiassistant.Controller.Dating.Controller;

import cn.gdeiassistant.Exception.DatabaseException.NoAccessException;
import cn.gdeiassistant.Exception.DatingException.RepeatPickException;
import cn.gdeiassistant.Exception.DatingException.SelfPickException;
import cn.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import cn.gdeiassistant.Pojo.Entity.DatingMessage;
import cn.gdeiassistant.Pojo.Entity.DatingPick;
import cn.gdeiassistant.Pojo.Entity.DatingProfile;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Dating.DatingService;
import cn.gdeiassistant.Tools.Utils.StringUtils;
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

@Deprecated
public class DatingController {

    private DatingService datingService;

    private final int MAX_PICTURE_SIZE = 1024 * 1024 * 5;

    /**
     * 进入卖室友首页
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/dating", method = RequestMethod.GET)
    public ModelAndView ResolveDatingIndexPage(HttpServletRequest request)  {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Dating/datingIndex");
        Integer unreadCount = datingService.QueryUserUnReadDatingMessageCount(request.getSession().getId());
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
    public ModelAndView ResolveDatingPickPage(HttpServletRequest request, @PathVariable("id") Integer id) throws DataNotExistException, NoAccessException {
        ModelAndView modelAndView = new ModelAndView();
        //只有自己可以查看该撩一下记录
        datingService.VerifyDatingPickViewAccess(request.getSession().getId(), id);
        DatingPick datingPick = datingService.QueryDatingPickById(id);
        modelAndView.setViewName("Dating/datingPick");
        modelAndView.addObject("DatingPick", datingPick);
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
            , @PathVariable("id") Integer id, Integer state) throws DataNotExistException, NoAccessException {
        JsonResult jsonResult = new JsonResult();
        if (!state.equals(-1) && !state.equals(1)) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
        } else {
            datingService.VerifyDatingPickViewAccess(request.getSession().getId(), id);
            datingService.UpdateDatingPickState(id, state);
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
            , @PathVariable("id") Integer id) throws DataNotExistException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("ProfileID", id);
        DatingProfile datingProfile = datingService.QueryDatingProfile(id);
        //获取图片地址
        String url = datingService.GetDatingProfilePictureURL(id);
        //查询浏览者对该卖室友信息的撩一下记录
        DatingPick datingPick = datingService.QueryDatingPick(id, request.getSession().getId());
        if (datingPick != null) {
            boolean pickPageHidden = datingService.CheckIsPickPageHidden(request.getSession()
                    .getId(), datingPick.getPickId());
            if (pickPageHidden) {
                modelAndView.addObject("isContactVisible", 1);
                modelAndView.addObject("isPickNotAvailable", 1);
            }
            modelAndView.addObject("DatingProfile", datingProfile);
            modelAndView.addObject("PictureURL", url);
            modelAndView.setViewName("Dating/datingDetail");
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
            , MultipartFile image) throws IOException {
        if (image == null || image.getSize() <= 0 || image.getSize() >= MAX_PICTURE_SIZE) {
            return new JsonResult(false, "图片文件不能为空");
        } else {
            Integer id = datingService.AddDatingProfile(request.getSession().getId(), datingProfile);
            datingService.UploadPicture(id, image.getInputStream());
            return new JsonResult(true);
        }
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
    public JsonResult AddDatingPick(HttpServletRequest request, DatingPick datingPick, Integer profileId) throws
            SelfPickException, RepeatPickException {
        DatingProfile datingProfile = new DatingProfile();
        datingProfile.setProfileId(profileId);
        datingPick.setDatingProfile(datingProfile);
        if (datingPick.getDatingProfile().getProfileId() == null) {
            return new JsonResult(false, "请求参数不合法");
        }
        if (datingPick.getContent().length() > 50) {
            return new JsonResult(false, "文本内容超过限制");
        }
        datingService.VerifyDatingPickRequestAccess(request.getSession().getId()
                , datingPick.getPickId());
        datingService.AddDatingPick(request.getSession().getId(), datingPick);
        return new JsonResult(true);
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
    public DataJsonResult<List<DatingMessage>> QueryDatingMessage(HttpServletRequest
                                                                          request, @PathVariable("start") Integer start)  {
        List<DatingMessage> datingMessageList = datingService.QueryUserDatingMessage(request.getSession().getId(), start, 10);
        return new DataJsonResult<>(true, datingMessageList);
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
    public DataJsonResult<List<DatingProfile>> QueryDatingProfile(@PathVariable("start") Integer
                                                                          start, @PathVariable("area") Integer area) {
        List<DatingProfile> datingProfileList = datingService.QueryDatingProfile(start, 10, area);
        return new DataJsonResult<>(true, datingProfileList);
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
        datingService.UpdateDatingMessageState(id, 1);
        return new JsonResult(true);
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
        String url = datingService.GetDatingProfilePictureURL(id);
        if (StringUtils.isNotBlank(url)) {
            return new DataJsonResult<>(true, url);
        }
        return new DataJsonResult<>(new JsonResult(false));
    }
}
