package cn.gdeiassistant.core.announcement.controller;

import cn.gdeiassistant.common.i18n.Messages;
import cn.gdeiassistant.common.pojo.result.*;
import cn.gdeiassistant.core.announcement.mapper.AnnouncementMapper;
import cn.gdeiassistant.core.userlogin.service.UserCertificateService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/information/announcement")
public class AnnouncementReadController {
    private final AnnouncementMapper announcements;
    private final UserCertificateService certificates;
    public AnnouncementReadController(AnnouncementMapper announcements, UserCertificateService certificates) {
        this.announcements = announcements; this.certificates = certificates;
    }
    private String username(HttpServletRequest request) throws Exception {
        var user = certificates.getUserLoginCertificate((String) request.getAttribute("sessionId"));
        if (user == null) throw new cn.gdeiassistant.common.exception.tokenvalidexception.TokenExpiredException(Messages.get("token.expired"));
        return user.getUsername();
    }
    @GetMapping("/unread")
    public DataJsonResult<Integer> unread(HttpServletRequest request) throws Exception {
        return new DataJsonResult<>(true, announcements.countUnread(username(request)));
    }
    @PostMapping({"/{id}/read"})
    public JsonResult read(HttpServletRequest request, @PathVariable int id) throws Exception {
        String username = username(request);
        if (announcements.queryAnnouncementById(id) == null) return new JsonResult(false, Messages.forLanguage("announcement.notFound", request.getHeader("Accept-Language")));
        announcements.markRead(username, id);
        return new JsonResult(true);
    }
}
