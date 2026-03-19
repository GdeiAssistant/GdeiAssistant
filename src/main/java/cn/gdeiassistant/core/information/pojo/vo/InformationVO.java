package cn.gdeiassistant.core.information.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.Festival;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InformationVO implements Serializable {

    private AnnouncementVO notice;
    private List<AnnouncementVO> notices;
    private Festival festival;

    public AnnouncementVO getNotice() {
        return notice;
    }

    public void setNotice(AnnouncementVO notice) {
        this.notice = notice;
    }

    public List<AnnouncementVO> getNotices() {
        return notices;
    }

    public void setNotices(List<AnnouncementVO> notices) {
        this.notices = notices;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }
}
