package cn.gdeiassistant.core.qq.service;

import cn.gdeiassistant.common.pojo.Entity.QQUser;
import cn.gdeiassistant.common.tools.SpringUtils.QQUtils;
import org.springframework.beans.factory.annotation.Autowired;

//@Component
@Deprecated
public class QQAppService {

    @Autowired
    private QQUtils qqUtils;

    /**
     * 使用登录凭证获取用户标识信息
     *
     * @param js_code
     * @return
     */
    public QQUser getQQUser(String js_code) throws Exception {
        return qqUtils.GetQQUser(js_code);
    }
}
