package edu.gdei.gdeiassistant.Pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class DatingPick implements Serializable {

    /**
     * 撩一下记录的主键ID
     */
    private Integer pickId;

    /**
     * 撩一下记录对应的卖室友信息
     */
    private DatingProfile datingProfile;

    /**
     * 撩一下记录的提交者
     */
    private String username;

    /**
     * 撩一下内容
     */
    private String content;

    /**
     * 撩一下处理状态，默认为0未处理
     * 值为1时表示已接受，值为-1时表示已拒绝
     */
    private Integer state;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public DatingProfile getDatingProfile() {
        return datingProfile;
    }

    public void setDatingProfile(DatingProfile datingProfile) {
        this.datingProfile = datingProfile;
    }

    public Integer getPickId() {
        return pickId;
    }

    public void setPickId(Integer pickId) {
        this.pickId = pickId;
    }
}
