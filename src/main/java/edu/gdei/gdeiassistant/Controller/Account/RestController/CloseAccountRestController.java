package edu.gdei.gdeiassistant.Controller.Account.RestController;

import edu.gdei.gdeiassistant.Annotation.UserGroupAccess;
import edu.gdei.gdeiassistant.Pojo.Result.JsonResult;
import edu.gdei.gdeiassistant.Service.Account.CloseAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class CloseAccountRestController {

    @Autowired
    private CloseAccountService closeAccountService;

    /**
     * 删除账号
     *
     * @param request
     * @param password
     * @return
     */
    @RequestMapping(value = "/api/close/submit", method = RequestMethod.POST)
    @UserGroupAccess(group = {2, 3, 6}, rest = true)
    public JsonResult CloseAccount(HttpServletRequest request, String password) throws Exception {
        String username = (String) request.getSession().getAttribute("username");
        closeAccountService.CloseAccount(username, password);
        return new JsonResult(true);
    }
}
