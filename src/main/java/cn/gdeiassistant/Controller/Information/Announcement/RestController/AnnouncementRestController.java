package cn.gdeiassistant.Controller.Information.Announcement.RestController;

import cn.gdeiassistant.Pojo.Entity.Announcement;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Pojo.Result.JsonResult;
import cn.gdeiassistant.Service.Information.Announcement.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AnnouncementRestController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取通知公告信息
     *
     * @return
     */
    @RequestMapping(value = "/api/announcement", method = RequestMethod.GET)
    public DataJsonResult<Announcement> QueryLatestAnnouncement() {
        Announcement announcement = announcementService.QueryLatestAnnouncement();
        return new DataJsonResult<>(true, announcement);
    }

    /**
     * 发布通知公告信息，仅用户组为管理员的用户可以使用该数据接口
     *
     * @param request
     * @param announcement
     * @return
     */
    @RequestMapping(value = "/api/announcement", method = RequestMethod.POST)
    public JsonResult SaveAnnouncement(HttpServletRequest request, @Validated Announcement announcement) {
        announcementService.SaveAnnouncement(announcement);
        return new JsonResult(true);
    }
}
