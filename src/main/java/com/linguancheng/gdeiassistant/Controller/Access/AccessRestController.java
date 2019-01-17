package com.linguancheng.gdeiassistant.Controller.Access;

import com.linguancheng.gdeiassistant.Annotation.RestAuthentication;
import com.linguancheng.gdeiassistant.Pojo.Entity.Access;
import com.linguancheng.gdeiassistant.Pojo.Entity.User;
import com.linguancheng.gdeiassistant.Pojo.Result.DataJsonResult;
import com.linguancheng.gdeiassistant.Service.Access.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class AccessRestController {

    @Autowired
    private AccessService accessService;

    @RequestMapping(value = "/rest/access/android", method = RequestMethod.POST)
    @RestAuthentication
    public DataJsonResult<Access> GetUserAndroidAccess(HttpServletRequest request, @RequestParam("token") String token) throws Exception {
        User user = (User) request.getAttribute("user");
        Access access = accessService.GetUserAndroidAccess(user.getGroup());
        return new DataJsonResult<>(true, access);
    }

    @RequestMapping(value = "/rest/access/wechat", method = RequestMethod.POST)
    @RestAuthentication
    public DataJsonResult<Access> GetUserWechatAccess(HttpServletRequest request, @RequestParam("token") String token) throws Exception {
        User user = (User) request.getAttribute("user");
        Access access = accessService.GetUserWechatAccess(user.getGroup());
        return new DataJsonResult<>(true, access);
    }
}
