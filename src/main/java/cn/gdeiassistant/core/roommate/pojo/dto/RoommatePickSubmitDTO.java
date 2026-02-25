package cn.gdeiassistant.core.roommate.pojo.dto;

import org.hibernate.validator.constraints.Length;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 提交撩一下请求入参 DTO。
 */
public class RoommatePickSubmitDTO implements Serializable {

    @NotNull
    private Integer profileId;

    @Length(max = 50)
    private String content;

    public Integer getProfileId() { return profileId; }
    public void setProfileId(Integer profileId) { this.profileId = profileId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
