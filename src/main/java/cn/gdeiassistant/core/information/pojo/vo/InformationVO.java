package cn.gdeiassistant.core.information.pojo.vo;

import cn.gdeiassistant.common.pojo.Entity.Festival;
import cn.gdeiassistant.common.pojo.Entity.WechatAccount;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InformationVO implements Serializable {

    private AnnouncementVO notice;
    private List<WechatAccount> accounts;
    private List<ReadingVO> topics;
    private Festival festival;

    public AnnouncementVO getNotice() {
        return notice;
    }

    public void setNotice(AnnouncementVO notice) {
        this.notice = notice;
    }

    public List<WechatAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<WechatAccount> accounts) {
        this.accounts = accounts;
    }

    public List<ReadingVO> getTopics() {
        return topics;
    }

    public void setTopics(List<ReadingVO> topics) {
        this.topics = topics;
    }

    public Festival getFestival() {
        return festival;
    }

    public void setFestival(Festival festival) {
        this.festival = festival;
    }
}
