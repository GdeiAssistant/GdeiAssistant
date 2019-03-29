package edu.gdei.gdeiassistant.Controller.Dating;

import com.taobao.wsgsvr.WsgException;
import edu.gdei.gdeiassistant.Exception.DatabaseException.DataNotExistException;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingMessage;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingPick;
import edu.gdei.gdeiassistant.Pojo.Entity.DatingProfile;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Dating.DatingService;
import edu.gdei.gdeiassistant.Tools.StringUtils;
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
    public ModelAndView ResolveDatingIndexPage(HttpServletRequest request) throws WsgException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("/Dating/datingIndex");
        String username = (String) request.getSession().getAttribute("username");
        Integer unreadCount = datingService.QueryUserUnReadDatingMessageCount(username);
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
    public ModelAndView ResolveDatingPickPage(HttpServletRequest request, @PathVariable("id") Integer id) throws WsgException {
        ModelAndView modelAndView = new ModelAndView();
        String username = (String) request.getSession().getAttribute("username");
        DatingPick datingPick = datingService.QueryDatingPickById(id);
        if (datingPick.getDatingProfile().getUsername().equals(username)) {
            //只有自己可以查看该撩一下记录
            modelAndView.setViewName("Dating/datingPick");
            modelAndView.addObject("DatingPick", datingPick);
        } else {
            modelAndView.addObject("ErrorTitle", "广东第二师范学院卖室友-错误");
            modelAndView.addObject("ErrorMessage", "你没有权限查看该撩一下记录信息");
            modelAndView.setViewName("Error/commonError");
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
            , @PathVariable("id") Integer id, Integer state) throws WsgException {
        JsonResult jsonResult = new JsonResult();
        String username = (String) request.getSession().getAttribute("username");
        if (!state.equals(-1) && !state.equals(1)) {
            jsonResult.setSuccess(false);
            jsonResult.setMessage("请求参数不合法");
        } else {
            DatingPick datingPick = datingService.QueryDatingPickById(id);
            if (datingPick.getState().equals(0)) {
                if (datingPick.getDatingProfile().getUsername().equals(username)) {
                    datingService.UpdateDatingPickState(id, state);
                    jsonResult.setSuccess(true);
                } else {
                    jsonResult.setSuccess(false);
                    jsonResult.setMessage("你没有权限操作该撩一下记录");
                }
            } else {
                jsonResult.setSuccess(false);
                jsonResult.setMessage("该撩一下记录已处理，请勿重复提交");
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
    public ModelAndView ResolveDatingProfileDetailPage(HttpServletRequest request, @PathVariable("id") Integer id) throws WsgException, DataNotExistException {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("ProfileID", id);
        String username = (String) request.getSession().getAttribute("username");
        DatingProfile datingProfile = datingService.QueryDatingProfile(id);
        //获取图片地址
        String url = datingService.GetDatingProfilePictureURL(id);
        //查询浏览者对该卖室友信息的撩一下记录
        DatingPick datingPick = datingService
                .QueryDatingPick(id, username);
        if (datingPick != null) {
            //如果对方已接受该撩一下请求，则隐藏撩一下界面，显示联系方式
            if (datingPick.getState().equals(1)) {
                modelAndView.addObject("isContactVisible", 1);
                modelAndView.addObject("isPickNotAvailable", 1);
            }
            modelAndView.addObject("DatingProfile", datingProfile);
            modelAndView.addObject("PictureURL", url);
            modelAndView.setViewName("Dating/datingDetail");
        } else {
            //当发布者与浏览者相同时，隐藏撩一下功能
            if (datingProfile.getUsername().equals(username)) {
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
            , MultipartFile image) throws IOException, WsgException {
        if (image == null || image.getSize() <= 0 || image.getSize() >= MAX_PICTURE_SIZE) {
            return new JsonResult(false, "图片文件不能为空");
        } else {
            String username = (String) request.getSession().getAttribute("username");
            Integer id = datingService.AddDatingProfile(username, datingProfile);
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
    public JsonResult AddDatingPick(HttpServletRequest request, DatingPick datingPick, Integer profileId) throws WsgException, DataNotExistException {
        DatingProfile datingProfile = new DatingProfile();
        datingProfile.setProfileId(profileId);
        datingPick.setDatingProfile(datingProfile);
        if (datingPick.getDatingProfile().getProfileId() == null) {
            return new JsonResult(false, "请求参数不合法");
        }
        if (datingPick.getContent().length() > 50) {
            return new JsonResult(false, "文本内容超过限制");
        }
        String username = (String) request.getSession().getAttribute("username");
        DatingPick queryDatingPick = datingService.QueryDatingPick(datingPick.getDatingProfile().getProfileId(), username);
        if (queryDatingPick != null) {
            //对方未拒绝前，不能发起多次撩一下请求
            if (!queryDatingPick.getState().equals(-1)) {
                return new JsonResult(false, "你已发送了撩一下请求，请耐心等待对方回复");
            }
            //对方已拒绝，可以再次发起撩一下请求
            DatingProfile queryDatingProfile = datingService.QueryDatingProfile(datingPick.getDatingProfile().getProfileId());
            if (queryDatingProfile.getUsername().equals(username)) {
                return new JsonResult(false, "不能向自己发布的卖室友信息发送撩一下请求");
            }
            datingService.AddDatingPick(username, datingPick);
            return new JsonResult(true);
        }
        DatingProfile queryDatingProfile = datingService.QueryDatingProfile(datingPick.getDatingProfile().getProfileId());
        if (queryDatingProfile.getUsername().equals(username)) {
            return new JsonResult(false, "不能向自己发布的卖室友信息发送撩一下请求");
        }
        datingService.AddDatingPick(username, datingPick);
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
    public DataJsonResult<List<DatingMessage>> QueryDatingMessage(HttpServletRequest request, @PathVariable("start") Integer start) throws WsgException {
        String username = (String) request.getSession().getAttribute("username");
        List<DatingMessage> datingMessageList = datingService.QueryUserDatingMessage(username, start, 10);
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
    public DataJsonResult<List<DatingProfile>> QueryDatingProfile(@PathVariable("start") Integer start, @PathVariable("area") Integer area) throws WsgException {
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
