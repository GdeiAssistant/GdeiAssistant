package com.linguancheng.gdeiassistant.Pojo.Charge;

/**
 * 安全版本号记录当前使用的安全校验的版本号,当前为1.1,使用客户端+服务端共同校验的校验方式
 * 客户端校验值为：客户端保留字+时间戳的对称加密后的字符串，再进行一次SHA1哈希散列的结果字符串
 * 客户端类型记录客户端为Android/iOS/UWP，不同的客户端采用不同的校验方式
 * 1.1版本的安全校验最后会返回：用户名+密码+充值金额+保留字+时间戳的对称加密后，再次进行一次SHA1哈希散列的结果的字符串，供客户端校验
 */
public class ChargeSecurity {

    //安全版本号
    private String securityVersion;

    //客户端校验值
    private String clientKeyCode;

    //客户端时间戳
    private String timeStamp;

    //客户端类型
    private String clientType;

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getClientKeyCode() {
        return clientKeyCode;
    }

    public void setClientKeyCode(String clientKeyCode) {
        this.clientKeyCode = clientKeyCode;
    }

    public String getSecurityVersion() {
        return securityVersion;
    }

    public void setSecurityVersion(String securityVersion) {
        this.securityVersion = securityVersion;
    }

    /**
     * 如果信息为空,不进行安全校验
     *
     * @return
     */
    public boolean isSecurityRequired() {
        if (securityVersion != null && !securityVersion.trim().isEmpty()) {
            if (clientKeyCode != null && !clientKeyCode.trim().isEmpty()) {
                if (timeStamp != null && !timeStamp.trim().isEmpty()) {
                    if (clientType != null && !clientType.trim().isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检验时间戳是否过期
     *
     * @return
     */
    public boolean isTimeStampExpired() {
        try {
            int currentTimeStamp = (int) (System.currentTimeMillis() / 1000);
            //超过一分钟即视时间戳为过期
            if (Math.abs(currentTimeStamp - Integer.valueOf(timeStamp)) <= 60) {
                return false;
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return true;
    }
}
