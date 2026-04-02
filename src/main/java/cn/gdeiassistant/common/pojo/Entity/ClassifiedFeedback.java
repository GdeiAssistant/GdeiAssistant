package cn.gdeiassistant.common.pojo.Entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClassifiedFeedback extends Feedback {

    //问题分类
    @NotBlank
    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
