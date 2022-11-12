package cn.gdeiassistant.Service.ThirdParty.QQ;

import cn.gdeiassistant.Pojo.Entity.QQUser;
import cn.gdeiassistant.Tools.SpringUtils.QQUtils;
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
    public QQUser GetQQUser(String js_code) throws Exception {
        return qqUtils.GetQQUser(js_code);
    }
}
