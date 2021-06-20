package cn.gdeiassistant.Controller.QQ.RestController;

import cn.gdeiassistant.Pojo.Entity.QQUser;
import cn.gdeiassistant.Pojo.Result.DataJsonResult;
import cn.gdeiassistant.Service.QQ.QQAppService;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class QQAppRestController {

    @Autowired
    private QQAppService qqAppService;

    /**
     * 获取微信小程序用户唯一标识信息
     *
     * @param js_code
     * @return
     */
    @RequestMapping(value = "/qq/app/userid", method = RequestMethod.POST)
    public DataJsonResult<QQUser> QQUser(@Validated @NotBlank @RequestParam("code") String js_code) throws Exception {
        QQUser qqUser = qqAppService.GetQQUser(js_code);
        return new DataJsonResult<>(true, qqUser);
    }
}
