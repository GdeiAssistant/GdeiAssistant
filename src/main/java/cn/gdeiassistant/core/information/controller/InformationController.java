package cn.gdeiassistant.core.information.controller;

import cn.gdeiassistant.common.pojo.Entity.Festival;
import cn.gdeiassistant.common.pojo.Result.DataJsonResult;
import cn.gdeiassistant.core.information.pojo.vo.AnnouncementVO;
import cn.gdeiassistant.core.information.pojo.vo.InformationVO;
import cn.gdeiassistant.core.information.pojo.vo.ReadingVO;
import cn.gdeiassistant.core.information.service.Announcement.AnnouncementService;
import cn.gdeiassistant.core.information.service.Reading.ReadingService;
import cn.gdeiassistant.common.tools.Utils.FestivalUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 资讯信息聚合接口：
 * 通知公告 / 校园公众号 / 专题阅读 / 世界上的今日
 * GET /api/information/list
 */
@RestController
public class InformationController {

    @Autowired
    private AnnouncementService announcementService;

    @Autowired
    private ReadingService readingService;

    @RequestMapping(value = "/api/information/list", method = RequestMethod.GET)
    public DataJsonResult<InformationVO> loadInformation() {
        AnnouncementVO notice = announcementService.queryLatestAnnouncement();
        List<AnnouncementVO> notices = announcementService.queryAnnouncementPage(0, 5);
        List<ReadingVO> topics = readingService.loadLatestReadingList();
        Festival festival = FestivalUtils.getFestivalInfo();

        InformationVO information = new InformationVO();
        information.setNotice(notice);
        information.setNotices(notices);
        information.setTopics(topics);
        information.setFestival(festival);

        return new DataJsonResult<>(true, information);
    }
}
