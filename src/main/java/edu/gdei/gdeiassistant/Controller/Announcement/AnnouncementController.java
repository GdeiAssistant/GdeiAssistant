package edu.gdei.gdeiassistant.Controller.Announcement;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Pojo.Entity.Announcement;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Announcement.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @RequestMapping(value = "/api/announcement", method = RequestMethod.GET)
    public DataJsonResult<Announcement> QueryLatestAnnouncement() {
        Announcement announcement = announcementService.QueryLatestAnnouncement();
        return new DataJsonResult<>(true, announcement);
    }

    @RequestMapping(value = "/api/announcement", method = RequestMethod.POST)
    @UserGroupAccess(group = 0)
    public JsonResult SaveAnnouncement(HttpServletRequest request, @Validated Announcement announcement) {
        announcementService.SaveAnnouncement(announcement);
        return new JsonResult(true);
    }
}
