package cn.gdeiassistant.Pojo.YiBan;

import cn.gdeiassistant.Pojo.Entity.YiBanUser;
import com.google.gson.annotations.SerializedName;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
public class YiBanUserInfoJsonResult {

    @SerializedName("status")
    private String status;

    @SerializedName("info")
    private YiBanUser yiBanUser;

    public YiBanUser getYiBanUser() {
        return yiBanUser;
    }

    public void setYiBanUser(YiBanUser yiBanUser) {
        this.yiBanUser = yiBanUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
