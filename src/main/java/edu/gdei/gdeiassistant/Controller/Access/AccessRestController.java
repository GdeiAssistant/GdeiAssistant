package edu.gdei.gdeiassistant.Controller.Access;

import edu.gdei.gdeiassistant.Annotation.RestAuthentication;
import edu.gdei.gdeiassistant.Pojo.Entity.User;
import edu.gdei.gdeiassistant.Pojo.Result.DataJsonResult;
import edu.gdei.gdeiassistant.Service.Access.AccessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

@RestController
public class AccessRestController {

    @Autowired
    private AccessService accessService;

    @RequestMapping(value = {"/rest/access", "/rest/access/android", "/rest/access/wechat", "/rest/access/miniapp"}, method = RequestMethod.POST)
    @RestAuthentication
    public DataJsonResult<Set<String>> GetUserAccess(HttpServletRequest request
            , @RequestParam("token") String token) throws Exception {
        User user = (User) request.getAttribute("user");
        Set<String> set = accessService.GetUserAccess(user.getGroup());
        return new DataJsonResult<>(true, set);
    }
}
