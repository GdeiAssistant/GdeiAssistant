package cn.gdeiassistant.core.announcement.controller;

import cn.gdeiassistant.common.pojo.Entity.Announcement;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.common.pojo.Result.JsonResult;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import cn.gdeiassistant.core.information.service.Announcement.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 获取通知公告信息
     */
    @RequestMapping(value = "/api/announcement", method = RequestMethod.GET)
    public DataJsonResult<AnnouncementVO> queryLatestAnnouncement() {
        AnnouncementVO announcement = announcementService.queryLatestAnnouncement();
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
    public JsonResult saveAnnouncement(HttpServletRequest request, @Validated Announcement announcement) {
        announcementService.saveAnnouncement(announcement);
        return new JsonResult(true);
    }
}
