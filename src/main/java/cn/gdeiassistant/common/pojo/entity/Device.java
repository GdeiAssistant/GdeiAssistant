package cn.gdeiassistant.common.pojo.entity;

import cn.gdeiassistant.common.validgroup.device.DeviceDataValidGroup;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;

/**
 * 记录上次使用令牌访问时的设备MAC地址（或以算法或其他方式生成的可以用于唯一识别设备的内容）、请求的IP地址
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Device implements Serializable {

    @NotBlank(message = "设备唯一标识符不能为空", groups = {DeviceDataValidGroup.class})

    private String unionID;

    private String ip;

    public String getUnionID() {
        return unionID;
    }

    public void setUnionID(String unionID) {
        this.unionID = unionID;
    }

    public String getIP() {
        return ip;
    }

    public void setIP(String ip) {
        this.ip = ip;
    }
}
